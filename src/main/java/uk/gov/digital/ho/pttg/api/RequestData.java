package uk.gov.digital.ho.pttg.api;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author Home Office Digital
 */
@Component
public class RequestData {

    public static final String SESSION_ID_HEADER = "x-session-id";
    public static final String CORRELATION_ID_HEADER = "x-correlation-id";
    public static final String USER_ID_HEADER = "x-auth-userid";

    @Value("${auditing.deployment.name}") private String deploymentName;
    @Value("${auditing.deployment.namespace}") private String deploymentNamespace;

    public String deploymentName() {
        return deploymentName;
    }

    public String deploymentNamespace() {
        return deploymentNamespace;
    }

    public String sessionId() {
        String sessionId = MDC.get(SESSION_ID_HEADER);
        return StringUtils.isNotBlank(sessionId) ? sessionId : "unknown";
    }

    public String correlationId() {
        return MDC.get(CORRELATION_ID_HEADER);
    }

    public String userId() {
        return MDC.get(USER_ID_HEADER);
    }
}
