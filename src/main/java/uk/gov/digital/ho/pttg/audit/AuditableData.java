package uk.gov.digital.ho.pttg.audit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
class AuditableData {

    @JsonProperty(value="eventId", required=true)
    private String eventId;

    @JsonProperty(value="timestamp", required=true)
    private LocalDateTime timestamp;

    @JsonProperty(value="sessionId", required=true)
    private String sessionId;

    @JsonProperty(value="correlationId", required=true)
    private String correlationId;

    @JsonProperty(value="userId", required=true)
    private String userId;

    @JsonProperty(value="deploymentName", required=true)
    private String deploymentName;

    @JsonProperty(value="deploymentNamespace", required=true)
    private String deploymentNamespace;

    @JsonProperty(value="eventType", required=true)
    private AuditEventType eventType;

    @JsonProperty(value="data", required=true)
    private String data;
}
