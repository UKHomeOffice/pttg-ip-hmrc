package uk.gov.digital.ho.pttg.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_SERVICE_GENERATED_CORRELATION_ID;

@RunWith(MockitoJUnitRunner.class)
public class RequestHeaderDataTest {

    @Mock private HttpServletRequest mockHttpServletRequest;
    @Mock private HttpServletResponse mockHttpServletResponse;
    @Mock private Object mockHandler;
    @Mock private Appender<ILoggingEvent> mockAppender;

    private RequestHeaderData requestData;

    @Before
    public void setup() {
        requestData = new RequestHeaderData();
        ReflectionTestUtils.setField(requestData, "hmrcAccessBasicAuth", "user:password");

        Logger rootLogger = (Logger) LoggerFactory.getLogger(RequestHeaderData.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
    }

    @Test
    public void shouldProduceBasicAuthHeaderValue() {
        assertThat(requestData.hmrcBasicAuth()).isEqualTo("Basic dXNlcjpwYXNzd29yZA==");
    }

    @Test
    public void shouldDefaultRequestData() {
        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.deploymentName()).isNull();
        assertThat(requestData.deploymentNamespace()).isNull();
        assertThat(requestData.sessionId()).isEqualTo("unknown");
        assertThat(requestData.correlationId()).isNotNull();
        assertThat(requestData.userId()).isEqualTo("unknown");
    }

    @Test
    public void shouldUseSessionIdFromRequest() {
        when(mockHttpServletRequest.getHeader("x-session-id")).thenReturn("some session id");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.sessionId()).isEqualTo("some session id");
    }

    @Test
    public void shouldUseCorrelationIdFromRequest() {
        when(mockHttpServletRequest.getHeader("x-correlation-id")).thenReturn("some correlation id");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.correlationId()).isEqualTo("some correlation id");
    }

    @Test
    public void shouldUseUserIdFromRequest() {
        when(mockHttpServletRequest.getHeader("x-auth-userid")).thenReturn("some user id");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.userId()).isEqualTo("some user id");
    }

    @Test
    public void shouldLogWhenCorrelationIdGenerated() {
        when(mockHttpServletRequest.getHeader("x-correlation-id")).thenReturn(null);

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Generated new correlation id as not passed in request header") &&
                    loggingEvent.getArgumentArray()[0].equals(new ObjectAppendingMarker("event_id", HMRC_SERVICE_GENERATED_CORRELATION_ID));
        }));
    }

    @Test
    public void shouldAddRequestTimeStampToMDC() {
        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);
        assertThat(MDC.get("request-timestamp")).isNotNull();
    }

    @Test
    public void shouldReturnRequestDuration() {
        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);
        assertThat(requestData.calculateRequestDuration()).isNotNegative();
    }
}