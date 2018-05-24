package uk.gov.digital.ho.pttg.application.retry;

import com.google.common.collect.ImmutableMap;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;

public class HmrcClientErrorRetryPolicy extends ExceptionClassifierRetryPolicy {

    public HmrcClientErrorRetryPolicy(int hmrcUnauthorizedRetryAttempts) {
        setPolicyMap(ImmutableMap.of(
                ApplicationExceptions.HmrcForbiddenException.class, getHmrcForbiddenRetryPolicy(),
                ApplicationExceptions.HmrcUnauthorisedException.class, getHmrcUnauthorisedRetryPolicy(hmrcUnauthorizedRetryAttempts)
        ));
    }

    private RetryPolicy getHmrcForbiddenRetryPolicy() {
        return new NameMatchingRetryPolicy();
    }

    private RetryPolicy getHmrcUnauthorisedRetryPolicy(int hmrcUnauthorizedRetryAttempts) {
        return new UnauthorizedHttpClientErrorExceptionRetryPolicy(hmrcUnauthorizedRetryAttempts);
    }

}
