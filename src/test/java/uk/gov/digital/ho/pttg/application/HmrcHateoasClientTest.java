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
import org.mockito.ArgumentCaptor;
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

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@RunWith(MockitoJUnitRunner.class)
public class HmrcHateoasClientTest {

    private static final LocalDate SOME_DOB = LocalDate.now();
    private static final String LOG_TEST_APPENDER = "tester";

    @Mock private HmrcCallWrapper mockHmrcCallWrapper;
    @Mock private NameNormalizer mockNameNormalizer;
    @Mock private ResponseEntity mockResponse;
    @Mock private Appender<ILoggingEvent> mockAppender;
    @Mock private RequestHeaderData mockRequestHeaderData;
    @Mock private NameMatchingCandidatesService mockNameMatchingCandidatesService;

    private final Individual individual = new Individual("John", "Smith", "NR123456C", LocalDate.of(2018, 7, 30), "");
    private final HmrcIndividual individualForMatching = new HmrcIndividual("John", "Smith", "NR123456C", LocalDate.of(2018, 7, 30));
    private HmrcHateoasClient client;

    private ArgumentCaptor<LoggingEvent> logArgumentCaptor;

    @Before
    public void setup() {
        mockAppender.setName(LOG_TEST_APPENDER);
        Logger logger = (Logger) LoggerFactory.getLogger(HmrcHateoasClient.class);
        logger.setLevel(Level.INFO);
        logger.addAppender(mockAppender);
        logArgumentCaptor = ArgumentCaptor.forClass(LoggingEvent.class);

        when(mockNameNormalizer.normalizeNames(any(HmrcIndividual.class))).thenReturn(individualForMatching);
        List<CandidateName> defaultCandidateNames = Arrays.asList(new CandidateName("somefirstname", "somelastname"), new CandidateName("somelastname", "somefirstname"));
        when(mockNameMatchingCandidatesService.generateCandidateNames(anyString(), anyString(), anyString())).thenReturn(defaultCandidateNames);

        client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");
    }

