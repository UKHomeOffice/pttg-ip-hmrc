package uk.gov.digital.ho.pttg.audit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditClientTest {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY = 1;

    @Mock private RequestHeaderData mockRequestHeaderData;
    @Mock private RestTemplate mockRestTemplate;
    @Mock private AuditIndividualData mockAuditableData;
    @Mock private Appender<ILoggingEvent> mockAppender;
    @Mock private ObjectMapper mockMapper;

    private AuditClient client;

    @Before
    public void setup() {
        final Clock clock = Clock.fixed(Instant.now(), ZoneId.of("Europe/London"));
        client = new AuditClient(clock, mockRestTemplate, mockRequestHeaderData, "endpoint", mockMapper,
                MAX_RETRY_ATTEMPTS, RETRY_DELAY);
        when(mockRestTemplate.exchange(eq("endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        Logger rootLogger = (Logger) LoggerFactory.getLogger(AuditClient.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);

    }

    @Test
    public void dispatchAuditableDataShouldRetryOnHttpError() {
        client.add(AuditEventType.HMRC_INCOME_REQUEST, UUID.randomUUID(), mockAuditableData);

        verify(mockRestTemplate, times(3)).exchange(eq("endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    public void dispatchAuditableDataShouldLogErrorOnAuditFailure() {
        client.add(AuditEventType.HMRC_INCOME_REQUEST, UUID.randomUUID(), mockAuditableData);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Failed to audit HMRC_INCOME_REQUEST after retries") &&
                    loggingEvent.getLevel() == Level.ERROR &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void addShouldLogErrorOnJsonProcessingException() {
        try {
            given(mockMapper.writeValueAsString(any(AuditIndividualData.class))).willThrow(JsonProcessingException.class);
        } catch (JsonProcessingException e) {
            // Ignore expected exception
        }

        // when
        client.add(AuditEventType.HMRC_INCOME_REQUEST, UUID.randomUUID(), mockAuditableData);

        // then
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Failed to create json representation of audit data") &&
                    loggingEvent.getLevel() == Level.ERROR &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void logInfoOnRetry() {
        client.add(AuditEventType.HMRC_INCOME_REQUEST, UUID.randomUUID(), mockAuditableData);
        
        verifyLogMessage("Retrying audit attempt 1 of 2");
        verifyLogMessage("Retrying audit attempt 2 of 2");
    }

    private void verifyLogMessage(String message) {
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals(message) &&
                    loggingEvent.getLevel() == Level.INFO &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
        }));
    }
}