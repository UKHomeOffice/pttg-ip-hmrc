package uk.gov.digital.ho.pttg.application;

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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.application.util.NameNormalizer;
import uk.gov.digital.ho.pttg.application.util.TraversonFollower;
import uk.gov.digital.ho.pttg.dto.*;

import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RunWith(MockitoJUnitRunner.class)
public class HmrcHateoasClientTest {

    @Mock
    private RestTemplate mockRestTemplate;
    @Mock
    private TraversonFollower mockTraversonFollower;
    @Mock
    private NameNormalizer mockNameNormalizer;
    @Mock
    private ResponseEntity mockResponse;
    @Mock
    private Appender<ILoggingEvent> mockAppender;

    private Individual individual = new Individual("John", "Smith", "12345677", LocalDate.of(2018, 7, 30));

    @Before
    public void setup() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(HmrcHateoasClient.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
    }

    @Test
    public void shouldLogInfoBeforeMatchingRequestSent() {
        when(mockRestTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(mockResponse);
        HmrcHateoasClient client = new HmrcHateoasClient(mockRestTemplate, new NinoUtils(), new TraversonFollower(), mockNameNormalizer, "any api version", "http://localhost");

        client.getMatchResource(individual, "");

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Match Individual 12345*** via a POST to http://localhost/individuals/matching/") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoAfterMatchingRequestSent() {
        when(mockRestTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(mockResponse);
        HmrcHateoasClient client = new HmrcHateoasClient(mockRestTemplate, new NinoUtils(), new TraversonFollower(), mockNameNormalizer, "any api version", "http://something.com/anyurl");

        client.getMatchResource(individual, "");

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Successfully matched individual 12345***") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoAfterMatchingFailure() {
        when(mockRestTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenThrow(
                new HttpClientErrorException(FORBIDDEN, "No match", "MATCHING_FAILED".getBytes(Charset.defaultCharset()), null)
        );
        HmrcHateoasClient client = new HmrcHateoasClient(mockRestTemplate, new NinoUtils(), new TraversonFollower(), mockNameNormalizer, "any api version", "http://something.com/anyurl");

        try {
            client.getMatchResource(individual, "");
        } catch (ApplicationExceptions.HmrcNotFoundException e) {
            // Swallowed as not of interest for this test.
        }

        verify(mockAppender, atLeast(1)).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Failed to match individual 12345***") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoForEveryMatchingAttempt() {
        when(mockRestTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenThrow(
                new HttpClientErrorException(FORBIDDEN, "No match", "MATCHING_FAILED".getBytes(Charset.defaultCharset()), null)
        );
        HmrcHateoasClient client = new HmrcHateoasClient(mockRestTemplate, new NinoUtils(), new TraversonFollower(), mockNameNormalizer, "any api version", "http://something.com/anyurl");


        try {
            client.getMatchResource(individual, "");
        } catch (ApplicationExceptions.HmrcNotFoundException e) {
            // Swallowed as not of interest for this test.
        }

        verify(mockAppender, atLeast(1)).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Match attempt 1 of 2") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
        }));

        verify(mockAppender, atLeast(1)).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Match attempt 2 of 2") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldThrowExceptionForHttpUnauthorised() {
        final String baseHmrcUrl = "http://localhost";
        final URI uri = URI.create(baseHmrcUrl + "/individuals/matching/");
        final String hmrcApiVersion = "1";

        HmrcHateoasClient client = new HmrcHateoasClient(mockRestTemplate, new NinoUtils(), new TraversonFollower(), mockNameNormalizer, "any api version", baseHmrcUrl);

        when(mockRestTemplate.exchange(eq(uri), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> {
            client.getMatchResource(
                    new Individual("first", "last", "nino", LocalDate.now()),
                    "ACCESS_TOKEN"
            );
        }).isInstanceOf(ApplicationExceptions.HmrcUnauthorisedException.class);

    }

    @Test(expected = HttpClientErrorException.class)
    public void shouldNotThrowHmrcNotFoundExceptionWhenNot403() {
        NinoUtils anyNinoUtils = new NinoUtils();
        TraversonFollower anyTraversonFollower = new TraversonFollower();
        String anyApiVersion = "any api version";

        when(mockRestTemplate.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenThrow(
                new HttpClientErrorException(NOT_FOUND));

        HmrcHateoasClient client = new HmrcHateoasClient(mockRestTemplate, anyNinoUtils, anyTraversonFollower, mockNameNormalizer, anyApiVersion, "some-resource");

        LocalDate now = LocalDate.now();
        client.getMatchResource(new Individual("somefirstname", "somelastname", "some nino", now), "some access token");
    }

    @Test(expected = ApplicationExceptions.HmrcNotFoundException.class)
    public void shouldThrowHmrcNotFoundExceptionWhenForbiddenFromHmrc() {
        NinoUtils anyNinoUtils = new NinoUtils();
        TraversonFollower anyTraversonFollower = new TraversonFollower();
        String anyApiVersion = "any api version";

        String responseBody = "{\"code\" : \"MATCHING_FAILED\", \"message\" : \"There is no match for the information provided\"}";
        Charset defaultCharset = Charset.defaultCharset();
        HttpClientErrorException exception = new HttpClientErrorException(FORBIDDEN, "", responseBody.getBytes(defaultCharset), defaultCharset);
        when(mockRestTemplate.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenThrow(exception);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRestTemplate, anyNinoUtils, anyTraversonFollower, mockNameNormalizer, anyApiVersion, "some-resource");

        LocalDate now = LocalDate.now();
        client.getMatchResource(new Individual("somefirstname", "somelastname", "some nino", now), "some access token");
    }

    @Test
    public void shouldThrowProxyForbiddenExceptionWhenForbiddenFromProxy() {
        when(mockRestTemplate.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenThrow(new HttpClientErrorException(FORBIDDEN));

        HmrcHateoasClient client = new HmrcHateoasClient(mockRestTemplate, new NinoUtils(), new TraversonFollower(), mockNameNormalizer, "", "some-resource");

        LocalDate now = LocalDate.now();
        Individual testIndividual = new Individual("somefirstname", "somelastname", "some nino", now);

        assertThatThrownBy(() -> client.getMatchResource(testIndividual, "some access token"))
                .isInstanceOf(ApplicationExceptions.ProxyForbiddenException.class);
    }

    @Test
    public void shouldLogInfoBeforePayeRequestSent() {
        // given
        Resource<Object> incomeResource = new Resource<>(new PayeIncome(new Incomes(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), anyString(), any(RestTemplate.class), any())).willReturn(incomeResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRestTemplate, new NinoUtils(), mockTraversonFollower, mockNameNormalizer, "application/json", "http://something.com/anyurl");

        // when
        client.getPayeIncome(LocalDate.of(2018, 8, 1), LocalDate.of(2018, 8, 1), "token", new Link("http://foo.com/bar"));

        // then
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Sending PAYE request to HMRC") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoAfterPayeResponseReceived() {
        // given
        Resource<Object> incomeResource = new Resource<>(new PayeIncome(new Incomes(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), anyString(), any(RestTemplate.class), any())).willReturn(incomeResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRestTemplate, new NinoUtils(), mockTraversonFollower, mockNameNormalizer, "application/json", "http://something.com/anyurl");

        // when
        client.getPayeIncome(LocalDate.of(2018, 8, 1), LocalDate.of(2018, 8, 1), "token", new Link("http://foo.com/bar"));

        // then
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("PAYE response received from HMRC") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoBeforeSelfAssessmentRequestSent() {
        // given
        Resource<Object> saResource = new Resource<>(new SelfEmployments(new TaxReturns(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), anyString(), any(RestTemplate.class), any())).willReturn(saResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRestTemplate, new NinoUtils(), mockTraversonFollower, mockNameNormalizer, "application/json", "http://something.com/anyurl");

        // when
        client.getSelfAssessmentIncome("token", new Link("http://foo.com/bar"));

        // then
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Sending Self Assessment request to HMRC") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoAfterSelfAssessmentResponseReceived() {
        // given
        Resource<Object> saResource = new Resource<>(new SelfEmployments(new TaxReturns(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), anyString(), any(RestTemplate.class), any())).willReturn(saResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRestTemplate, new NinoUtils(), mockTraversonFollower, mockNameNormalizer, "application/json", "http://something.com/anyurl");

        // when
        client.getSelfAssessmentIncome("token", new Link("http://foo.com/bar"));

        // then
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Self Assessment response received from HMRC") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoBeforeEmploymentsRequestSent() {
        // given
        Resource<Object> employmentsResource = new Resource<>(new Employments(new ArrayList<>()), new Link("http://www.foo.com/bar"));
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), anyString(), any(RestTemplate.class), any())).willReturn(employmentsResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRestTemplate, new NinoUtils(), mockTraversonFollower, mockNameNormalizer, "application/json", "http://something.com/anyurl");

        // when
        client.getEmployments(LocalDate.of(2018, 8, 3), LocalDate.of(2018, 8, 3),"token", new Link("http://foo.com/bar"));

        // then
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Sending Employments request to HMRC") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoAfterEmploymentsResponseReceived() {
        // given
        Resource<Object> employmentsResource = new Resource<>(new Employments(new ArrayList<>()), new Link("http://www.foo.com/bar"));
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), anyString(), any(RestTemplate.class), any())).willReturn(employmentsResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRestTemplate, new NinoUtils(), mockTraversonFollower, mockNameNormalizer, "application/json", "http://something.com/anyurl");

        // when
        client.getEmployments(LocalDate.of(2018, 8, 3), LocalDate.of(2018, 8, 3),"token", new Link("http://foo.com/bar"));

        // then
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Employments response received from HMRC") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }



}
