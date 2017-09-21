package uk.gov.digital.ho.pttg.audit;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit")
@Access(AccessType.FIELD)
@NoArgsConstructor
@EqualsAndHashCode(of = "uuid")
public class AuditEntry {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false)
    @Getter
    private String uuid;

    @Column(name = "timestamp", nullable = false)
    @Getter
    private LocalDateTime timestamp;

    @Column(name = "session_id", nullable = false)
    @Getter
    private String sessionId;

    @Column(name = "correlation_id", nullable = false)
    @Getter
    private String correlationId;

    @Column(name = "user_id", nullable = false)
    @Getter
    private String userId;

    @Column(name = "deployment", nullable = false)
    @Getter
    private String deployment;

    @Column(name = "namespace", nullable = false)
    @Getter
    private String namespace;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @Getter
    private AuditEventType type;

    @Column(name = "detail", nullable = false)
    @Getter
    private String detail;

    public AuditEntry(String uuid, LocalDateTime timestamp, String sessionId, String correlationId,
                      String userId, String deployment, String namespace, AuditEventType type, String detail) {
        this.uuid = uuid;
        this.timestamp = timestamp;
        this.sessionId = sessionId;
        this.correlationId = correlationId;
        this.userId = userId;
        this.deployment = deployment;
        this.namespace = namespace;
        this.type = type;
        this.detail = detail;
    }
}
