package uk.gov.digital.ho.pttg.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "timeouts")
public class TimeoutProperties {

    private Audit audit;
    private HmrcApi hmrcApi;
    private HmrcAccessCode hmrcAccessCode;

    public static class Audit {
        private int readMs;
        private int connectMs;

        public int getReadMs() {
            return readMs;
        }

        public void setReadMs(int readMs) {
            this.readMs = readMs;
        }

        public int getConnectMs() {
            return connectMs;
        }

        public void setConnectMs(int connectMs) {
            this.connectMs = connectMs;
        }
    }

    public static class HmrcApi {
        private int readMs;
        private int connectMs;

        public int getReadMs() {
            return readMs;
        }

        public void setReadMs(int readMs) {
            this.readMs = readMs;
        }

        public int getConnectMs() {
            return connectMs;
        }

        public void setConnectMs(int connectMs) {
            this.connectMs = connectMs;
        }
    }

    public static class HmrcAccessCode {
        private int readMs;
        private int connectMs;

        public int getReadMs() {
            return readMs;
        }

        public void setReadMs(int readMs) {
            this.readMs = readMs;
        }

        public int getConnectMs() {
            return connectMs;
        }

        public void setConnectMs(int connectMs) {
            this.connectMs = connectMs;
        }
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    public HmrcApi getHmrcApi() {
        return hmrcApi;
    }

    public void setHmrcApi(HmrcApi hmrcApi) {
        this.hmrcApi = hmrcApi;
    }

    public HmrcAccessCode getHmrcAccessCode() {
        return hmrcAccessCode;
    }

    public void setHmrcAccessCode(HmrcAccessCode hmrcAccessCode) {
        this.hmrcAccessCode = hmrcAccessCode;
    }
}
