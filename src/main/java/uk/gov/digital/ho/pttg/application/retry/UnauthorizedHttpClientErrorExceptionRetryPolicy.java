package uk.gov.digital.ho.pttg.application.retry;

import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcUnauthorisedException;

public class UnauthorizedHttpClientErrorExceptionRetryPolicy extends ExceptionAwareRetryPolicy {

    public UnauthorizedHttpClientErrorExceptionRetryPolicy(int maxAttempts) {
        super(HmrcUnauthorisedException.class, maxAttempts);
    }
}