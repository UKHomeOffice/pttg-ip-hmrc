package uk.gov.digital.ho.pttg.application.retry;

import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.context.RetryContextSupport;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.util.List;

public class NameMatchingRetryPolicy implements RetryPolicy {

    private static final Class<ApplicationExceptions.HmrcForbiddenException> HANDLED_EXCEPTION = ApplicationExceptions.HmrcForbiddenException.class;
    private List<String> candidateNames;
    private Individual individual;
    private int retries;

    public NameMatchingRetryPolicy(List<String> candidateNames, Individual individual) {
        this.candidateNames = candidateNames;
        this.individual = individual;
    }

    @Override
    public boolean canRetry(RetryContext context) {
        if (!HANDLED_EXCEPTION.isInstance(context.getLastThrowable())) {
            return false;
        }
        if(retries < candidateNames.size()) {
            String[] names = candidateNames.get(retries++).split("\\s+");
            individual.setFirstName(names[0]);
            individual.setLastName(names[1]);
            return true;
        } else {
            return false;
        }
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
