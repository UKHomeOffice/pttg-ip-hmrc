package uk.gov.digital.ho.pttg.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.api.RequestData;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@Slf4j
public class AuditClient {

    private final Clock clock;
    private final RestTemplate restTemplate;
    private final String auditEndpoint;
    private final RequestData requestData;

    public AuditClient(Clock clock,
                       RestTemplate restTemplate,
                       RequestData requestData,
                       @Value("${pttg.audit.endpoint}") String auditEndpoint) {
        this.clock = clock;
        this.restTemplate = restTemplate;
        this.requestData = requestData;
        this.auditEndpoint = auditEndpoint;
    }

    @Retryable(
            value = { RestClientException.class },
            maxAttemptsExpression = "#{${audit.service.retry.attempts}}",
            backoff = @Backoff(delayExpression = "#{${audit.service.retry.delay}}"))
    public void add(AuditEventType eventType, UUID eventId, AuditIndividualData auditData) {
        log.info("POST data for {} to audit service");

        AuditableData auditableData = generateAuditableData(eventType, eventId, auditData);

        restTemplate.exchange(auditEndpoint, POST, toEntity(auditableData), Void.class);

        log.info("data POSTed to audit service");
    }

    @Recover
    void addRetryFailureRecovery(RestClientException e, AuditEventType eventType) {
        log.error("Failed to audit {} after retries", eventType);
    }

    private AuditableData generateAuditableData(AuditEventType eventType, UUID eventId, AuditIndividualData auditData) {
        return new AuditableData(eventId.toString(),
                                    LocalDateTime.now(clock),
                                    requestData.sessionId(),
                                    requestData.correlationId(),
                                    requestData.userId(),
                                    requestData.deploymentName(),
                                    requestData.deploymentNamespace(),
                                    eventType,
                                    auditData);
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
