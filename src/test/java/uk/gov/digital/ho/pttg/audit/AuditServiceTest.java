package uk.gov.digital.ho.pttg.audit;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.api.RequestData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.AuditDataException;

@RunWith(MockitoJUnitRunner.class)
public class AuditServiceTest {

    @Mock private ObjectMapper mockMapper;
    @Mock private RequestData mockRequestData;
    @Mock private AuditEntryJpaRepository mockRepository;

    @Captor private ArgumentCaptor<AuditEntry> captorAuditEntry;

    @InjectMocks private AuditService auditService;


    private AuditEventType someAuditEventType;
    private UUID someUUID;
    private Map<String, Object> someAuditData;


    @Before
    public void setup() {
        someAuditEventType = AuditEventType.INCOME_PROVING_HMRC_INCOME_REQUEST;
        someUUID = UUID.randomUUID();
        someAuditData = new HashMap<>();
        when(mockRequestData.sessionId()).thenReturn("some session id");
        when(mockRequestData.correlationId()).thenReturn("some correlation id");
        when(mockRequestData.deploymentName()).thenReturn("some deployment name");
        when(mockRequestData.deploymentNamespace()).thenReturn("some deployment namespace");
        when(mockRequestData.userId()).thenReturn("some user id");
    }

    @Test
    public void shouldUseCollaborators() {

        auditService.add(someAuditEventType, someUUID, someAuditData);

        verify(mockRepository).save(any(AuditEntry.class));
    }

    @Test
    public void shouldThrowExceptionWhenJsonMappingError() throws JsonProcessingException {

        when(mockMapper.writeValueAsString(someAuditData)).thenThrow(JsonProcessingException.class);

        assertThatThrownBy(() -> auditService.add(someAuditEventType, someUUID, someAuditData))
                .isInstanceOf(AuditDataException.class)
                .hasCauseInstanceOf(JsonProcessingException.class);
    }

    @Test
    public void shouldCreateAuditEntry() throws JsonProcessingException {

        when(mockMapper.writeValueAsString(someAuditData)).thenReturn("some json");

        auditService.add(someAuditEventType, someUUID, someAuditData);

        verify(mockRepository).save(captorAuditEntry.capture());

        AuditEntry arg = captorAuditEntry.getValue();

        assertThat(arg.getUuid()).isEqualTo(someUUID.toString());
        assertThat(arg.getSessionId()).isEqualTo("some session id");
        assertThat(arg.getCorrelationId()).isEqualTo("some correlation id");
        assertThat(arg.getUserId()).isEqualTo("some user id");
        assertThat(arg.getDeployment()).isEqualTo("some deployment name");
        assertThat(arg.getNamespace()).isEqualTo("some deployment namespace");
        assertThat(arg.getType()).isEqualTo(someAuditEventType);
        assertThat(arg.getDetail()).isEqualTo("some json");
    }

}
