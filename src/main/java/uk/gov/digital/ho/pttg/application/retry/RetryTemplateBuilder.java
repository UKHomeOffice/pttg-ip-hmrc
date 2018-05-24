package uk.gov.digital.ho.pttg.application.retry;

import org.apache.http.conn.HttpHostConnectException;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.CompositeRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonMap;

public class RetryTemplateBuilder {
    private final int maxRetryAttempts;
    private final List<RetryPolicy> retryPolicyList;
    private final List<RetryListener> retryListenerList;
    private FixedBackOffPolicy backOffPolicy;

    public RetryTemplateBuilder(final int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
        this.retryPolicyList = new ArrayList<>();
        this.retryListenerList = new ArrayList<>();
    }

    public RetryTemplateBuilder retryHttpServerErrors() {
        final SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(maxRetryAttempts, singletonMap(HttpServerErrorException.class, TRUE));
        retryPolicyList.add(simpleRetryPolicy);

        return this;
    }

    public RetryTemplateBuilder retryConnectionRefusedErrors() {
        final SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(maxRetryAttempts, singletonMap(HttpHostConnectException.class, TRUE), true);
        retryPolicyList.add(simpleRetryPolicy);

        return this;
    }

    public RetryTemplateBuilder withBackOffPeriod(final long retryDelayInMillis) {
        backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(retryDelayInMillis);

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
}