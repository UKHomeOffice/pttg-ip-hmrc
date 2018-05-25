package uk.gov.digital.ho.pttg.application.retry;

import org.springframework.retry.RetryContext;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.util.List;

public class NameMatchingRetryPolicy extends ExceptionAwareRetryPolicy {

    private List<String> candidateNames;
    private Individual individual;

    public NameMatchingRetryPolicy(List<String> candidateNames, Individual individual) {
        super(ApplicationExceptions.HmrcForbiddenException.class);
        this.candidateNames = candidateNames;
        this.individual = individual;
    }

    @Override
    protected boolean checkAndConfigureRetry(RetryContext context) {
        int retryCount = context.getRetryCount();
        if(retryCount < candidateNames.size()) {
            String[] names = candidateNames.get(retryCount).split("\\s+");
            individual.setFirstName(names[0]);
            individual.setLastName(names[1]);
            return true;
        }
        else {
            return false;
        }
    }
}
