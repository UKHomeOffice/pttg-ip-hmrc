package uk.gov.digital.ho.pttg.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.application.util.TraversonFollower;

import java.net.URI;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_REQUEST_FOLLOWS;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_RESPONSE_RECEIVED;

@RunWith(MockitoJUnitRunner.class)
public class HmrcCallWrapperTest {

    private static final String LOG_TEST_APPENDER = "HmrcCallWrapperAppender";
    private HmrcCallWrapper hmrcCallWrapper;

    @Mock private RestTemplate mockRestTemplate;
    @Mock private TraversonFollower mockTraversonFollower;

    private Appender<ILoggingEvent> mockAppender;

    @Before
    public void setup() {
        hmrcCallWrapper = new HmrcCallWrapper(mockRestTemplate, mockTraversonFollower);

        mockAppender = mock(Appender.class);
        mockAppender.setName(LOG_TEST_APPENDER);

        Logger logger = (Logger) LoggerFactory.getLogger(HmrcCallWrapper.class);
        logger.setLevel(Level.INFO);
        logger.addAppender(mockAppender);
    }

    @After
    public void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(HmrcCallWrapper.class);
        logger.detachAppender(LOG_TEST_APPENDER);
    }

    @Test
    public void shouldThrowCustomExceptionForHttpForbidden() {
        when(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(FORBIDDEN));

        assertThatThrownBy(() -> hmrcCallWrapper.exchange(new URI("some-uri"), POST, new HttpEntity("some-body"), new ParameterizedTypeReference<Resource<String>>() {}))
                .isInstanceOf(ApplicationExceptions.ProxyForbiddenException.class);
    }

    @Test
    public void shouldThrowCustomExceptionForHttpUnauthorised() {
        when(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(UNAUTHORIZED));

        assertThatThrownBy(() -> hmrcCallWrapper.exchange(new URI("some-uri"), POST, new HttpEntity("some-body"), new ParameterizedTypeReference<Resource<String>>() {}))
                .isInstanceOf(ApplicationExceptions.HmrcUnauthorisedException.class);

    }

    @Test
    public void shouldThrowCustomExceptionForHmrcMatchingFailed() {
        byte[] responseBody = "blah blahMATCHING_FAILEDblah blah".getBytes(UTF_8);

        when(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(FORBIDDEN, "", responseBody, UTF_8));

        assertThatThrownBy(() -> hmrcCallWrapper.exchange(new URI("some-uri"), POST, new HttpEntity("some-body"), new ParameterizedTypeReference<Resource<String>>() {}))
                .isInstanceOf(ApplicationExceptions.HmrcNotFoundException.class);

    }

    @Test
    public void shouldThrowCustomExceptionForHmrcOverRateLimit() {
        when(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> hmrcCallWrapper.exchange(new URI("some-uri"), POST, new HttpEntity("some-body"), new ParameterizedTypeReference<Resource<String>>() {}))
                .isInstanceOf(ApplicationExceptions.HmrcOverRateLimitException.class);
    }

    @Test
    public void shouldRethrowOtherExceptions() {
        when(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new NullPointerException());

        assertThatThrownBy(() -> hmrcCallWrapper.exchange(new URI("some-uri"), POST, new HttpEntity("some-body"), new ParameterizedTypeReference<Resource<String>>() {}))
                .isInstanceOf(NullPointerException.class);

    }

    @Test
    public void shouldThrowCustomExceptionForHttpForbiddenWithTraverson() {
        when(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(FORBIDDEN));

        assertThatThrownBy(() -> hmrcCallWrapper.followTraverson("some-link", "some-access-token", new ParameterizedTypeReference<Resource<String>>() {}))
                .isInstanceOf(ApplicationExceptions.ProxyForbiddenException.class);
    }

    @Test
    public void shouldThrowCustomExceptionForHttpUnauthorisedWithTraverson() {
        when(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(UNAUTHORIZED));

        assertThatThrownBy(() -> hmrcCallWrapper.followTraverson("some-link", "some-access-token", new ParameterizedTypeReference<Resource<String>>() {}))
                .isInstanceOf(ApplicationExceptions.HmrcUnauthorisedException.class);
    }

    @Test
    public void shouldThrowCustomExceptionForHmrcNotFoundWithTraverson() {
        byte[] responseBody = "blah blahMATCHING_FAILEDblah blah".getBytes(UTF_8);
        when(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(FORBIDDEN, "", responseBody, UTF_8));

        assertThatThrownBy(() -> hmrcCallWrapper.followTraverson("some-link", "some-access-token", new ParameterizedTypeReference<Resource<String>>() {}))
                .isInstanceOf(ApplicationExceptions.HmrcNotFoundException.class);
    }

    @Test
    public void shouldThrowCustomExceptionForHmrcOverRateLimitWithTraverson() {
        when(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> hmrcCallWrapper.followTraverson("some-link", "some-access-token", new ParameterizedTypeReference<Resource<String>>() {}))
                .isInstanceOf(ApplicationExceptions.HmrcOverRateLimitException.class);
    }

    @Test
    public void shouldRethrowCOtherExceptionsWithTraverson() {
        when(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new NullPointerException());

        assertThatThrownBy(() -> hmrcCallWrapper.followTraverson("some-link", "some-access-token", new ParameterizedTypeReference<Resource<String>>() {}))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void shouldLogEventsAroundRestTemplate_exchange() {

        hmrcCallWrapper.exchange(null, null, null, null);

        then(mockAppender)
                .should()
                .doAppend(argThat(isALogMessageWith(
                        "HMRC exchange request follows",
                        0,
                        HMRC_REQUEST_FOLLOWS)));
        then(mockAppender)
                .should()
                .doAppend(argThat(isALogMessageWith(
                        "HMRC exchange response received",
                        0,
                        HMRC_RESPONSE_RECEIVED)));
    }

    @Test
    public void shouldLogEventsAroundRestTemplate_followTraverson() {

        hmrcCallWrapper.followTraverson(null, null, null);

        then(mockAppender)
                .should()
                .doAppend(argThat(isALogMessageWith(
                        "HMRC traverson request follows",
                        0,
                        HMRC_REQUEST_FOLLOWS)));
        then(mockAppender)
                .should()
                .doAppend(argThat(isALogMessageWith(
                        "HMRC traverson response received",
                        0,
                        HMRC_RESPONSE_RECEIVED)));
    }

    private ArgumentMatcher<ILoggingEvent> isALogMessageWith(String message, int eventArgIndex, LogEvent event) {
        return (argument) -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals(message) &&
                           loggingEvent.getArgumentArray()[eventArgIndex].equals(new ObjectAppendingMarker("event_id", event));
        };
    }
}
