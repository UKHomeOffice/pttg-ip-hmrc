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
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import uk.gov.digital.ho.pttg.application.LogEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.*;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@RunWith(MockitoJUnitRunner.class)
public class ResourceExceptionHandlerTest {

    private ResourceExceptionHandler handler;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Mock
    private RequestHeaderData mockRequestHeaderData;

    @Before
    public void setup() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(ResourceExceptionHandler.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);

        handler = new ResourceExceptionHandler(mockRequestHeaderData);
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

        assertErrorLog("HmrcUnauthorisedException: any message", HMRC_AUTHENTICATION_ERROR, 1);
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

        assertErrorLog("HttpClientErrorException: 418 any message", HMRC_SERVICE_RESPONSE_ERROR, 2);
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

        assertErrorLog("HttpServerErrorException: any message", HMRC_SERVICE_RESPONSE_ERROR, 1);
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

        assertErrorLog("Fault Detected:", HMRC_SERVICE_RESPONSE_ERROR, 1);
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

        assertErrorLog("Received 403 Forbidden from a request to HMRC. This was from the proxy and not HMRC.", HMRC_PROXY_ERROR, 0);
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

        assertInfoLog("HmrcNotFoundException: any message", HMRC_SERVICE_RESPONSE_NOT_FOUND, 1);
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

        assertErrorLog("RestClientException:", HMRC_SERVICE_RESPONSE_ERROR, 1);
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

    @Test
    public void shouldLogRequestDurationOnHmrcException(){
        HmrcException mockHmrcException = mock(HmrcException.class);

        handler.handle(mockHmrcException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

        return ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("request_duration_ms");

        }));
    }

    @Test
    public void shouldLogRequestDurationOnHttpClientErrorException(){
        HttpClientErrorException mockHttpClientErrorException = mock(HttpClientErrorException.class);
        when(mockHttpClientErrorException.getStatusCode()).thenReturn(I_AM_A_TEAPOT);

        handler.handle(mockHttpClientErrorException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[3]).getFieldName().equals("request_duration_ms");
        }));
    }

    @Test
    public void shouldLogRequestDurationOnHmrcUnauthorisedException(){
        HmrcUnauthorisedException mockHmrcUnauthorisedException = mock(HmrcUnauthorisedException.class);

        handler.handle(mockHmrcUnauthorisedException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("request_duration_ms");
        }));
    }

    @Test
    public void shouldLogRequestDurationOnHttpServerErrorException(){
        HttpServerErrorException mockHttpServerErrorException = mock(HttpServerErrorException.class);
        when(mockHttpServerErrorException.getStatusCode()).thenReturn(I_AM_A_TEAPOT);

        handler.handle(mockHttpServerErrorException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("request_duration_ms");
        }));
    }

    @Test
    public void shouldLogRequestDurationOnRestClientException(){
        RestClientException mockRestClientException = mock(RestClientException.class);

        handler.handle(mockRestClientException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("request_duration_ms");
        }));
    }

    @Test
    public void shouldLogRequestDurationOnFaultDetection(){
        Exception mockException = mock(Exception.class);

        handler.handle(mockException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("request_duration_ms");
        }));
    }

    @Test
    public void shouldLogRequestDurationOnHmrcNotFoundException(){
        HmrcNotFoundException mockHmrcNotFoundException = mock(HmrcNotFoundException.class);

        handler.handle(mockHmrcNotFoundException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("request_duration_ms");
        }));
    }

    @Test
    public void shouldLogRequestDurationOnProxyForbiddenException(){
        ProxyForbiddenException mockProxyForbiddenException = mock(ProxyForbiddenException.class);

        handler.handle(mockProxyForbiddenException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("request_duration_ms");
        }));
    }

    @Test
    public void shouldLogRequestDurationOnInvalidIdentityException(){
        InvalidIdentityException mockInvalidIdentityException = mock(InvalidIdentityException.class);

        handler.handle(mockInvalidIdentityException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("request_duration_ms");
        }));
    }

    @Test
    public void shouldLogRequestDurationOnInvalidNationalInsuranceNumberException(){
        InvalidNationalInsuranceNumberException mockInvalidNinoException = mock(InvalidNationalInsuranceNumberException.class);

        handler.handle(mockInvalidNinoException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("request_duration_ms");
        }));
    }

    @Test
    public void shouldLogRequestDurationOnHttpMessageConversionException(){
        HttpMessageConversionException mockHttpMessageConversionException = mock(HttpMessageConversionException.class);

        handler.handle(mockHttpMessageConversionException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("request_duration_ms");
        }));
    }

    @Test
    public void handle_HmrcOverRateLimitException_produceTooManyRequestsResponse() {
        HmrcOverRateLimitException hmrcOverRateLimitException = new HmrcOverRateLimitException("some message");

        ResponseEntity response = handler.handle(hmrcOverRateLimitException);

        assertThat(response.getStatusCode()).isEqualTo(TOO_MANY_REQUESTS);
    }

    @Test
    public void handle_HmrcOverRateLimitException_returnExceptionMessageInResponse() {
        HmrcOverRateLimitException hmrcOverRateLimitException = new HmrcOverRateLimitException("some message");

        ResponseEntity response = handler.handle(hmrcOverRateLimitException);

        assertThat(response.getBody()).isEqualTo("some message");
    }

    @Test
    public void handle_HmrcOverRateLimitException_logError() {
        HmrcOverRateLimitException hmrcOverRateLimitException = new HmrcOverRateLimitException("some message");

        handler.handle(hmrcOverRateLimitException);

        assertErrorLog("HMRC Rate Limit Exceeded: some message", HMRC_OVER_RATE_LIMIT, 1);
    }

    @Test
    public void handle_HmrcOverRateLimitException_logRequestDuration() {
        HmrcOverRateLimitException hmrcOverRateLimitException = new HmrcOverRateLimitException("some message");

        handler.handle(hmrcOverRateLimitException);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("request_duration_ms");
        }));
    }

    private void assertInfoLog(String expectedMessage, LogEvent expectedLogEvent, int expectedEventIndex) {
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals(expectedMessage) &&
                    loggingEvent.getLevel().equals(Level.INFO) &&
                    loggingEvent.getArgumentArray()[expectedEventIndex].equals(new ObjectAppendingMarker("event_id", expectedLogEvent));
        }));
    }

    private void assertErrorLog(String expectedMessage, LogEvent expectedLogEvent, int expectedEventIndex) {
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals(expectedMessage) &&
                    loggingEvent.getLevel().equals(Level.ERROR) &&
                    loggingEvent.getArgumentArray()[expectedEventIndex].equals(new ObjectAppendingMarker("event_id", expectedLogEvent));
        }));
    }
}