    @After
    public void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(HmrcHateoasClient.class);
        logger.detachAppender(LOG_TEST_APPENDER);
    }

    @Test
    public void shouldLogInfoBeforeMatchingRequestSent() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://localhost");

        client.getMatchResource(individual, "");

        then(mockAppender)
                .should()
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("Match Individual NR123456C via a POST to http://localhost/individuals/matching/") &&
                            loggingEvent.getArgumentArray()[2].equals(new ObjectAppendingMarker("event_id", HMRC_MATCHING_REQUEST_SENT));
                }));
    }

    @Test
    public void shouldLogInfoAfterMatchingRequestSent() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getMatchResource(individual, "");

        then(mockAppender).should(atLeastOnce())
                          .doAppend(logArgumentCaptor.capture());

        LoggingEvent loggingEvent = logArgumentCaptor.getValue();

        assertThat(loggingEvent.getFormattedMessage()).isEqualTo("Successfully matched individual NR123456C");
        assertThat(loggingEvent.getArgumentArray())
                .contains(new ObjectAppendingMarker("combination", "1 of 2"),
                          new ObjectAppendingMarker("event_id", HMRC_MATCHING_SUCCESS_RECEIVED));
        assertThat(loggingEvent.getArgumentArray()).anyMatch(this::isNameMatchingAnalysis);

    }

    @Test
    public void shouldLogInfoAfterMatchingFailure() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new ApplicationExceptions.HmrcNotFoundException(""));

        try {
            client.getMatchResource(individual, "");
        } catch (ApplicationExceptions.HmrcNotFoundException e) {
            // Swallowed as not of interest for this test.
        }

        then(mockAppender).should(atLeastOnce())
                          .doAppend(logArgumentCaptor.capture());

        LoggingEvent loggingEvent = findLog("Failed to match individual NR123456C");

        assertThat(loggingEvent.getArgumentArray())
                .contains(new ObjectAppendingMarker("event_id", HMRC_MATCHING_FAILURE_RECEIVED));
    }

    @Test
    public void shouldLogInfoForEveryMatchingAttempt() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new ApplicationExceptions.HmrcNotFoundException(""));

        try {
            client.getMatchResource(individual, "");
        } catch (ApplicationExceptions.HmrcNotFoundException e) {
            // Swallowed as not of interest for this test.
        }

        then(mockAppender)
                .should(atLeast(1))
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("Match attempt 1 of 2") &&
                            ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
                }));

        then(mockAppender)
                .should(atLeast(1))
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("Match attempt 2 of 2") &&
                            ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
                }));
    }

    @Test(expected = HttpClientErrorException.class)
    public void shouldNotThrowHmrcNotFoundExceptionWhenNot403() {

        given(mockHmrcCallWrapper.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(NOT_FOUND));

        LocalDate now = LocalDate.now();
        client.getMatchResource(new Individual("somefirstname", "somelastname", "some nino", now, ""), "some access token");
    }

    @Test(expected = ApplicationExceptions.HmrcNotFoundException.class)
    public void shouldThrowHmrcNotFoundExceptionWhenForbiddenFromHmrc() {

        given(mockHmrcCallWrapper.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new ApplicationExceptions.HmrcNotFoundException(""));

        LocalDate now = LocalDate.now();
        client.getMatchResource(new Individual("somefirstname", "somelastname", "some nino", now, ""), "some access token");
    }

    @Test
    public void shouldLogInfoBeforePayeRequestSent() {
        Resource<Object> incomeResource = new Resource<>(new PayeIncome(new Incomes(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(incomeResource);

        client.getPayeIncome(LocalDate.of(2018, 8, 1), LocalDate.of(2018, 8, 1), "token", new Link("http://foo.com/bar"));

        then(mockAppender)
                .should().
                doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("Sending PAYE request to HMRC") &&
                            (loggingEvent.getArgumentArray()[0]).equals(new ObjectAppendingMarker(EVENT, HMRC_PAYE_REQUEST_SENT));
                }));
    }

    @Test
    public void shouldLogInfoAfterPayeResponseReceived() {
        Resource<Object> incomeResource = new Resource<>(new PayeIncome(new Incomes(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(incomeResource);

        client.getPayeIncome(LocalDate.of(2018, 8, 1), LocalDate.of(2018, 8, 1), "token", new Link("http://foo.com/bar"));

        then(mockAppender)
                .should()
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("PAYE response received from HMRC") &&
                            (loggingEvent.getArgumentArray()[0]).equals(new ObjectAppendingMarker(EVENT, HMRC_PAYE_RESPONSE_RECEIVED)) &&
                            ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("request_duration_ms");
                }));
    }

    @Test
    public void shouldLogInfoBeforeSelfAssessmentSelfEmploymentRequestSent() {
        Resource<Object> saResource = new Resource<>(new SelfEmploymentSelfAssessment(new SelfEmploymentTaxReturns(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(saResource);

        client.getSelfAssessmentSelfEmploymentIncome("token", new Link("http://foo.com/bar"));

        then(mockAppender)
                .should()
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("Sending Self Assessment self employment request to HMRC") &&
                            (loggingEvent.getArgumentArray()[0]).equals(new ObjectAppendingMarker(EVENT, HMRC_SA_REQUEST_SENT));
                }));
    }

    @Test
    public void shouldLogInfoAfterSelfAssessmentSelfEmploymentResponseReceived() {
        Resource<Object> saResource = new Resource<>(new SelfEmploymentSelfAssessment(new SelfEmploymentTaxReturns(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(saResource);

        client.getSelfAssessmentSelfEmploymentIncome("token", new Link("http://foo.com/bar"));

        then(mockAppender)
                .should()
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("Self Assessment self employment response received from HMRC") &&
                            (loggingEvent.getArgumentArray()[0]).equals(new ObjectAppendingMarker(EVENT, HMRC_SA_RESPONSE_RECEIVED)) &&
                            ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("request_duration_ms");
                }));
    }

    @Test
    public void shouldLogInfoBeforeEmploymentsRequestSent() {
        Resource<Object> employmentsResource = new Resource<>(new Employments(new ArrayList<>()), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(employmentsResource);

        client.getEmployments(LocalDate.of(2018, 8, 3), LocalDate.of(2018, 8, 3), "token", new Link("http://foo.com/bar"));

        then(mockAppender)
                .should()
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("Sending Employments request to HMRC") &&
                            (loggingEvent.getArgumentArray()[0]).equals(new ObjectAppendingMarker(EVENT, HMRC_EMPLOYMENTS_REQUEST_SENT));
                }));
    }

    @Test
    public void shouldLogInfoAfterEmploymentsResponseReceived() {
        Resource<Object> employmentsResource = new Resource<>(new Employments(new ArrayList<>()), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(employmentsResource);

        client.getEmployments(LocalDate.of(2018, 8, 3), LocalDate.of(2018, 8, 3), "token", new Link("http://foo.com/bar"));

        then(mockAppender)
                .should()
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("Employments response received from HMRC") &&
                            (loggingEvent.getArgumentArray()[0]).equals(new ObjectAppendingMarker(EVENT, HMRC_EMPLOYMENTS_RESPONSE_RECEIVED)) &&
                            ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("request_duration_ms");
                }));
    }

    @Test
    public void shouldLogBeforeGetIndividualResourceRequestSent() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getIndividualResource("token", new Link("http://something.com/anyurl"));

        then(mockAppender)
                .should()
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("About to GET individual resource from HMRC at http://something.com/anyurl") &&
                            (loggingEvent.getArgumentArray()[1]).equals(new ObjectAppendingMarker(EVENT, HMRC_INDIVIDUAL_REQUEST_SENT));
                }));
    }

    @Test
    public void shouldLogAfterIndividualResourceResponseReceived() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getIndividualResource("token", new Link("http://something.com/anyurl"));

        then(mockAppender)
                .should()
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("Individual resource response received") &&
                            (loggingEvent.getArgumentArray()[0]).equals(new ObjectAppendingMarker(EVENT, HMRC_INDIVIDUAL_RESPONSE_RECEIVED)) &&
                            ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("request_duration_ms");
                }));
    }

    @Test
    public void shouldLogBeforeGetIncomeResourceRequestSent() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getIncomeResource("token", new Link("http://something.com/anyurl"));

        then(mockAppender)
                .should()
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("About to GET income resource from HMRC at http://something.com/anyurl") &&
                            (loggingEvent.getArgumentArray()[1]).equals(new ObjectAppendingMarker(EVENT, HMRC_INCOME_REQUEST_SENT));
                }));
    }

    @Test
    public void shouldLogAfterIncomeResourceResponseReceived() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getIncomeResource("token", new Link("http://something.com/anyurl"));

        then(mockAppender)
                .should()
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("Income resource response received") &&
                            (loggingEvent.getArgumentArray()[0]).equals(new ObjectAppendingMarker(EVENT, HMRC_INCOME_RESPONSE_RECEIVED)) &&
                            ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("request_duration_ms");
                }));
    }

    @Test
    public void shouldLogBeforeGetEmploymentResourceRequestSent() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getEmploymentResource("token", new Link("http://something.com/anyurl"));

        then(mockAppender)
                .should()
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("About to GET employment resource from HMRC at http://something.com/anyurl") &&
                            (loggingEvent.getArgumentArray()[1]).equals(new ObjectAppendingMarker(EVENT, HMRC_EMPLOYMENTS_REQUEST_SENT));
                }));
    }

    @Test
    public void shouldLogAfterEmploymentResourceResponseReceived() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getEmploymentResource("token", new Link("http://something.com/anyurl"));

        then(mockAppender)
                .should()
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("Employment resource response received") &&
                            (loggingEvent.getArgumentArray()[0]).equals(new ObjectAppendingMarker(EVENT, HMRC_EMPLOYMENTS_RESPONSE_RECEIVED)) &&
                            ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("request_duration_ms");
                }));
    }

    @Test
    public void shouldLogBeforeGetSelfAssessmentResourceRequestSent() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getSelfAssessmentResource("token", "2010-11", "2011-12", new Link("http://something.com/anyurl"));

        then(mockAppender)
                .should()
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("About to get self assessment resource from HMRC at http://something.com/anyurl?fromTaxYear=2010-11&toTaxYear=2011-12") &&
                            (loggingEvent.getArgumentArray()[1]).equals(new ObjectAppendingMarker(EVENT, HMRC_SA_REQUEST_SENT));
                }));
    }

    @Test
    public void shouldLogAfterSelAssessmentResourceResponseReceived() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getSelfAssessmentResource("token", "2010-11", "2011-12", new Link("http://something.com/anyurl"));

        then(mockAppender)
                .should()
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("Self assessment resource response received") &&
                            (loggingEvent.getArgumentArray()[0]).equals(new ObjectAppendingMarker(EVENT, HMRC_SA_RESPONSE_RECEIVED)) &&
                            ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("request_duration_ms");
                }));
    }

    @Test
    public void getSelfAssessmentResource_responseFromCallWrapper_returnedToCaller() {
        ResponseEntity someResponse = new ResponseEntity<>(new Resource<>("some response"), OK);
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(someResponse);

        Resource<String> returnedResponse = client.getSelfAssessmentResource("token", "2010-11", "2011-12", new Link("http://something.com/anyurl"));

        assertThat(returnedResponse.getContent()).isEqualTo("some response");
    }

    @Test
    public void getSelfAssessmentResource_nullLink_returnEmptyResource() {
        Resource<String> returnedResponse = client.getSelfAssessmentResource("token", "2010-11", "2011-12", null);

        assertThat(returnedResponse.getContent()).isEqualTo("");
        assertThat(returnedResponse.getLinks()).isEmpty();
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
        given(mockNameMatchingCandidatesService.generateCandidateNames(anyString(), anyString(), anyString())).willReturn(singletonList(emptyNameCandidate));

        try {
            client.getMatchResource(individual, "any access token");
        } catch (ApplicationExceptions.HmrcNotFoundException e) {
            // Swallow exception that is not of interest to this test
        }

        then(mockHmrcCallWrapper)
                .should(never())
                .exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));

    }

    @Test
    public void shouldNotCallHmrcWithEmptyLastName() {
        NameNormalizer nameNormalizer = new InvalidCharacterNameNormalizer();
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, nameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");

        CandidateName emptyNameCandidate = new CandidateName("Bob John", "");
        given(mockNameMatchingCandidatesService.generateCandidateNames(anyString(), anyString(), anyString())).willReturn(singletonList(emptyNameCandidate));


        try {
            client.getMatchResource(individual, "any access token");
        } catch (ApplicationExceptions.HmrcNotFoundException e) {
            // Swallow exception that is not of interest to this test
        }

        then(mockHmrcCallWrapper)
                .should(never())
                .exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void shouldLogSkippedCallToHmrcDueToEmptyName() {
        NameNormalizer nameNormalizer = new InvalidCharacterNameNormalizer();
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData, nameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, "http://something.com/anyurl");

        CandidateName emptyNameCandidate = new CandidateName("Bob John", "");
        given(mockNameMatchingCandidatesService.generateCandidateNames(anyString(), anyString(), anyString())).willReturn(singletonList(emptyNameCandidate));

        try {
            client.getMatchResource(individual, "any access token");
        } catch (ApplicationExceptions.HmrcNotFoundException e) {
            // Swallow exception that is not of interest to this test
        }

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;

                    return loggingEvent.getFormattedMessage().equals("Skipped HMRC call due to Invalid Identity: Normalized name contains a blank name") &&
                            loggingEvent.getLevel().equals(Level.INFO) &&
                            ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
                }));
    }

    @Test
    public void shouldPassAliasSurnamesToCandidateGenerator() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        Individual individual = new Individual("some first names", "some last names", "some nino", SOME_DOB, "some alias surnames");
        client.getMatchResource(individual, "");

        then(mockNameMatchingCandidatesService)
                .should()
                .generateCandidateNames("some first names", "some last names", "some alias surnames");
    }

    @Test
    public void getMatchResource_hmrcOverRateLimitException_notCaught() {
        HmrcOverRateLimitException hmrcOverRateLimitException = new HmrcOverRateLimitException("some message");
        given(mockHmrcCallWrapper.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willThrow(hmrcOverRateLimitException);

        assertThatThrownBy(() -> client.getMatchResource(individual, "some access token"))
                .isEqualTo(hmrcOverRateLimitException);
    }

    private LoggingEvent findLog(String message) {
        Optional<LoggingEvent> matchedEvent = logArgumentCaptor.getAllValues().stream()
                                                               .filter(log -> log.getFormattedMessage().equals(message))
                                                               .findFirst();
        if (!matchedEvent.isPresent()) {
            fail("No caputred logs with message=" + message);
        }
        return matchedEvent.get();
    }

    private boolean isNameMatchingAnalysis(Object logArgument) {
        return logArgument instanceof ObjectAppendingMarker && ((ObjectAppendingMarker) logArgument).getFieldName().equals("name-matching-analysis");
    }
}
