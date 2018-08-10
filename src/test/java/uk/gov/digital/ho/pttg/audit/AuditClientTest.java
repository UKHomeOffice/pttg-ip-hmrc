package uk.gov.digital.ho.pttg.audit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditClientTest {

    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final int RETRY_DELAY = 1;

    @Mock
    private RequestHeaderData mockRequestHeaderData;
    @Mock
    private RestTemplate mockRestTemplate;
    @Mock
    private AuditIndividualData mockAuditableData;
    @Mock
    private Appender<ILoggingEvent> mockAppender;

    private AuditClient client;

    @Before
    public void setup(){
        final Clock clock = Clock.fixed(Instant.now(), ZoneId.of("Europe/London"));
        client = new AuditClient(clock, mockRestTemplate, mockRequestHeaderData, "endpoint", new ObjectMapper(),
                MAX_RETRY_ATTEMPTS, RETRY_DELAY);
        when(mockRestTemplate.exchange(eq("endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

    }

    @Test
    public void dispatchAuditableDataShouldRetryOnHttpError() {
        try {
            client.add(AuditEventType.HMRC_INCOME_REQUEST, UUID.randomUUID(), mockAuditableData);
        }
        catch (HttpServerErrorException e){
            // Ignore expected exception.
        }

        verify(mockRestTemplate, times(5)).exchange(eq("endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    public void logInfoOnRetry() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(AuditClient.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);

        try {
            client.add(AuditEventType.HMRC_INCOME_REQUEST, UUID.randomUUID(), mockAuditableData);
        }
        catch (HttpServerErrorException e){
            // Ignore expected exception.
        }

        verifyLogMessage("Audit attempt 1 of 5");
        verifyLogMessage("Audit attempt 2 of 5");
        verifyLogMessage("Audit attempt 3 of 5");
        verifyLogMessage("Audit attempt 4 of 5");
        verifyLogMessage("Audit attempt 5 of 5");
    }


    private void verifyLogMessage(String message) {
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals(message) &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
        }));
    }
}