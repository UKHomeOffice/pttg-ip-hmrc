package uk.gov.digital.ho.pttg.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {HmrcClient.class, HmrcClientDefaultValuesSpringTest.TestConfig.class})
public class HmrcClientDefaultValuesSpringTest {

    @Autowired
    public HmrcClient hmrcClient;

    @Test
    public void autowiring_noPropertiesSet_payeEpochLocalDateMIN() {
        LocalDate payeDataEpoch = (LocalDate) ReflectionTestUtils.getField(hmrcClient, "payeDataEpoch");
        assertThat(payeDataEpoch)
                .isEqualTo(LocalDate.MIN);
    }

    @EnableConfigurationProperties
    @TestConfiguration
    static class TestConfig {
        @Bean
        public HmrcHateoasClient hmrcHateoasClient() {
            return new HmrcHateoasClient(null, null, null, null, null);
        }
    }
}