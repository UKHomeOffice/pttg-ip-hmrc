package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
public class UnauthorizedHttpClientErrorExceptionRetryPolicy extends SimpleRetryPolicy {

    private static final Map<Class<? extends Throwable>, Boolean> COMPULSORY_RETRYABLE_EXCEPTIONS = Collections.singletonMap(HttpClientErrorException.class, true);

    UnauthorizedHttpClientErrorExceptionRetryPolicy(final int maxAttempts) {
        super(maxAttempts, COMPULSORY_RETRYABLE_EXCEPTIONS);
    }

    @Override
    public boolean canRetry(final RetryContext context) {
        final Throwable lastThrowable = context.getLastThrowable();

        final boolean noExceptionThrown = lastThrowable == null;
        final boolean isRetryableExceptionAndHasRetriesLeft = super.canRetry(context);

        return isRetryableExceptionAndHasRetriesLeft && (noExceptionThrown || isUnauthorizedHttpClientErrorException(lastThrowable));
    }

    private boolean isUnauthorizedHttpClientErrorException(final Throwable throwable) {
        final HttpClientErrorException httpClientErrorException = (HttpClientErrorException) throwable;
        return httpClientErrorException.getStatusCode().equals(UNAUTHORIZED);
    }
}