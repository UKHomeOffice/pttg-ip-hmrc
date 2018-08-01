package uk.gov.digital.ho.pttg.application.retry;

import org.junit.Test;
import org.springframework.retry.support.RetryTemplate;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcUnauthorisedException;

import static org.assertj.core.api.Assertions.assertThat;


public class RetryTemplateBuilderTest {
    @Test
    public void shouldAddRetryPolicyOnCallToRetryHmrcUnauthorisedException() {
        RetryTemplate retryTemplate = new RetryTemplateBuilder(5)
                .withBackOffPeriod(1)
                .retryHmrcUnauthorisedException()
                .build();

        TestHelper testHelper = new TestHelper();
        try {
            retryTemplate.execute(context -> {
                testHelper.call();
                    throw new HmrcUnauthorisedException("foo");
            });
        } catch (HmrcUnauthorisedException e) {
            // Ignore expected exception
        }

        assertThat(testHelper.getCount()).isGreaterThan(1);
    }

    static private class TestHelper {
        private int count = 0;
        public void call() {
            count++;
        }

        public int getCount() {
            return count;
        }
    }

}