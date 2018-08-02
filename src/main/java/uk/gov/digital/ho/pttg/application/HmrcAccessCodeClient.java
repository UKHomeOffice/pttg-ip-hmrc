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
import uk.gov.digital.ho.pttg.api.RequestData;
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
import static uk.gov.digital.ho.pttg.api.RequestData.*;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_API_CALL_ATTEMPT;

@Component
@Slf4j
public class HmrcAccessCodeClient {
    private static final String ACCESS_ENDPOINT_PATH = "/access";

    private final RestTemplate restTemplate;
    private final URI accessUri;
    private final RequestData requestData;
    private final RetryTemplate retryTemplate;
    private final int maxRetryAttempts;
    private Optional<AccessCode> accessCode = Optional.ofNullable(null);

    public HmrcAccessCodeClient(final RestTemplate restTemplate,
                                final RequestData requestData,
                                @Value("${base.hmrc.access.code.url}") final String baseAccessCodeUrl,
                                @Value("${hmrc.access.service.retry.attempts}") final int maxRetryAttempts,
                                @Value("${hmrc.access.service.retry.delay}") final long retryDelayInMillis) {
        this.restTemplate = restTemplate;
        this.accessUri = URI.create(baseAccessCodeUrl).resolve(ACCESS_ENDPOINT_PATH);
        this.requestData = requestData;

        this.maxRetryAttempts = maxRetryAttempts;
        this.retryTemplate = new RetryTemplateBuilder(this.maxRetryAttempts)
                .retryHttpServerErrors()
                .retryConnectionRefusedErrors()
                .withBackOffPeriod(retryDelayInMillis)
                .addListener(new RetryLogger())
                .build();
    }

    public String getAccessCode() {
        if(accessCode.isPresent() && LocalDateTime.now().isBefore(accessCode.get().getExpiry())) {
            return accessCode.get().getCode();
        }
        return getAccessCodeWithRetries();
    }

    public void invalidateAccessCode() {
        accessCode = Optional.ofNullable(null);
    }

    private String getAccessCodeWithRetries() {
        return this.retryTemplate.execute(context -> {
            log.info("Attempting to fetch the latest access code. Attempt number {} of {}", context.getRetryCount() + 1, maxRetryAttempts,
                    value(EVENT, HMRC_API_CALL_ATTEMPT));
            accessCode = Optional.of(requestAccessCode());
            return accessCode.get().getCode();
        });
    }

    private AccessCode requestAccessCode() {
        return restTemplate.exchange(accessUri, GET, getHttpEntity(), AccessCode.class).getBody();
    }

    private HttpEntity getHttpEntity() {
        final HttpHeaders headers = new HttpHeaders();

        headers.add(AUTHORIZATION, requestData.hmrcBasicAuth());
        headers.add(SESSION_ID_HEADER, requestData.sessionId());
        headers.add(CORRELATION_ID_HEADER, requestData.correlationId());
        headers.add(USER_ID_HEADER, requestData.userId());
        headers.setContentType(APPLICATION_JSON);

        return new HttpEntity<>(headers);
    }

    private static class RetryLogger extends RetryListenerSupport {
        @Override
        public <T, E extends Throwable> void onError(final RetryContext context, final RetryCallback<T, E> callback, final Throwable throwable) {
            log.warn("An error occurred while attempting to fetch Access Code.", context.getLastThrowable());
        }

        @Override
        public <T, E extends Throwable> void close(final RetryContext context, final RetryCallback<T, E> callback, final Throwable throwable) {
            final boolean isSuccessful = isNull(throwable);
            if (isSuccessful) {
                log.info("Successfully retrieved Access Code.");
            } else {
                log.error("Failed to fetch Access Code after {} attempts. Will not try again.", context.getRetryCount());
            }
        }
    }
}
