package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;
import uk.gov.digital.ho.pttg.application.retry.RetryTemplateBuilder;
import uk.gov.digital.ho.pttg.dto.AccessCode;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

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

    private final RestTemplate restTemplate;
    private final URI accessUri;
    private final RequestHeaderData requestHeaderData;
    private final RetryTemplate retryTemplate;
    private final int maxRetryAttempts;
    private Optional<AccessCode> accessCode = Optional.ofNullable(null);

    public HmrcAccessCodeClient(RestTemplate restTemplate,
                                RequestHeaderData requestHeaderData,
                                @Value("${base.hmrc.access.code.url}") String baseAccessCodeUrl,
                                @Value("${hmrc.access.service.retry.attempts}") int maxRetryAttempts,
                                @Value("${hmrc.access.service.retry.delay}") long retryDelayInMillis) {
        this.restTemplate = restTemplate;
        this.accessUri = URI.create(baseAccessCodeUrl).resolve(ACCESS_ENDPOINT_PATH);
        this.requestHeaderData = requestHeaderData;

        this.maxRetryAttempts = maxRetryAttempts;
        this.retryTemplate = new RetryTemplateBuilder(this.maxRetryAttempts)
                .retryHttpServerErrors()
                .retryConnectionRefusedErrors()
                .withBackOffPeriod(retryDelayInMillis)
                .addListener(new RetryLogger())
                .build();
    }

    public String getAccessCode() throws Exception {
        if(isAccessCodeStale()) {
            refreshAccessCode();
        }
        return accessCode.get().getCode();
    }

    public void refreshAccessCode() throws Exception {
        getAccessCodeWithRetries();
    }

    private boolean isAccessCodeStale() {
        if (!accessCode.isPresent()) {
            return true;
        }
        if (!LocalDateTime.now().isBefore(accessCode.get().getExpiry())) {
            return true;
        }
        return false;
    }

    private synchronized void getAccessCodeWithRetries() throws Exception {
        // TODO This try catch is not required on jdk >= 1.8.0_131.  Remove it once we can be sure all build and deployment jdks are up to date.
        try {
            accessCode = Optional.of(this.retryTemplate.execute(context -> {
                    log.info("Attempting to fetch the latest access code. Attempt number {} of {}", context.getRetryCount() + 1, maxRetryAttempts,
                            value(EVENT, HMRC_API_CALL_ATTEMPT));
                    return requestAccessCode();
            }));
        } catch(Exception e) {
            log.info("Rethrowing following exception to workaround an issue on jdk < 1.8.0_131", e);
            throw e;
        }

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
