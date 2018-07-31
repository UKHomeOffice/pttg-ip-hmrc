package uk.gov.digital.ho.pttg.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.apache.http.HttpHost;
import org.apache.http.conn.HttpHostConnectException;
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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.api.RequestData;
import uk.gov.digital.ho.pttg.dto.AccessCode;

import java.net.ConnectException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RunWith(MockitoJUnitRunner.class)
public class HmrcAccessCodeClientTest {
    private static final String ACCESS_CODE_URL = "https://localhost:9876";
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final long RETRY_DELAY_IN_MILLIS = 0L;
    private static final String TEST_ACCESS_CODE = "Test Access Code";

    @Mock
    private RestTemplate mockRestTemplate;

    @Mock
    private RequestData mockRequestData;

    @Mock
    private AccessCode mockAccessCode;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<HttpEntity> httpEntityCaptor;

    @Captor
    private ArgumentCaptor<URI> uriCaptor;

    private HmrcAccessCodeClient accessCodeClient;

    @Before
    public void setUp() {
        accessCodeClient = new HmrcAccessCodeClient(mockRestTemplate, mockRequestData, ACCESS_CODE_URL, MAX_RETRY_ATTEMPTS, RETRY_DELAY_IN_MILLIS);
    }

    @Test
    public void shouldSendRequestToCorrectEndpoint() {
        // given
        when(mockRestTemplate.exchange(uriCaptor.capture(), eq(HttpMethod.GET), isA(HttpEntity.class), eq(AccessCode.class))).thenReturn(okResponse());

        // when
        accessCodeClient.getAccessCode();

        // then
        final URI accessEndpoint = uriCaptor.getValue();

        // verify requested URI matches expected
        assertThat(accessEndpoint).hasScheme("https");
        assertThat(accessEndpoint).hasAuthority("localhost:9876");
        assertThat(accessEndpoint).hasPath("/access");
        assertThat(accessEndpoint).hasNoFragment();
        assertThat(accessEndpoint).hasNoParameters();
        assertThat(accessEndpoint).hasNoQuery();
    }

    @Test
    public void shouldCallAccessCodeServiceToGetTheLatestAccessCode() {
        // given
        when(mockRestTemplate.exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(AccessCode.class)))
                .thenReturn(okResponse());

        when(mockAccessCode.getCode()).thenReturn(TEST_ACCESS_CODE);

        // when
        final String actualAccessCode = accessCodeClient.getAccessCode();

