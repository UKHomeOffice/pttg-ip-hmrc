package uk.gov.digital.ho.pttg.application;

import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.context.RetryContextSupport;

public class NameMatchingRetryPolicy implements RetryPolicy {

    private static final Class<ApplicationExceptions.HmrcForbiddenException> HANDLED_EXCEPTION = ApplicationExceptions.HmrcForbiddenException.class;

    private int retries = 2;

    @Override
    public boolean canRetry(RetryContext context) {
        if (!HANDLED_EXCEPTION.isInstance(context.getLastThrowable())) {
            return false;
        }
        return context.getRetryCount() < retries;
    }

    @Override
    public RetryContext open(RetryContext parent) {
        return new RetryContextSupport(parent);
    }

    @Override
    public void close(RetryContext context) {

    }

    @Override
    public void registerThrowable(RetryContext context, Throwable throwable) {

    }
}
