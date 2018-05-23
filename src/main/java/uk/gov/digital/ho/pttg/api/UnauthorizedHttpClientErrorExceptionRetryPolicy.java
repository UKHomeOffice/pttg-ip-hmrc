package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.context.RetryContextSupport;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
public class UnauthorizedHttpClientErrorExceptionRetryPolicy implements RetryPolicy {

    private static final Class<HttpClientErrorException> HTTP_CLIENT_ERROR_EXCEPTION = HttpClientErrorException.class;

    private final int maxAttempts;

    public UnauthorizedHttpClientErrorExceptionRetryPolicy(final int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public RetryContext open(final RetryContext parent) {
        return new RetryContextSupport(parent);
    }

    @Override
    public void registerThrowable(final RetryContext context, final Throwable throwable) {
        Class<RetryContextSupport> contextClass = RetryContextSupport.class;

        final boolean isContextIncorrectType = !contextClass.isInstance(context);
        if (isContextIncorrectType) {
            throw new IllegalArgumentException("Context is not the correct type. This should never happen.");
        }

        final RetryContextSupport retryContextSupport = contextClass.cast(context);
        retryContextSupport.registerThrowable(throwable);
    }

    @Override
    public boolean canRetry(final RetryContext context) {
        final Throwable lastThrowable = context.getLastThrowable();

        final boolean noExceptionThrown = (lastThrowable == null);
        final boolean hasRetriesLeft = this.maxAttempts >= context.getRetryCount();

//        return hasRetriesLeft && (noExceptionThrown || isUnauthorizedHttpClientErrorException(lastThrowable));
        return hasRetriesLeft && (noExceptionThrown || ApplicationExceptions.HmrcUnauthorisedException.class.isInstance(lastThrowable));
    }

    private boolean isUnauthorizedHttpClientErrorException(final Throwable throwable) {
        final boolean isRetryableException = HTTP_CLIENT_ERROR_EXCEPTION.isInstance(throwable);

        if (isRetryableException) {
            final HttpClientErrorException httpClientErrorException = HTTP_CLIENT_ERROR_EXCEPTION.cast(throwable);
            return httpClientErrorException.getStatusCode().equals(UNAUTHORIZED);
        }

        return false;
    }

    @Override
    public void close(final RetryContext context) {
    }
}