package uk.gov.digital.ho.pttg.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.api.RequestData;
import uk.gov.digital.ho.pttg.application.retry.RetryTemplateBuilder;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_API_CALL_ATTEMPT;

@Component
@Slf4j
public class AuditClient {

    private final Clock clock;
    private final RestTemplate restTemplate;
    private final String auditEndpoint;
    private final RequestData requestData;
    private final ObjectMapper mapper;
    private final RetryTemplate retryTemplate;
    private final int maxCallAttempts;

    public AuditClient(Clock clock,
                       RestTemplate restTemplate,
                       RequestData requestData,
                       @Value("${pttg.audit.endpoint}") String auditEndpoint,
                       ObjectMapper mapper,
                       @Value("#{${audit.service.retry.attempts}}") int maxCallAttempts,
                       @Value("#{${audit.service.retry.delay}}") int retryDelay) {
        this.clock = clock;
        this.restTemplate = restTemplate;
        this.requestData = requestData;
        this.auditEndpoint = auditEndpoint;
        this.mapper = mapper;
        this.maxCallAttempts = maxCallAttempts;
        this.retryTemplate = new RetryTemplateBuilder(this.maxCallAttempts)
                .withBackOffPeriod(retryDelay)
                .retryHttpServerErrors()
                .build();
    }

    public void add(AuditEventType eventType, UUID eventId, AuditIndividualData auditData) {

        log.info("POST data for {} to audit service", eventId);

        try {
            AuditableData auditableData = generateAuditableData(eventType, eventId, auditData);
            dispatchAuditableData(auditableData);
            log.info("data POSTed to audit service");
        } catch (JsonProcessingException e) {
            log.error("Failed to create json representation of audit data");
        }
    }

    public void dispatchAuditableData(AuditableData auditableData) {
        try {
            retryTemplate.execute(context -> {
                log.info("Audit attempt {} of {}", context.getRetryCount() + 1, maxCallAttempts, value(EVENT, HMRC_API_CALL_ATTEMPT));
                return restTemplate.exchange(auditEndpoint, POST, toEntity(auditableData), Void.class);
            });
        } catch (HttpServerErrorException e) {
            log.error("Failed to audit {} after retries", auditableData.getEventType());
            throw e;
        }
    }

    private AuditableData generateAuditableData(AuditEventType eventType, UUID eventId, AuditIndividualData auditData) throws JsonProcessingException {
        return new AuditableData(eventId.toString(),
                LocalDateTime.now(clock),
                requestData.sessionId(),
                requestData.correlationId(),
                requestData.userId(),
                requestData.deploymentName(),
                requestData.deploymentNamespace(),
                eventType,
                mapper.writeValueAsString(auditData));
    }

    private HttpEntity<AuditableData> toEntity(AuditableData auditableData) {
        return new HttpEntity<>(auditableData, generateRestHeaders());
    }

    private HttpHeaders generateRestHeaders() {

        HttpHeaders headers = new HttpHeaders();

        headers.add(AUTHORIZATION, requestData.auditBasicAuth());
        headers.setContentType(APPLICATION_JSON);

        return headers;
    }
}
