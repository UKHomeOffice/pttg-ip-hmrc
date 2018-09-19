package uk.gov.digital.ho.pttg.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;
import uk.gov.digital.ho.pttg.application.retry.RetryTemplateBuilder;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@Component
@Slf4j
public class AuditClient {

    private final Clock clock;
    private final RestTemplate restTemplate;
    private final String auditEndpoint;
    private final RequestHeaderData requestHeaderData;
    private final ObjectMapper mapper;
    private final RetryTemplate retryTemplate;
    private final int maxCallAttempts;

    public AuditClient(Clock clock,
                       @Qualifier("auditRestTemplate") RestTemplate restTemplate,
                       RequestHeaderData requestHeaderData,
                       @Value("${pttg.audit.endpoint}") String auditEndpoint,
                       ObjectMapper mapper,
                       @Value("#{${audit.service.retry.attempts}}") int maxCallAttempts,
                       @Value("#{${audit.service.retry.delay}}") int retryDelay) {
        this.clock = clock;
        this.restTemplate = restTemplate;
        this.requestHeaderData = requestHeaderData;
        this.auditEndpoint = auditEndpoint;
        this.mapper = mapper;
        this.maxCallAttempts = maxCallAttempts;
        this.retryTemplate = new RetryTemplateBuilder(this.maxCallAttempts)
                .withBackOffPeriod(retryDelay)
                .retryHttpServerErrors()
                .build();
    }

    public void add(AuditEventType eventType, UUID eventId, AuditIndividualData auditData) {

        log.debug("POST data for {} to audit service", eventId);

        try {
            AuditableData auditableData = generateAuditableData(eventType, eventId, auditData);
            dispatchAuditableData(auditableData);
            log.debug("data POSTed to audit service");
        } catch (JsonProcessingException e) {
            log.error("Failed to create json representation of audit data", value(EVENT, HMRC_AUDIT_FAILURE));
        }
    }

    void dispatchAuditableData(AuditableData auditableData) {
        try {
            retryTemplate.execute(context -> {
                if (context.getRetryCount() > 0) {
                    log.info("Retrying audit attempt {} of {}", context.getRetryCount(), maxCallAttempts - 1, value(EVENT, auditableData.getEventType()));
                }
                return restTemplate.exchange(auditEndpoint, POST, toEntity(auditableData), Void.class);
            });
        } catch (Exception e) {
            log.error("Failed to audit {} after retries", auditableData.getEventType(), value(EVENT, HMRC_AUDIT_FAILURE));
        }
    }

    private AuditableData generateAuditableData(AuditEventType eventType, UUID eventId, AuditIndividualData auditData) throws JsonProcessingException {
        return new AuditableData(eventId.toString(),
                LocalDateTime.now(clock),
                requestHeaderData.sessionId(),
                requestHeaderData.correlationId(),
                requestHeaderData.userId(),
                requestHeaderData.deploymentName(),
                requestHeaderData.deploymentNamespace(),
                eventType,
                mapper.writeValueAsString(auditData));
    }

    private HttpEntity<AuditableData> toEntity(AuditableData auditableData) {
        return new HttpEntity<>(auditableData, generateRestHeaders());
    }

    private HttpHeaders generateRestHeaders() {

        HttpHeaders headers = new HttpHeaders();

        headers.add(AUTHORIZATION, requestHeaderData.auditBasicAuth());
        headers.setContentType(APPLICATION_JSON);
        headers.add(RequestHeaderData.SESSION_ID_HEADER, requestHeaderData.sessionId());
        headers.add(RequestHeaderData.CORRELATION_ID_HEADER, requestHeaderData.correlationId());
        headers.add(RequestHeaderData.USER_ID_HEADER, requestHeaderData.userId());

        return headers;
    }
}
