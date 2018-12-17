package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static java.lang.Thread.activeCount;
import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_SERVICE_GENERATED_CORRELATION_ID;

@Slf4j
public class RequestHeaderData implements HandlerInterceptor {

    public static final String SESSION_ID_HEADER = "x-session-id";
    public static final String CORRELATION_ID_HEADER = "x-correlation-id";
    public static final String USER_ID_HEADER = "x-auth-userid";
    private static final String REQUEST_START_TIMESTAMP = "request-timestamp";
    public static final String REQUEST_DURATION_MS = "request_duration_ms";
    public static final String THREAD_COUNT = "thread_count";

    @Value("${auditing.deployment.name}") private String deploymentName;
    @Value("${auditing.deployment.namespace}") private String deploymentNamespace;
    @Value("${hmrc.access.service.auth}") private String hmrcAccessBasicAuth;
    @Value("${audit.service.auth}") private String auditBasicAuth;
    @Value("${hmrc.api.version}") private String hmrcApiVersion;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        MDC.clear();
        initialiseSessionId(request);
        initialiseCorrelationId(request);
        initialiseUserName(request);
        inititaliseRequestStart();
        initialiseThreadCount();
        MDC.put("userHost", request.getRemoteHost());
        MDC.put("thread_id", String.valueOf(Thread.currentThread().getId()));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        response.setHeader(SESSION_ID_HEADER, sessionId());
        response.setHeader(USER_ID_HEADER, userId());
        response.setHeader(CORRELATION_ID_HEADER, correlationId());
        MDC.clear();
    }

    private void initialiseSessionId(HttpServletRequest request) {
        String sessionId = request.getHeader(SESSION_ID_HEADER);
        if(StringUtils.isBlank(sessionId)) {
            sessionId = "unknown";
        }
        MDC.put(SESSION_ID_HEADER, sessionId);
    }

    private void initialiseCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if(StringUtils.isBlank(correlationId)) {
            correlationId = UUID.randomUUID().toString();
            MDC.put(CORRELATION_ID_HEADER, correlationId);
            log.info("Generated new correlation id as not passed in request header", value(EVENT, HMRC_SERVICE_GENERATED_CORRELATION_ID));
        } else {
            MDC.put(CORRELATION_ID_HEADER, correlationId);
        }
    }

    private void initialiseUserName(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        if(StringUtils.isBlank(userId)) {
            userId = "unknown";
        }
        MDC.put(USER_ID_HEADER, userId);
    }

    private void inititaliseRequestStart() {
        long requestStartTimeStamp = Instant.now().toEpochMilli();
        MDC.put(REQUEST_START_TIMESTAMP, Long.toString(requestStartTimeStamp));

    }

    long calculateRequestDuration() {
        long timeStamp = Instant.now().toEpochMilli();
        return timeStamp - Long.parseLong(MDC.get(REQUEST_START_TIMESTAMP));
    }

    public String deploymentName() {
        return deploymentName;
    }

    private void initialiseThreadCount() {
        MDC.put(THREAD_COUNT, Integer.toString(activeCount()));
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
