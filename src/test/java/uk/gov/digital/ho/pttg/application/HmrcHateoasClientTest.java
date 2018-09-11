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
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;
import uk.gov.digital.ho.pttg.application.util.NameNormalizer;
import uk.gov.digital.ho.pttg.dto.*;
import uk.gov.digital.ho.pttg.dto.selfemployment.SelfAssessment;
import uk.gov.digital.ho.pttg.dto.selfemployment.TaxReturns;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RunWith(MockitoJUnitRunner.class)
public class HmrcHateoasClientTest {

    @Mock private HmrcCallWrapper mockHmrcCallWrapper;
    @Mock private NameNormalizer mockNameNormalizer;
    @Mock private ResponseEntity mockResponse;
    @Mock private Appender<ILoggingEvent> mockAppender;
    @Mock private RequestHeaderData mockRequestHeaderData;

    private final Individual individual = new Individual("John", "Smith", "NR123456C", LocalDate.of(2018, 7, 30));

    @Before
    public void setup() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(HmrcHateoasClient.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
    }

    @Test
    public void shouldLogInfoBeforeMatchingRequestSent() {
        when(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(mockResponse);
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, "http://localhost");

        client.getMatchResource(individual, "");

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Match Individual NR123456C via a POST to http://localhost/individuals/matching/") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoAfterMatchingRequestSent() {
        when(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(mockResponse);
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, "http://something.com/anyurl");

        client.getMatchResource(individual, "");

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Successfully matched individual NR123456C") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoAfterMatchingFailure() {
        when(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new ApplicationExceptions.HmrcNotFoundException(""));
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, "http://something.com/anyurl");

        try {
            client.getMatchResource(individual, "");
        } catch (ApplicationExceptions.HmrcNotFoundException e) {
            // Swallowed as not of interest for this test.
        }

        verify(mockAppender, atLeast(1)).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Failed to match individual NR123456C") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoForEveryMatchingAttempt() {
        when(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new ApplicationExceptions.HmrcNotFoundException(""));
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, "http://something.com/anyurl");


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

    @Test(expected = HttpClientErrorException.class)
    public void shouldNotThrowHmrcNotFoundExceptionWhenNot403() {
        String anyApiVersion = "any api version";

        when(mockHmrcCallWrapper.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenThrow(
                new HttpClientErrorException(NOT_FOUND));

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, "some-resource");

        LocalDate now = LocalDate.now();
        client.getMatchResource(new Individual("somefirstname", "somelastname", "some nino", now), "some access token");
    }

    @Test(expected = ApplicationExceptions.HmrcNotFoundException.class)
    public void shouldThrowHmrcNotFoundExceptionWhenForbiddenFromHmrc() {
        String anyApiVersion = "any api version";

        when(mockHmrcCallWrapper.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new ApplicationExceptions.HmrcNotFoundException(""));

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, "some-resource");

        LocalDate now = LocalDate.now();
        client.getMatchResource(new Individual("somefirstname", "somelastname", "some nino", now), "some access token");
    }

    @Test
    public void shouldLogInfoBeforePayeRequestSent() {
        // given
        Resource<Object> incomeResource = new Resource<>(new PayeIncome(new Incomes(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(incomeResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, "http://something.com/anyurl");

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
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(incomeResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, "http://something.com/anyurl");

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
        Resource<Object> saResource = new Resource<>(new SelfAssessment(new TaxReturns(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(saResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, "http://something.com/anyurl");

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
        Resource<Object> saResource = new Resource<>(new SelfAssessment(new TaxReturns(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(saResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, "http://something.com/anyurl");

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
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(employmentsResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, "http://something.com/anyurl");

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
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(employmentsResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, "http://something.com/anyurl");

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
