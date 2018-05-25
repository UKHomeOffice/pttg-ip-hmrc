package uk.gov.digital.ho.pttg.application.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;

@Slf4j
public class UnauthorizedHttpClientErrorExceptionRetryPolicy extends ExceptionAwareRetryPolicy {

    private final int maxAttempts;

    public UnauthorizedHttpClientErrorExceptionRetryPolicy(final int maxAttempts) {
        super(ApplicationExceptions.HmrcUnauthorisedException.class);
        this.maxAttempts = maxAttempts;
    }

    @Override
    protected boolean checkAndConfigureRetry(final RetryContext context) {

        final boolean hasRetriesLeft = this.maxAttempts >= context.getRetryCount();

        return hasRetriesLeft;
    }
}