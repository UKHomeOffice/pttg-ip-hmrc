package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.InsufficientTimeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static java.lang.Thread.activeCount;
import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@Slf4j
public class RequestHeaderData implements HandlerInterceptor {

    private static final String REQUEST_START_TIMESTAMP = "request-timestamp";
    private static final String THREAD_COUNT = "thread_count";
    private static final String MAX_DURATION = "max_duration";

    static final String REQUEST_DURATION_MS = "request_duration_ms";
    static final String POOL_SIZE = "pool_size";

    public static final String MAX_DURATION_MS_HEADER = "x-max-duration";
    public static final String SESSION_ID_HEADER = "x-session-id";
    public static final String CORRELATION_ID_HEADER = "x-correlation-id";
    public static final String USER_ID_HEADER = "x-auth-userid";
    public static final String RETRY_COUNT_HEADER = "x-retry-count";

    static final long EXPECTED_REMAINING_TIME_TO_COMPLETE = 0;

    public static final String SMOKE_TESTS_USER_ID = "smoke-tests";

    @Value("${auditing.deployment.name}") private String deploymentName;
    @Value("${auditing.deployment.namespace}") private String deploymentNamespace;
    @Value("${hmrc.access.service.auth}") private String hmrcAccessBasicAuth;
    @Value("${audit.service.auth}") private String auditBasicAuth;
    @Value("${hmrc.api.version}") private String hmrcApiVersion;
    @Value("${service.max.duration:60000}") private int defaultMaxDuration;
    private Clock clock;

    public RequestHeaderData() {
        this(Clock.systemUTC());
    }

    public RequestHeaderData(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        MDC.clear();

        initialiseSessionId(request);
        initialiseCorrelationId(request);
        initialiseUserName(request);
        initialiseMaxDuration(request);
        initialiseRetryCount(request);

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

        if (StringUtils.isBlank(sessionId)) {
            sessionId = "unknown";
        }

        MDC.put(SESSION_ID_HEADER, sessionId);
    }

    private void initialiseCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (StringUtils.isBlank(correlationId)) {
            correlationId = UUID.randomUUID().toString();
            MDC.put(CORRELATION_ID_HEADER, correlationId);
            log.info("Generated new correlation id as not passed in request header", value(EVENT, HMRC_SERVICE_GENERATED_CORRELATION_ID));
        } else {
            MDC.put(CORRELATION_ID_HEADER, correlationId);
        }
    }

    private void initialiseUserName(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        if (StringUtils.isBlank(userId)) {
            userId = "unknown";
        }
        MDC.put(USER_ID_HEADER, userId);
    }

    private void initialiseMaxDuration(HttpServletRequest request) {

        int maxDuration = maxDuration(request.getHeader(MAX_DURATION_MS_HEADER));

        log.info("Hmrc service response required in {} ms", maxDuration, value(EVENT, HMRC_SERVICE_MAX_RESPONSE_TIME));

        MDC.put(MAX_DURATION, Integer.toString(maxDuration));
    }

    private void initialiseRetryCount(HttpServletRequest request) {
        MDC.put(RETRY_COUNT_HEADER, request.getHeader(RETRY_COUNT_HEADER));
    }

    private int maxDuration(String header) {
        log.info("In maxDuration setter with header {}", header);

        if (StringUtils.isBlank(header)) {
            log.info("In maxDuration setter no header so using default {}", defaultMaxDuration);

            return defaultMaxDuration;
        }

        try {
            return Integer.parseInt(header);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Cannot parse %s header (%s) into Integer", MAX_DURATION_MS_HEADER, header), e);
        }
    }

    private void inititaliseRequestStart() {
        MDC.put(REQUEST_START_TIMESTAMP, Long.toString(timestamp()));
    }

    long calculateRequestDuration() {
        return timestamp() - requestStartTimestamp();
    }

    private long timestamp() {
        return Instant.now(clock).toEpochMilli();
    }

    private long requestStartTimestamp() {
        return Long.parseLong(MDC.get(REQUEST_START_TIMESTAMP));
    }

    public String deploymentName() {
        return deploymentName;
    }

    private void initialiseThreadCount() {
        MDC.put(THREAD_COUNT, Integer.toString(activeCount()));
    }

    Integer poolSize() {
        return new ThreadPoolTaskExecutor().getPoolSize();
    }

    public String deploymentNamespace() {
        return deploymentNamespace;
    }

    public String hmrcApiVersion() {
        return hmrcApiVersion;
    }

    public String hmrcBasicAuth() {
        return String.format("Basic %s", Base64.getEncoder().encodeToString(hmrcAccessBasicAuth.getBytes(StandardCharsets.UTF_8)));
    }

    public String auditBasicAuth() {
        return String.format("Basic %s", Base64.getEncoder().encodeToString(auditBasicAuth.getBytes(StandardCharsets.UTF_8)));
    }

    public String sessionId() {
        return MDC.get(SESSION_ID_HEADER);
    }

    public String correlationId() {
        return MDC.get(CORRELATION_ID_HEADER);
    }

    public String userId() {
        return MDC.get(USER_ID_HEADER);
    }

    int serviceMaxDuration() {
        return Integer.parseInt(MDC.get(MAX_DURATION));
    }

    long responseRequiredBy() {
        return requestStartTimestamp() + serviceMaxDuration();
    }

    public Integer retryCount() {
        String retryCountString = MDC.get(RETRY_COUNT_HEADER);
        int retryCount = -1;
        try {
            retryCount = Integer.parseInt(retryCountString);
        } catch(NumberFormatException ne) {
            // noop
        }
        return retryCount;
    }

    public void abortIfTakingTooLong() {
        abortIfLikelyToTakeLongerThan(EXPECTED_REMAINING_TIME_TO_COMPLETE);
    }

    void abortIfLikelyToTakeLongerThan(long minTimeToRespond) {
        long remainingTime = responseRequiredBy() - timestamp();

        if (remainingTime < minTimeToRespond) {
            log.info("Insufficient time to complete the Response - {} ms remaining and expected duration is {}", remainingTime, minTimeToRespond, value(EVENT, HMRC_INSUFFICIENT_TIME_TO_COMPLETE));
            throw new InsufficientTimeException("Insufficient time to complete the Response");
        }
    }

    public boolean isASmokeTest() {
        return userId().equals(SMOKE_TESTS_USER_ID);
    }
}
