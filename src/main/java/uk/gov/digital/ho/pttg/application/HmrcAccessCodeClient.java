package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;
import uk.gov.digital.ho.pttg.application.retry.RetryTemplateBuilder;
import uk.gov.digital.ho.pttg.dto.AccessCode;

import java.net.URI;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;
import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.digital.ho.pttg.api.RequestHeaderData.*;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_API_CALL_ATTEMPT;

@Component
@Slf4j
public class HmrcAccessCodeClient {
    private static final String ACCESS_ENDPOINT_PATH = "/access";
    private static final String REPORT_ACCESS_ENDPOINT_PATH = "/access/{accessCode}/report";

    private final RestTemplate restTemplate;
    private final String baseAccessCodeUrl;
    private final URI accessUri;
    private final RequestHeaderData requestHeaderData;
    private final RetryTemplate retryTemplate;
    private final int maxRetryAttempts;
    private Optional<AccessCode> accessCode = Optional.ofNullable(null);

    public HmrcAccessCodeClient(@Qualifier("hmrcAccessCodeRestTemplate") RestTemplate restTemplate,
                                RequestHeaderData requestHeaderData,
                                @Value("${base.hmrc.access.code.url}") String baseAccessCodeUrl,
                                @Value("${hmrc.access.service.retry.attempts}") int maxRetryAttempts,
                                @Value("${hmrc.access.service.retry.delay}") long retryDelayInMillis) {
        this.restTemplate = restTemplate;
        this.baseAccessCodeUrl = baseAccessCodeUrl;
        this.accessUri = URI.create(baseAccessCodeUrl + ACCESS_ENDPOINT_PATH);
        this.requestHeaderData = requestHeaderData;

        this.maxRetryAttempts = maxRetryAttempts;
        this.retryTemplate = new RetryTemplateBuilder(this.maxRetryAttempts)
                .retryHttpServerErrors()
                .retryConnectionRefusedErrors()
                .withBackOffPeriod(retryDelayInMillis)
                .addListener(new RetryLogger())
                .build();
    }

    public String getAccessCode() {

        if (accecssCodeIsStale()) {
            loadLatestAccessCode();
        }

        return accessCode.get().getCode();
    }

    public void loadLatestAccessCode() {
        log.info("Refresh the cached Access Code");
        getAccessCodeWithRetries();
        log.info("Cached Access Code refreshed");
    }

    public void reportBadAccessCode() {
        UriBuilderFactory factory = new DefaultUriBuilderFactory();
        final URI reportUri = factory.expand(baseAccessCodeUrl + REPORT_ACCESS_ENDPOINT_PATH, singletonMap("accessCode", accessCode.get().getCode()));
        restTemplate.postForLocation(reportUri, getHttpEntity());
    }

    private boolean accecssCodeIsStale() {

        if (!accessCode.isPresent()) {
            log.debug("No cached Access Code available yet");
            return true;
        }

        if (accessCode.get().needsRefreshing()) {
            log.debug("The cached Access Code should have been refreshed");
            return true;
        }

        if (accessCode.get().hasExpired()) {
            log.debug("The cached Access Code has expired");
            return true;
        }

        return false;
    }

    private synchronized void getAccessCodeWithRetries() {
        AccessCode fetchedAccessCode = this.retryTemplate.execute(context -> {
            try {
                log.info("Attempting to fetch the latest access code. Attempt number {} of {}", context.getRetryCount() + 1, maxRetryAttempts,
                        value(EVENT, HMRC_API_CALL_ATTEMPT));
                return requestAccessCode();
            } catch (Exception e) {
                log.info("Rethrowing following exception to workaround an issue on jdk < 1.8.0_131", e);
                throw e;
            }
        });

        accessCode = Optional.of(fetchedAccessCode);
    }

    private AccessCode requestAccessCode() {
        return restTemplate.exchange(accessUri, GET, getHttpEntity(), AccessCode.class).getBody();
    }

    private HttpEntity getHttpEntity() {

        HttpHeaders headers = new HttpHeaders();

        headers.add(AUTHORIZATION, requestHeaderData.hmrcBasicAuth());
        headers.add(SESSION_ID_HEADER, requestHeaderData.sessionId());
        headers.add(CORRELATION_ID_HEADER, requestHeaderData.correlationId());
        headers.add(USER_ID_HEADER, requestHeaderData.userId());
        headers.setContentType(APPLICATION_JSON);

        return new HttpEntity<>(headers);
    }

    private static class RetryLogger extends RetryListenerSupport {
        @Override
        public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            log.warn("An error occurred while attempting to fetch Access Code.", context.getLastThrowable());
        }

        @Override
        public <T, E extends Throwable> void close(RetryContext context,  RetryCallback<T, E> callback,  Throwable throwable) {
            boolean isSuccessful = isNull(throwable);
            if (isSuccessful) {
                log.debug("Successfully retrieved Access Code.");
            } else {
                log.error("Failed to fetch Access Code after {} attempts. Will not try again.", context.getRetryCount());
            }
        }
    }
}
