package uk.gov.digital.ho.pttg.application.retry;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "hmrc")
@Setter
public class RetryProperties {

    private Retry retry;

    @Getter
    @Setter
    public static class Retry {
        private int attempts;
        private int delay;
        private int unauthorizedAttempts;
    }

    public int getAttempts() {
        return retry.getAttempts();
    }

    public int getDelay() {
        return retry.getDelay();
    }

    public int getUnauthorizedAttempts() {
        return retry.getUnauthorizedAttempts();
    }

}
