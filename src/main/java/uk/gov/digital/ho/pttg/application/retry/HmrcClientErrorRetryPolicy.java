package uk.gov.digital.ho.pttg.application.retry;

import com.google.common.collect.ImmutableMap;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.util.List;

public class HmrcClientErrorRetryPolicy extends ExceptionClassifierRetryPolicy {

    public HmrcClientErrorRetryPolicy(int hmrcUnauthorizedRetryAttempts, List<String> candidateNames, Individual individual) {
        setPolicyMap(ImmutableMap.of(
                ApplicationExceptions.HmrcForbiddenException.class, getHmrcForbiddenRetryPolicy(candidateNames, individual),
                ApplicationExceptions.HmrcUnauthorisedException.class, getHmrcUnauthorisedRetryPolicy(hmrcUnauthorizedRetryAttempts)
        ));
    }

    private RetryPolicy getHmrcForbiddenRetryPolicy(List<String> candidateNames, Individual individual) {
        return new NameMatchingRetryPolicy(candidateNames, individual);
    }

    private RetryPolicy getHmrcUnauthorisedRetryPolicy(int hmrcUnauthorizedRetryAttempts) {
        return new UnauthorizedHttpClientErrorExceptionRetryPolicy(hmrcUnauthorizedRetryAttempts);
    }

}
