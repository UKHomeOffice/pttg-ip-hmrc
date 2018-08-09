package uk.gov.digital.ho.pttg.api;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Component
public class RequestHeaderData implements HandlerInterceptor {

    public static final String SESSION_ID_HEADER = "x-session-id";
    public static final String CORRELATION_ID_HEADER = "x-correlation-id";
    public static final String USER_ID_HEADER = "x-auth-userid";

    @Value("${auditing.deployment.name}") private String deploymentName;
    @Value("${auditing.deployment.namespace}") private String deploymentNamespace;
    @Value("${hmrc.access.service.auth}") private String hmrcAccessBasicAuth;
    @Value("${audit.service.auth}") private String auditBasicAuth;
    @Value("${hmrc.api.version}") private String hmrcApiVersion;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        MDC.clear();
        MDC.put(SESSION_ID_HEADER, initialiseSessionId(request));
        MDC.put(CORRELATION_ID_HEADER, initialiseCorrelationId(request));
        MDC.put(USER_ID_HEADER, initialiseUserName(request));
        MDC.put("userHost", request.getRemoteHost());

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        MDC.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }

    private String initialiseSessionId(HttpServletRequest request) {
        String sessionId = request.getHeader(SESSION_ID_HEADER);
        return StringUtils.isNotBlank(sessionId) ? sessionId : "unknown";
    }

    private String initialiseCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        return StringUtils.isNotBlank(correlationId) ? correlationId : UUID.randomUUID().toString();
    }

    private String initialiseUserName(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        return StringUtils.isNotBlank(userId) ? userId : "anonymous";
    }

    public String deploymentName() {
        return deploymentName;
    }

    public String deploymentNamespace() {
        return deploymentNamespace;
    }

    public String hmrcApiVersion() {
        return hmrcApiVersion;
    }

    public String hmrcBasicAuth() { return String.format("Basic %s", Base64.getEncoder().encodeToString(hmrcAccessBasicAuth.getBytes(StandardCharsets.UTF_8))); }

    public String auditBasicAuth() { return String.format("Basic %s", Base64.getEncoder().encodeToString(auditBasicAuth.getBytes(StandardCharsets.UTF_8))); }

    public String sessionId() {
        return MDC.get(SESSION_ID_HEADER);
    }

    public String correlationId() {
        return MDC.get(CORRELATION_ID_HEADER);
    }

    public String userId() {
        return MDC.get(USER_ID_HEADER);
    }
}
