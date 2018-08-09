package uk.gov.digital.ho.pttg.application.retry;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.http.conn.HttpHostConnectException;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.backoff.NoBackOffPolicy;
import org.springframework.retry.backoff.StatelessBackOffPolicy;
import org.springframework.retry.policy.CompositeRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonMap;

public class RetryTemplateBuilder {

    @Getter
    @Accessors(fluent = true)
    private final int maxAttempts;

    private final List<RetryPolicy> retryPolicyList;
    private final List<RetryListener> retryListenerList;
    private StatelessBackOffPolicy backOffPolicy;

    public RetryTemplateBuilder(final int maxAttempts) {
        this.maxAttempts = maxAttempts;
        this.retryPolicyList = new ArrayList<>();
        this.retryListenerList = new ArrayList<>();

        backOffPolicy = new NoBackOffPolicy();
    }

    public RetryTemplateBuilder retryHttpServerErrors() {
        final SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(maxAttempts, singletonMap(HttpServerErrorException.class, TRUE));
        retryPolicyList.add(simpleRetryPolicy);

        return this;
    }

    public RetryTemplateBuilder retryConnectionRefusedErrors() {
        final SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(maxAttempts, singletonMap(HttpHostConnectException.class, TRUE), true);
        retryPolicyList.add(simpleRetryPolicy);

        return this;
    }

    public RetryTemplateBuilder withBackOffPeriod(final long retryDelayInMillis) {
        FixedBackOffPolicy fixedBackoffPolicy = new FixedBackOffPolicy();
        fixedBackoffPolicy.setBackOffPeriod(retryDelayInMillis);
        this.backOffPolicy = fixedBackoffPolicy;

        return this;
    }

    public RetryTemplateBuilder addListener(final RetryListener retryListener) {
        retryListenerList.add(retryListener);
        return this;
    }

    public RetryTemplate build() {
        final RetryTemplate retryTemplate = new RetryTemplate();

        retryTemplate.setRetryPolicy(getCombinedRetryPolicy());
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setListeners(getListeners());

        return retryTemplate;
    }

    private RetryListener[] getListeners() {
        return retryListenerList.toArray(new RetryListener[0]);
    }

    private CompositeRetryPolicy getCombinedRetryPolicy() {
        final CompositeRetryPolicy compositeRetryPolicy = new CompositeRetryPolicy();
        compositeRetryPolicy.setOptimistic(true);

        final RetryPolicy[] retryPolicies = getRetryPolicies();
        compositeRetryPolicy.setPolicies(retryPolicies);

        return compositeRetryPolicy;
    }

    private RetryPolicy[] getRetryPolicies() {
        return this.retryPolicyList.toArray(new RetryPolicy[0]);
    }

    public RetryTemplateBuilder retryHmrcUnauthorisedException() {
        final SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts, singletonMap(ApplicationExceptions.HmrcUnauthorisedException.class, TRUE));
        retryPolicyList.add(retryPolicy);
        return this;
    }
}