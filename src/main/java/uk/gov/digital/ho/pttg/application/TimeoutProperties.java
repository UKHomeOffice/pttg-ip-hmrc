package uk.gov.digital.ho.pttg.application;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "timeouts")
@NoArgsConstructor
@Getter
@Setter
public class TimeoutProperties {

    private Audit audit;
    private HmrcApi hmrcApi;
    private HmrcAccessCode hmrcAccessCode;

    public static class Audit extends TimeoutPropertiesTemplate {}
    public static class HmrcApi extends TimeoutPropertiesTemplate {}
    public static class HmrcAccessCode extends TimeoutPropertiesTemplate {}


    @NoArgsConstructor
    @Getter
    @Setter
    private static class TimeoutPropertiesTemplate {
        private int readMs;
        private int connectMs;
    }
}