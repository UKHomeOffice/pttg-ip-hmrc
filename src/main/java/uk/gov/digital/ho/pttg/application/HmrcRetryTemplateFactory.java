package uk.gov.digital.ho.pttg.application;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.CompositeRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Boolean.TRUE;

@AllArgsConstructor
@Slf4j
public class HmrcRetryTemplateFactory {

    private int retryAttempts;
    private int retryDelay;

    public RetryTemplate createInstance() {

        CompositeRetryPolicy compositeRetryPolicy = compositeRetryPolicy(
                simpleRetryPolicy(retryAttempts)
        );

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

    private CompositeRetryPolicy compositeRetryPolicy(RetryPolicy... retryPolicies) {
        CompositeRetryPolicy compositeRetryPolicy = new CompositeRetryPolicy();
        compositeRetryPolicy.setPolicies(retryPolicies);
        return compositeRetryPolicy;
    }

    private SimpleRetryPolicy simpleRetryPolicy(int attempts) {

        log.debug("Retry policy has {} attempts", attempts);

        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(HttpServerErrorException.class, TRUE);

        return new SimpleRetryPolicy(
                attempts,
                retryableExceptions);
    }

}
