package uk.gov.digital.ho.pttg.audit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.api.ComponentTraceHeaderData;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_INCOME_REQUEST;

@RunWith(MockitoJUnitRunner.class)
public class AuditClientTest {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY = 1;
    private static final String LOG_TEST_APPENDER = "tester";

    @Mock private RequestHeaderData mockRequestHeaderData;
    @Mock private ComponentTraceHeaderData mockComponentTraceHeaderData;
    @Mock private RestTemplate mockRestTemplate;
    @Mock private AuditIndividualData mockAuditableData;
    @Mock private Appender<ILoggingEvent> mockAppender;
    @Mock private ObjectMapper mockMapper;

    @Captor private ArgumentCaptor<HttpEntity> captorHttpEntity;

    private AuditClient client;

    @Before
    public void setup() {
        final Clock clock = Clock.fixed(Instant.now(), ZoneId.of("Europe/London"));
        client = new AuditClient(clock, mockRestTemplate, mockRequestHeaderData, mockComponentTraceHeaderData,
                                 "endpoint", mockMapper, MAX_RETRY_ATTEMPTS, RETRY_DELAY);

        mockAppender.setName(LOG_TEST_APPENDER);
        Logger logger = (Logger) LoggerFactory.getLogger(AuditClient.class);
        logger.setLevel(Level.INFO);
        logger.addAppender(mockAppender);
    }

    @After
    public void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(AuditClient.class);
        logger.detachAppender(LOG_TEST_APPENDER);
    }

    @Test
    public void dispatchAuditableDataShouldRetryOnHttpError() {
        when(mockRestTemplate.exchange(eq("endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        client.add(HMRC_INCOME_REQUEST, UUID.randomUUID(), mockAuditableData);

        verify(mockRestTemplate, times(3)).exchange(eq("endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class));
    }

    @Test
    public void dispatchAuditableDataShouldLogErrorOnAuditFailure() {
        when(mockRestTemplate.exchange(eq("endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        client.add(HMRC_INCOME_REQUEST, UUID.randomUUID(), mockAuditableData);

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
        client.add(HMRC_INCOME_REQUEST, UUID.randomUUID(), mockAuditableData);

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
        when(mockRestTemplate.exchange(eq("endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        client.add(HMRC_INCOME_REQUEST, UUID.randomUUID(), mockAuditableData);

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

    @Test
    public void shouldSetHeaders() {

        when(mockRequestHeaderData.auditBasicAuth()).thenReturn("some basic auth header value");
        when(mockRequestHeaderData.sessionId()).thenReturn("some session id");
        when(mockRequestHeaderData.correlationId()).thenReturn("some correlation id");
        when(mockRequestHeaderData.userId()).thenReturn("some user id");
        String someComponentTrace = "pttg-ip-api,pttg-ip-hmrc";
        when(mockComponentTraceHeaderData.componentTrace()).thenReturn(someComponentTrace);
        client.add(HMRC_INCOME_REQUEST, UUID.randomUUID(), null);

        verify(mockRestTemplate).exchange(eq("endpoint"), eq(POST), captorHttpEntity.capture(), eq(Void.class));

        HttpHeaders headers = captorHttpEntity.getValue().getHeaders();
        assertThat(headers.get("Authorization").get(0)).isEqualTo("some basic auth header value");
        assertThat(headers.get("Content-Type").get(0)).isEqualTo(APPLICATION_JSON_VALUE);
        assertThat(headers.get(RequestHeaderData.SESSION_ID_HEADER).get(0)).isEqualTo("some session id");
        assertThat(headers.get(RequestHeaderData.CORRELATION_ID_HEADER).get(0)).isEqualTo("some correlation id");
        assertThat(headers.get(RequestHeaderData.USER_ID_HEADER).get(0)).isEqualTo("some user id");
        assertThat(headers.get(ComponentTraceHeaderData.COMPONENT_TRACE_HEADER).get(0)).isEqualTo(someComponentTrace);
    }

    @Test
    public void add_successResponse_updateComponentTrace() {
        ResponseEntity<Void> someResponse = new ResponseEntity<>(HttpStatus.OK);
        given(mockRestTemplate.exchange(eq("endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .willReturn(someResponse);

        client.add(HMRC_INCOME_REQUEST, UUID.randomUUID(), null);

        then(mockComponentTraceHeaderData).should().updateComponentTrace(someResponse);
    }

    @Test
    public void add_httpException_updateComponentTrace() {
        HttpStatusCodeException someHttpException = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        given(mockRestTemplate.exchange(eq("endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .willThrow(someHttpException);

        client.add(HMRC_INCOME_REQUEST, UUID.randomUUID(), null);

        then(mockComponentTraceHeaderData).should().updateComponentTrace(someHttpException);
    }

    @Test
    public void add_otherException_doNotUpdateComponentTrace() {
        Exception anyException = new NullPointerException();
        given(mockRestTemplate.exchange(eq("endpoint"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .willThrow(anyException);

        client.add(HMRC_INCOME_REQUEST, UUID.randomUUID(), null);

        then(mockComponentTraceHeaderData).should(never()).updateComponentTrace(any(ResponseEntity.class));
        then(mockComponentTraceHeaderData).should(never()).updateComponentTrace(any(HttpStatusCodeException.class));
    }
}
