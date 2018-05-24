package uk.gov.digital.ho.pttg.application.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.context.RetryContextSupport;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;

@Slf4j
public class UnauthorizedHttpClientErrorExceptionRetryPolicy implements RetryPolicy {

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

        return hasRetriesLeft && (noExceptionThrown || ApplicationExceptions.HmrcUnauthorisedException.class.isInstance(lastThrowable));
    }

    @Override
    public void close(final RetryContext context) {
    }
}