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
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcOverRateLimitException;
import uk.gov.digital.ho.pttg.application.domain.Individual;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.NameMatchingCandidatesService;
import uk.gov.digital.ho.pttg.application.util.namenormalizer.InvalidCharacterNameNormalizer;
import uk.gov.digital.ho.pttg.application.util.namenormalizer.NameNormalizer;
import uk.gov.digital.ho.pttg.dto.*;
import uk.gov.digital.ho.pttg.dto.saselfemployment.SelfEmployment;
import uk.gov.digital.ho.pttg.dto.saselfemployment.SelfEmploymentSelfAssessment;
import uk.gov.digital.ho.pttg.dto.saselfemployment.SelfEmploymentTaxReturn;
import uk.gov.digital.ho.pttg.dto.saselfemployment.SelfEmploymentTaxReturns;
import uk.gov.digital.ho.pttg.dto.sasummary.Summary;
import uk.gov.digital.ho.pttg.dto.sasummary.SummarySelfAssessment;
import uk.gov.digital.ho.pttg.dto.sasummary.SummaryTaxReturn;
import uk.gov.digital.ho.pttg.dto.sasummary.SummaryTaxReturns;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private static final LocalDate SOME_DOB = LocalDate.now();

    @Mock private HmrcCallWrapper mockHmrcCallWrapper;
    @Mock private NameNormalizer mockNameNormalizer;
    @Mock private ResponseEntity mockResponse;
    @Mock private Appender<ILoggingEvent> mockAppender;
    @Mock private RequestHeaderData mockRequestHeaderData;
    @Mock private NameMatchingCandidatesService mockNameMatchingCandidatesService;

    private final Individual individual = new Individual("John", "Smith", "NR123456C", LocalDate.of(2018, 7, 30), "");
    private final HmrcIndividual individualForMatching = new HmrcIndividual("John", "Smith", "NR123456C", LocalDate.of(2018, 7, 30));

    @Before
    public void setup() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(HmrcHateoasClient.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
        when(mockNameNormalizer.normalizeNames(any(HmrcIndividual.class))).thenReturn(individualForMatching);
        List<CandidateName> defaultCandidateNames = Arrays.asList(new CandidateName("somefirstname", "somelastname"), new CandidateName("somelastname", "somefirstname"));
        when(mockNameMatchingCandidatesService.generateCandidateNames(anyString(), anyString(), anyString())).thenReturn(defaultCandidateNames);
    }

    @Test
    public void shouldLogInfoBeforeMatchingRequestSent() {
        when(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(mockResponse);
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://localhost");

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
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");

        client.getMatchResource(individual, "");

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Successfully matched individual NR123456C") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("combination") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("name-matching-analysis") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[3]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoAfterMatchingFailure() {
        when(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new ApplicationExceptions.HmrcNotFoundException(""));
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService,  "http://something.com/anyurl");

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
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService,  "http://something.com/anyurl");


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

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "some-resource");

        LocalDate now = LocalDate.now();
        client.getMatchResource(new Individual("somefirstname", "somelastname", "some nino", now, ""), "some access token");
    }

    @Test(expected = ApplicationExceptions.HmrcNotFoundException.class)
    public void shouldThrowHmrcNotFoundExceptionWhenForbiddenFromHmrc() {
        String anyApiVersion = "any api version";

        when(mockHmrcCallWrapper.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new ApplicationExceptions.HmrcNotFoundException(""));

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "some-resource");

        LocalDate now = LocalDate.now();
        client.getMatchResource(new Individual("somefirstname", "somelastname", "some nino", now, ""), "some access token");
    }

    @Test
    public void shouldLogInfoBeforePayeRequestSent() {
        // given
        Resource<Object> incomeResource = new Resource<>(new PayeIncome(new Incomes(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(incomeResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");

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

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");

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
    public void shouldLogInfoBeforeSelfAssessmentSelfEmploymentRequestSent() {
        // given
        Resource<Object> saResource = new Resource<>(new SelfEmploymentSelfAssessment(new SelfEmploymentTaxReturns(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(saResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");

        // when
        client.getSelfAssessmentSelfEmploymentIncome("token", new Link("http://foo.com/bar"));

        // then
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Sending Self Assessment self employment request to HMRC") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoAfterSelfAssessmentSelfEmploymentResponseReceived() {
        // given
        Resource<Object> saResource = new Resource<>(new SelfEmploymentSelfAssessment(new SelfEmploymentTaxReturns(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(saResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");

        // when
        client.getSelfAssessmentSelfEmploymentIncome("token", new Link("http://foo.com/bar"));

        // then
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Self Assessment self employment response received from HMRC") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoBeforeSelfAssessmentSummaryRequestSent() {
        // given
        Resource<Object> saResource = new Resource<>(new SummarySelfAssessment(new SummaryTaxReturns(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(saResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");

        // when
        client.getSelfAssessmentSummaryIncome("token", new Link("http://foo.com/bar"));

        // then
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Sending Self Assessment Summary request to HMRC") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoAfterSelfAssessmentSummaryResponseReceived() {
        // given
        Resource<Object> saResource = new Resource<>(new SummarySelfAssessment(new SummaryTaxReturns(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(saResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");

        // when
        client.getSelfAssessmentSummaryIncome("token", new Link("http://foo.com/bar"));

        // then
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Self Assessment summary response received from HMRC") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoBeforeEmploymentsRequestSent() {
        // given
        Resource<Object> employmentsResource = new Resource<>(new Employments(new ArrayList<>()), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(employmentsResource);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");

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

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");

        // when
        client.getEmployments(LocalDate.of(2018, 8, 3), LocalDate.of(2018, 8, 3),"token", new Link("http://foo.com/bar"));

        // then
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Employments response received from HMRC") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void groupSummarySelfAssessments() {
        List<Summary> summaries1 = Arrays.asList(new Summary(new BigDecimal("1.00")), new Summary(new BigDecimal("2.00")));
        List<Summary> summaries2 = Arrays.asList(new Summary(new BigDecimal("3.00")), new Summary(new BigDecimal("4.00")));
        List<SummaryTaxReturn> summaryTaxReturns =
                Arrays.asList(
                    new SummaryTaxReturn("2015-16", summaries1),
                    new SummaryTaxReturn("2016-17", summaries2)
                );

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");

        List<AnnualSelfAssessmentTaxReturn> saTaxReturns = client.groupSummaries(summaryTaxReturns);

        assertThat(saTaxReturns.size()).isEqualTo(2);
        assertThat(saTaxReturns.get(0).getTaxYear()).isEqualTo("2015-16");
        assertThat(saTaxReturns.get(0).getSummaryIncome()).isEqualTo(new BigDecimal("3.00"));
        assertThat(saTaxReturns.get(0).getSelfEmploymentProfit()).isEqualTo(new BigDecimal("0"));
        assertThat(saTaxReturns.get(1).getTaxYear()).isEqualTo("2016-17");
        assertThat(saTaxReturns.get(1).getSummaryIncome()).isEqualTo(new BigDecimal("7.00"));
        assertThat(saTaxReturns.get(1).getSelfEmploymentProfit()).isEqualTo(new BigDecimal("0"));
    }

    @Test
    public void groupSelfEmploymentSelfAssessments() {
        List<SelfEmployment> summaries1 = Arrays.asList(new SelfEmployment(new BigDecimal("1.00")), new SelfEmployment(new BigDecimal("2.00")));
        List<SelfEmployment> summaries2 = Arrays.asList(new SelfEmployment(new BigDecimal("3.00")), new SelfEmployment(new BigDecimal("4.00")));
        List<SelfEmploymentTaxReturn> selfEmploymentTaxReturns =
                Arrays.asList(
                        new SelfEmploymentTaxReturn("2015-16", summaries1),
                        new SelfEmploymentTaxReturn("2016-17", summaries2)
                );

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");

        List<AnnualSelfAssessmentTaxReturn> saTaxReturns = client.groupSelfEmploymentIncomes(selfEmploymentTaxReturns);

        assertThat(saTaxReturns.size()).isEqualTo(2);
        assertThat(saTaxReturns.get(0).getTaxYear()).isEqualTo("2015-16");
        assertThat(saTaxReturns.get(0).getSelfEmploymentProfit()).isEqualTo(new BigDecimal("3.00"));
        assertThat(saTaxReturns.get(0).getSummaryIncome()).isEqualTo(new BigDecimal("0"));
        assertThat(saTaxReturns.get(1).getTaxYear()).isEqualTo("2016-17");
        assertThat(saTaxReturns.get(1).getSelfEmploymentProfit()).isEqualTo(new BigDecimal("7.00"));
        assertThat(saTaxReturns.get(1).getSummaryIncome()).isEqualTo(new BigDecimal("0"));
    }

    @Test
    public void shouldNotCallHmrcWithEmptyFirstName() {
        NameNormalizer nameNormalizer = new InvalidCharacterNameNormalizer();
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, nameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");

        CandidateName emptyNameCandidate = new CandidateName("", "Smith Jones");
        when(mockNameMatchingCandidatesService.generateCandidateNames(anyString(), anyString(), anyString())).thenReturn(singletonList(emptyNameCandidate));

        try {
            client.getMatchResource(individual, "any access token");
        } catch (ApplicationExceptions.HmrcNotFoundException e) {
            // Swallow exception that is not of interest to this test
        }

        verify(mockHmrcCallWrapper, never()).exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));

    }

    @Test
    public void shouldNotCallHmrcWithEmptyLastName() {
        NameNormalizer nameNormalizer = new InvalidCharacterNameNormalizer();
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, nameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");

        CandidateName emptyNameCandidate = new CandidateName("Bob John", "");
        when(mockNameMatchingCandidatesService.generateCandidateNames(anyString(), anyString(), anyString())).thenReturn(singletonList(emptyNameCandidate));


        try {
            client.getMatchResource(individual, "any access token");
        } catch (ApplicationExceptions.HmrcNotFoundException e) {
            // Swallow exception that is not of interest to this test
        }

        verify(mockHmrcCallWrapper, never()).exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void shouldLogSkippedCallToHmrcDueToEmptyName() {
        NameNormalizer nameNormalizer = new InvalidCharacterNameNormalizer();
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, nameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");

        CandidateName emptyNameCandidate = new CandidateName("Bob John", "");
        when(mockNameMatchingCandidatesService.generateCandidateNames(anyString(), anyString(), anyString())).thenReturn(singletonList(emptyNameCandidate));

        try {
            client.getMatchResource(individual, "any access token");
        } catch (ApplicationExceptions.HmrcNotFoundException e) {
            // Swallow exception that is not of interest to this test
        }

        verify(mockAppender, atLeastOnce()).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Skipped HMRC call due to Invalid Identity: Normalized name contains a blank name") &&
                    loggingEvent.getLevel().equals(Level.INFO) &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldPassAliasSurnamesToCandidateGenerator() {
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://localhost");
        when(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(mockResponse);

        Individual individual = new Individual("some first names", "some last names", "some nino", SOME_DOB, "some alias surnames");
        client.getMatchResource(individual, "");

        verify(mockNameMatchingCandidatesService).generateCandidateNames("some first names", "some last names", "some alias surnames");
    }

    @Test
    public void getMatchResource_hmrcOverRateLimitException_notCaught() {
        HmrcOverRateLimitException hmrcOverRateLimitException = new HmrcOverRateLimitException("some message");
        when(mockHmrcCallWrapper.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenThrow(hmrcOverRateLimitException);

        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://localhost");

        assertThatThrownBy(() -> client.getMatchResource(individual, "some access token"))
                .isEqualTo(hmrcOverRateLimitException);
    }
}