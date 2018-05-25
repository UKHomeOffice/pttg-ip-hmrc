package uk.gov.digital.ho.pttg.application.retry;

import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.context.RetryContextSupport;

public abstract class ExceptionAwareRetryPolicy implements RetryPolicy {

    private Class<? extends Exception> supportedException;

    protected ExceptionAwareRetryPolicy(Class<? extends Exception> supportedException) {
        this.supportedException = supportedException;
    }

    protected boolean supportsThrowable(Throwable throwable) {
        return throwable == null || supportedException.isInstance(throwable);
    }

    @Override
    public void registerThrowable(RetryContext context, Throwable throwable) {
        Class<RetryContextSupport> contextClass = RetryContextSupport.class;

        final boolean isContextIncorrectType = !contextClass.isInstance(context);
        if (isContextIncorrectType) {
            throw new IllegalArgumentException("Context is not the correct type. This should never happen.");
        }

        final RetryContextSupport retryContextSupport = contextClass.cast(context);
        retryContextSupport.registerThrowable(throwable);
    }

    @Override
    public RetryContext open(final RetryContext parent) {
        return new RetryContextSupport(parent);
    }

    @Override
    public void close(final RetryContext context) {
    }

    @Override
    public boolean canRetry(RetryContext context) {
        if (!supportsThrowable(context.getLastThrowable())) {
            return false;
        }
        return checkAndConfigureRetry(context);
    }

    protected abstract boolean checkAndConfigureRetry(RetryContext context);
}
