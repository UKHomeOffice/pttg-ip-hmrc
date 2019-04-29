package uk.gov.digital.ho.pttg.application;

import lombok.AllArgsConstructor;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.CompositeRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonMap;

@AllArgsConstructor
public class HmrcRetryTemplateFactory {

    private int retryAttempts;
    private int retryDelay;

    public RetryTemplate createInstance(int maxDurationInMs) {

        CompositeRetryPolicy compositeRetryPolicy = compositeRetryPolicy(
                simpleRetryPolicy(retryAttempts),
                timeoutRetryPolicy(maxDurationInMs));

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(compositeRetryPolicy);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy(retryDelay));

        return retryTemplate;
    }

    private FixedBackOffPolicy fixedBackOffPolicy(int delay) {
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(delay);
        return backOffPolicy;
    }

    private CompositeRetryPolicy compositeRetryPolicy(SimpleRetryPolicy simpleRetryPolicy, TimeoutRetryPolicy timeoutRetryPolicy) {
        CompositeRetryPolicy retryPolicy = new CompositeRetryPolicy();
        retryPolicy.setPolicies(new RetryPolicy[]{simpleRetryPolicy, timeoutRetryPolicy});

        return retryPolicy;
    }

    private SimpleRetryPolicy simpleRetryPolicy(int attempts) {
        return new SimpleRetryPolicy(
                attempts,
                singletonMap(HttpServerErrorException.class, TRUE));
    }

    private TimeoutRetryPolicy timeoutRetryPolicy(int maxDurationInMs) {
        TimeoutRetryPolicy timeoutRetryPolicy = new TimeoutRetryPolicy();
        timeoutRetryPolicy.setTimeout(maxDurationInMs);
        return timeoutRetryPolicy;
    }

}
