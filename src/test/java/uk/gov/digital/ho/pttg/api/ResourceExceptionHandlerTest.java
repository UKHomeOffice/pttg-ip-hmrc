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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.*;

@RunWith(MockitoJUnitRunner.class)
public class ResourceExceptionHandlerTest {

    private ResourceExceptionHandler handler = new ResourceExceptionHandler();
    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Before
    public void setup() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(ResourceExceptionHandler.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
    }

    @Test
    public void shouldProduceInternalServerErrorForHmrcException() {
        HmrcException mockHmrcException = mock(HmrcException.class);
        when(mockHmrcException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(mockHmrcException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldLogErrorForForHmrcException() {
        HmrcException mockHmrcException = mock(HmrcException.class);
        when(mockHmrcException.getMessage()).thenReturn("any message");

        handler.handle(mockHmrcException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("HmrcException: any message") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldProduceHttpUnauthorisedForHmrcUnauthorisedException() {
        HmrcUnauthorisedException unauthorisedException = mock(HmrcUnauthorisedException.class);
        when(unauthorisedException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(unauthorisedException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
    }

    @Test
    public void shouldLogErrorForHmrcUnauthorisedException() {
        HmrcUnauthorisedException unauthorisedException = mock(HmrcUnauthorisedException.class);
        when(unauthorisedException.getMessage()).thenReturn("any message");

        handler.handle(unauthorisedException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("HmrcUnauthorisedException: any message") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldProduceInternalServerErrorForHttpClientErrorException() {
        HttpClientErrorException mockHttpClientErrorException = mock(HttpClientErrorException.class);
        when(mockHttpClientErrorException.getMessage()).thenReturn("any message");
        when(mockHttpClientErrorException.getStatusCode()).thenReturn(I_AM_A_TEAPOT);

        ResponseEntity responseEntity = handler.handle(mockHttpClientErrorException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(I_AM_A_TEAPOT);
    }


    @Test
    public void shouldLogErrorForHttpClientErrorException() {
        HttpClientErrorException mockHttpClientErrorException = mock(HttpClientErrorException.class);
        when(mockHttpClientErrorException.getMessage()).thenReturn("any message");
        when(mockHttpClientErrorException.getStatusCode()).thenReturn(I_AM_A_TEAPOT);

        handler.handle(mockHttpClientErrorException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("HttpClientErrorException: 418 any message") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldProduceInternalServerErrorForHttpServerErrorException() {
        HttpServerErrorException mockHttpServerErrorException = mock(HttpServerErrorException.class);
        when(mockHttpServerErrorException.getMessage()).thenReturn("any message");
        when(mockHttpServerErrorException.getStatusCode()).thenReturn(INTERNAL_SERVER_ERROR);

        ResponseEntity responseEntity = handler.handle(mockHttpServerErrorException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldLogErrorForHttpServerErrorException() {
        HttpServerErrorException mockHttpServerErrorException = mock(HttpServerErrorException.class);
        when(mockHttpServerErrorException.getMessage()).thenReturn("any message");
        when(mockHttpServerErrorException.getStatusCode()).thenReturn(INTERNAL_SERVER_ERROR);

        handler.handle(mockHttpServerErrorException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("HttpServerErrorException: any message") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldProduceInternalServerErrorForException() {
        Exception mockException = mock(Exception.class);
        when(mockException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(mockException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldLogErrorForException() {
        Exception mockException = mock(Exception.class);
        when(mockException.getMessage()).thenReturn("any message");

        handler.handle(mockException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Fault Detected:") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldProduceInternalServerErrorForProxyForbiddenException() {
        ProxyForbiddenException mockException = mock(ProxyForbiddenException.class);
        when(mockException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(mockException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldLogErrorForProxyForbiddenException() {
        ProxyForbiddenException mockException = mock(ProxyForbiddenException.class);
        when(mockException.getMessage()).thenReturn("any message");

        handler.handle(mockException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Received 403 Forbidden from a request to HMRC. This was from the proxy and not HMRC.") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldProduceInternalServerErrorForHmrcNotFoundException() {
        HmrcNotFoundException mockHmrcNotFoundException = mock(HmrcNotFoundException.class);
        when(mockHmrcNotFoundException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(mockHmrcNotFoundException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
    }


    @Test
    public void shouldLogMessageForHmrcNotFoundException() {
        HmrcNotFoundException mockHmrcNotFoundException = mock(HmrcNotFoundException.class);
        when(mockHmrcNotFoundException.getMessage()).thenReturn("any message");

        handler.handle(mockHmrcNotFoundException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("HmrcNotFoundException: any message") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldProduceInternalServerErrorForRestClientException() {
        RestClientException mockRestClientException = mock(RestClientException.class);
        when(mockRestClientException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(mockRestClientException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldLogErrorForRestClientException() {
        RestClientException mockRestClientException = mock(RestClientException.class);
        when(mockRestClientException.getMessage()).thenReturn("any message");

        handler.handle(mockRestClientException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("RestClientException:") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldProduceUnprocessableEntityForInvalidNationalInsuranceNumberException() {
        InvalidNationalInsuranceNumberException mockInvalidNationalInsuranceNumberException = mock(InvalidNationalInsuranceNumberException.class);
        when(mockInvalidNationalInsuranceNumberException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(mockInvalidNationalInsuranceNumberException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(UNPROCESSABLE_ENTITY);
    }

    @Test
    public void shouldLogErrorForInvalidNationalInsuranceNumberException() {
        InvalidNationalInsuranceNumberException mockInvalidNationalInsuranceNumberException = mock(InvalidNationalInsuranceNumberException.class);
        when(mockInvalidNationalInsuranceNumberException.getMessage()).thenReturn("any message");

        handler.handle(mockInvalidNationalInsuranceNumberException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Service called with invalid NINO: any message") &&
                           ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldProduceUnprocessableEntityForInvalidIdentityException() {
        InvalidIdentityException mockInvalidIdentityException = mock(InvalidIdentityException.class);
        when(mockInvalidIdentityException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(mockInvalidIdentityException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(UNPROCESSABLE_ENTITY);
    }

    @Test
    public void shouldLogErrorForInvalidIdentityException() {
        InvalidIdentityException mockInvalidIdentityException = mock(InvalidIdentityException.class);
        when(mockInvalidIdentityException.getMessage()).thenReturn("any message");

        handler.handle(mockInvalidIdentityException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Service called with invalid identity: any message") &&
                           ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

}