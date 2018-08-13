package uk.gov.digital.ho.pttg.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TimeoutProperties.class, TimeoutPropertiesTest.TestConfig.class})
@TestPropertySource(properties = {
        "timeouts.audit.read-ms=1000",
        "timeouts.audit.connect-ms=2000",
        "timeouts.hmrc-api.read-ms=3000",
        "timeouts.hmrc-api.connect-ms=4000",
        "timeouts.hmrc-access-code.read-ms=5000",
        "timeouts.hmrc-access-code.connect-ms=6000"
})
public class TimeoutPropertiesTest {

    @TestConfiguration
    @EnableConfigurationProperties
    public static class TestConfig {}

    @Autowired
    private TimeoutProperties timeoutProperties;

    @Test
    public void shouldLoadRestTemplateTimeouts() {
        assertThat(timeoutProperties.getAudit().getReadMs()).isEqualTo(1000);
        assertThat(timeoutProperties.getAudit().getConnectMs()).isEqualTo(2000);
        assertThat(timeoutProperties.getHmrcApi().getReadMs()).isEqualTo(3000);
        assertThat(timeoutProperties.getHmrcApi().getConnectMs()).isEqualTo(4000);
        assertThat(timeoutProperties.getHmrcAccessCode().getReadMs()).isEqualTo(5000);
        assertThat(timeoutProperties.getHmrcAccessCode().getConnectMs()).isEqualTo(6000);
    }

}

