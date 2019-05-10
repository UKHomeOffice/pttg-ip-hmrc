package uk.gov.digital.ho.pttg.application;

import org.junit.Before;
import org.junit.Test;
import org.springframework.classify.BinaryExceptionClassifier;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.CompositeRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

public class HmrcRetryTemplateFactoryTest {

    private RetryTemplate retryTemplate;

    @Before
    public void setup() {
        Clock testClock =  Clock.fixed(Instant.ofEpochMilli(222), ZoneId.of("Z"));

        HmrcRetryTemplateFactory factory = new HmrcRetryTemplateFactory(testClock, 123, 456);
        retryTemplate = factory.createInstance(999);
    }

    @Test
    public void createInstance_anyResponseRequiredBy_shouldProduceHmrcSpecificRetryPolicy() {

        CompositeRetryPolicy compositeRetryPolicy = (CompositeRetryPolicy) ReflectionTestUtils.getField(retryTemplate, "retryPolicy");
        assertThat(compositeRetryPolicy).isNotNull();

        RetryPolicy[] policies = (RetryPolicy[]) ReflectionTestUtils.getField(compositeRetryPolicy, "policies");
        assertThat(policies).isNotNull();

        assertThat(policies.length).isEqualTo(2);

        assertThat(policies[0]).isInstanceOf(SimpleRetryPolicy.class);
        assertThat(((SimpleRetryPolicy)policies[0]).getMaxAttempts()).isEqualTo(123);

        BinaryExceptionClassifier binaryExceptionClassifier = (BinaryExceptionClassifier) ReflectionTestUtils.getField(policies[0], "retryableClassifier");
        assertThat(binaryExceptionClassifier).isNotNull();

        assertThat(binaryExceptionClassifier.classify(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))).isTrue();
        assertThat(binaryExceptionClassifier.classify(new ApplicationExceptions.InsuffienctTimeException("Insufficient time to complete the Response"))).isTrue();
        assertThat(binaryExceptionClassifier.classify(new HttpClientErrorException(HttpStatus.BAD_REQUEST))).isFalse();

        assertThat(policies[1]).isInstanceOf(TimeoutRetryPolicy.class);
        assertThat(((TimeoutRetryPolicy)policies[1]).getTimeout()).isEqualTo(777);
    }

    @Test
    public void createInstance_anyResponseRequiredBy_shouldProduceHmrcSpecificBackoffPolicy() {

        FixedBackOffPolicy backOffPolicy = (FixedBackOffPolicy) ReflectionTestUtils.getField(retryTemplate, "backOffPolicy");
        assertThat(backOffPolicy).isNotNull();
        assertThat(backOffPolicy.getBackOffPeriod()).isEqualTo(456);
    }
}