        // then
        verify(mockRestTemplate).exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(AccessCode.class));
        verifyNoMoreInteractions(mockRestTemplate);

        assertThat(actualAccessCode).isEqualTo(TEST_ACCESS_CODE);
    }

    @Test
    public void shouldSetHeadersForCallToAccessCodeService() {
        // given
        when(mockRestTemplate.exchange(isA(URI.class), eq(HttpMethod.GET), httpEntityCaptor.capture(), eq(AccessCode.class)))
                .thenReturn(okResponse());

        final String testSessionId = "TestSessionId";
        final String testCorrelationId = "TestCorrelationId";
        final String testUserId = "TestUserId";
        final String testHmrcBasicAuth = "TestHmrcBasicAuth";

        when(mockRequestData.sessionId()).thenReturn(testSessionId);
        when(mockRequestData.correlationId()).thenReturn(testCorrelationId);
        when(mockRequestData.userId()).thenReturn(testUserId);
        when(mockRequestData.hmrcBasicAuth()).thenReturn(testHmrcBasicAuth);

        // when
        accessCodeClient.getAccessCode();

        // then
        final HttpEntity httpEntity = httpEntityCaptor.getValue();
        final HttpHeaders headers = httpEntity.getHeaders();

        // verify request contains all required headers AND they are correct
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(headers.get(AUTHORIZATION)).hasSize(1).contains(testHmrcBasicAuth);
        assertThat(headers.get("x-session-id")).hasSize(1).contains(testSessionId);
        assertThat(headers.get("x-correlation-id")).hasSize(1).contains(testCorrelationId);
        assertThat(headers.get("x-auth-userid")).hasSize(1).contains(testUserId);
    }

    @Test
    public void shouldRethrowExceptionWhenHttpClientErrorException() {
        // given
        final String exceptionMessage = "ExceptionMessage";
        when(mockRestTemplate.exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(AccessCode.class)))
                .thenThrow(new HttpClientErrorException(BAD_REQUEST, exceptionMessage));

        // when
        try {
            accessCodeClient.getAccessCode();
            fail("Expected `RestClientException` to be thrown");
        } catch (final HttpClientErrorException e) {
            assertThat(e.getMessage()).contains(exceptionMessage);
        }

        // then
        verify(mockRestTemplate).exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(AccessCode.class));
        verifyNoMoreInteractions(mockRestTemplate);
    }

    @Test
    public void shouldKeepRetryingHttpServerErrorExceptionsToMaxAttempts() {
        // given
        final String exceptionMessage = "ExceptionMessage";
        when(mockRestTemplate.exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(AccessCode.class)))
                .thenThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR, exceptionMessage));

        // when
        try {
            accessCodeClient.getAccessCode();
            fail("Expected `RestClientException` to be thrown");
        } catch (final HttpServerErrorException e) {
            assertThat(e.getMessage()).contains(exceptionMessage);
        }

        // then
        verify(mockRestTemplate, times(MAX_RETRY_ATTEMPTS)).exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(AccessCode.class));
        verifyNoMoreInteractions(mockRestTemplate);
    }

    @Test
    public void shouldLogRetryOnServerError() {
        final String exceptionMessage = "ExceptionMessage";
        when(mockRestTemplate.exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(AccessCode.class)))
                .thenThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR, exceptionMessage));

        Logger rootLogger = (Logger) LoggerFactory.getLogger(HmrcAccessCodeClient.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);

        try {
            accessCodeClient.getAccessCode();
        } catch (final HttpServerErrorException e) {
            // Ignore expected exception
        }

        verifyHmrcAccessCodeCallMessage("Attempting to fetch the latest access code. Attempt number 1 of 5");
        verifyHmrcAccessCodeCallMessage("Attempting to fetch the latest access code. Attempt number 2 of 5");
        verifyHmrcAccessCodeCallMessage("Attempting to fetch the latest access code. Attempt number 3 of 5");
        verifyHmrcAccessCodeCallMessage("Attempting to fetch the latest access code. Attempt number 4 of 5");
        verifyHmrcAccessCodeCallMessage("Attempting to fetch the latest access code. Attempt number 5 of 5");
    }
    @Test
    public void shouldSucceedAfterHttpServerErrorExceptionRetryAttempt() {
        // given
        when(mockRestTemplate.exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(AccessCode.class)))
                .thenThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR, "ExceptionMessage"))
                .thenReturn(okResponse());

        when(mockAccessCode.getCode()).thenReturn(TEST_ACCESS_CODE);

        // when
        final String accessCode = accessCodeClient.getAccessCode();

        // then
        verify(mockRestTemplate, times(2)).exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(AccessCode.class));
        verifyNoMoreInteractions(mockRestTemplate);

        assertThat(accessCode).isEqualTo(TEST_ACCESS_CODE);
    }

    @Test
    public void shouldKeepRetryingConnectionRefusedErrorsToMaxAttempts() {
        // given
        final String exceptionMessage = "ExceptionMessage";
        when(mockRestTemplate.exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(AccessCode.class)))
                .thenThrow(connectionRefusedException(exceptionMessage));

        // when
        try {
            accessCodeClient.getAccessCode();
            fail("Expected `RestClientException` to be thrown");
        } catch (final ResourceAccessException e) {
            assertThat(e.getLocalizedMessage()).contains(exceptionMessage);
        }

        // then
        verify(mockRestTemplate, times(MAX_RETRY_ATTEMPTS)).exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(AccessCode.class));
        verifyNoMoreInteractions(mockRestTemplate);
    }

    @Test
    public void shouldSucceedAfterConnectionRefusedRetryAttempt() {
        // given
        when(mockRestTemplate.exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(AccessCode.class)))
                .thenThrow(connectionRefusedException("ExceptionMessage"))
                .thenReturn(okResponse());

        when(mockAccessCode.getCode()).thenReturn(TEST_ACCESS_CODE);

        // when
        final String accessCode = accessCodeClient.getAccessCode();

        // then
        verify(mockRestTemplate, times(2)).exchange(isA(URI.class), eq(HttpMethod.GET), isA(HttpEntity.class), eq(AccessCode.class));
        verifyNoMoreInteractions(mockRestTemplate);

        assertThat(accessCode).isEqualTo(TEST_ACCESS_CODE);
    }

    private ResourceAccessException connectionRefusedException(final String exceptionMessage) {
        final HttpHostConnectException httpHostConnectException = new HttpHostConnectException(new ConnectException(), HttpHost.create(ACCESS_CODE_URL));
        return new ResourceAccessException(exceptionMessage, httpHostConnectException);
    }

    private ResponseEntity<AccessCode> okResponse() {
        return new ResponseEntity<>(mockAccessCode, HttpStatus.OK);
    }

    private void verifyHmrcAccessCodeCallMessage(String message) {
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals(message) &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
        }));
    }
}