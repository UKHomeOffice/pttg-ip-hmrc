package uk.gov.digital.ho.pttg.application.retry;

import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.context.RetryContextSupport;

public abstract class ExceptionAwareRetryPolicy implements RetryPolicy {

    private final Class<? extends Exception> supportedException;

    private final int maxAttempts;

    protected ExceptionAwareRetryPolicy(Class<? extends Exception> supportedException, int maxAttempts) {
        this.supportedException = supportedException;
        this.maxAttempts = maxAttempts;
    }

    protected boolean supportsThrowable(Throwable throwable) {
        return throwable == null || supportedException.isInstance(throwable);
    }

    @Override
    public void registerThrowable(RetryContext context, Throwable throwable) {
        Class<RetryContextSupport> contextClass = RetryContextSupport.class;

        if (!contextClass.isInstance(context)) {
            throw new IllegalArgumentException("Context is not the correct type. This should never happen.");
        }

        contextClass.cast(context).registerThrowable(throwable);
    }

    @Override
    public RetryContext open(final RetryContext parent) {
        return new RetryContextSupport(parent);
    }

    @Override
    public void close(final RetryContext context) {
    }

    @Override
    public final boolean canRetry(RetryContext context) {

        if (!supportsThrowable(context.getLastThrowable())) {
            return false;
        }

        return maxAttempts >= context.getRetryCount();
    }
}
