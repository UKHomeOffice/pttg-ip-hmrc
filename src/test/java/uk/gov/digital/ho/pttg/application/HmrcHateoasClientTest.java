package uk.gov.digital.ho.pttg.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.apache.commons.lang3.ArrayUtils;
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
import uk.gov.digital.ho.pttg.application.namematching.*;
import uk.gov.digital.ho.pttg.application.namematching.NameMatchingPerformance.HasAliases;
import uk.gov.digital.ho.pttg.application.namematching.NameMatchingPerformance.HasSpecialCharacters;
import uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator;
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
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
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
    @Mock private NameMatchingPerformance mockNameMatchingPerformance;

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

        client = new HmrcHateoasClient(mockRequestHeaderData,
                                       mockNameNormalizer,
                                       mockHmrcCallWrapper,
                                       mockNameMatchingCandidatesService,
                                       mockNameMatchingPerformance,
                                       "http://something.com/anyurl");
    }

    @After
    public void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(HmrcHateoasClient.class);
        logger.detachAppender(LOG_TEST_APPENDER);
    }

    @Test
    public void shouldLogInfoBeforeMatchingRequestSent() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData,
                                                         mockNameNormalizer,
                                                         mockHmrcCallWrapper,
                                                         mockNameMatchingCandidatesService,
                                                         mockNameMatchingPerformance,
                                                         "http://localhost");

        client.getMatchResource(individual, "");

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        assertThat(findLog(HMRC_MATCHING_REQUEST_SENT).getFormattedMessage())
                .contains("NR123456C", "http://localhost/individuals/matching/");
    }

    @Test
    public void shouldLogInfoAfterMatchingRequestSent() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getMatchResource(individual, "");

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        LoggingEvent loggingEvent = findLog(HMRC_MATCHING_SUCCESS_RECEIVED);

        assertThat(loggingEvent.getFormattedMessage()).contains("NR123456C");

        assertThat(loggingEvent.getArgumentArray())
                .contains(new ObjectAppendingMarker("attempt", 1),
                          new ObjectAppendingMarker("max_attempts", 2))
                .anyMatch(this::isNameMatchingAnalysis);

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

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        List<LoggingEvent> matchFailureEvents = findLogs(HMRC_MATCHING_FAILURE_RECEIVED);
        assertThat(matchFailureEvents).hasSize(2);
        assertThat(matchFailureEvents).allMatch(loggingEvent -> loggingEvent.getFormattedMessage().contains("NR123456C"));
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
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        List<LoggingEvent> matchingAttemptLogs = findLogs(HMRC_MATCHING_ATTEMPTS);
        assertThat(matchingAttemptLogs).hasSize(2)
                                       .anyMatch(loggingEvent -> loggingEvent.getFormattedMessage().contains("1 of 2"))
                                       .anyMatch(loggingEvent -> loggingEvent.getFormattedMessage().contains("2 of 2"));
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
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        assertThat(findLog(HMRC_PAYE_REQUEST_SENT)).isNotNull();
    }

    @Test
    public void shouldLogInfoAfterPayeResponseReceived() {
        Resource<Object> incomeResource = new Resource<>(new PayeIncome(new Incomes(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(incomeResource);

        client.getPayeIncome(LocalDate.of(2018, 8, 1), LocalDate.of(2018, 8, 1), "token", new Link("http://foo.com/bar"));

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        LoggingEvent loggingEvent = findLog(HMRC_PAYE_RESPONSE_RECEIVED);
        assertThat(loggingEvent.getArgumentArray()).anyMatch(this::isRequestDurationLog);
    }

    @Test
    public void shouldLogInfoBeforeSelfAssessmentSelfEmploymentRequestSent() {
        Resource<Object> saResource = new Resource<>(new SelfEmploymentSelfAssessment(new SelfEmploymentTaxReturns(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(saResource);

        client.getSelfAssessmentSelfEmploymentIncome("token", new Link("http://foo.com/bar"));

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        assertThat(findLog(HMRC_SA_REQUEST_SENT)).isNotNull();
    }

    @Test
    public void shouldLogInfoAfterSelfAssessmentSelfEmploymentResponseReceived() {
        Resource<Object> saResource = new Resource<>(new SelfEmploymentSelfAssessment(new SelfEmploymentTaxReturns(new ArrayList<>())), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(saResource);

        client.getSelfAssessmentSelfEmploymentIncome("token", new Link("http://foo.com/bar"));

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        LoggingEvent loggingEvent = findLog(HMRC_SA_RESPONSE_RECEIVED);
        assertThat(loggingEvent.getArgumentArray()).anyMatch(this::isRequestDurationLog);
    }

    @Test
    public void shouldLogInfoBeforeEmploymentsRequestSent() {
        Resource<Object> employmentsResource = new Resource<>(new Employments(new ArrayList<>()), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(employmentsResource);

        client.getEmployments(LocalDate.of(2018, 8, 3), LocalDate.of(2018, 8, 3), "token", new Link("http://foo.com/bar"));

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        assertThat(findLog(HMRC_EMPLOYMENTS_REQUEST_SENT)).isNotNull();
    }

    @Test
    public void shouldLogInfoAfterEmploymentsResponseReceived() {
        Resource<Object> employmentsResource = new Resource<>(new Employments(new ArrayList<>()), new Link("http://www.foo.com/bar"));
        given(mockHmrcCallWrapper.followTraverson(anyString(), anyString(), any())).willReturn(employmentsResource);

        client.getEmployments(LocalDate.of(2018, 8, 3), LocalDate.of(2018, 8, 3), "token", new Link("http://foo.com/bar"));

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        LoggingEvent loggingEvent = findLog(HMRC_EMPLOYMENTS_RESPONSE_RECEIVED);
        assertThat(loggingEvent.getArgumentArray()).anyMatch(this::isRequestDurationLog);
    }

    @Test
    public void shouldLogBeforeGetIndividualResourceRequestSent() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getIndividualResource("token", new Link("http://something.com/anyurl"));

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        assertThat(findLog(HMRC_INDIVIDUAL_REQUEST_SENT).getFormattedMessage())
                .contains("http://something.com/anyurl");
    }

    @Test
    public void shouldLogAfterIndividualResourceResponseReceived() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getIndividualResource("token", new Link("http://something.com/anyurl"));

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        LoggingEvent loggingEvent = findLog(HMRC_INDIVIDUAL_RESPONSE_RECEIVED);
        assertThat(loggingEvent.getArgumentArray()).anyMatch(this::isRequestDurationLog);
    }

    @Test
    public void shouldLogBeforeGetIncomeResourceRequestSent() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getIncomeResource("token", new Link("http://something.com/anyurl"));

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        assertThat(findLog(HMRC_INCOME_REQUEST_SENT).getFormattedMessage())
                .contains("http://something.com/anyurl");
    }

    @Test
    public void shouldLogAfterIncomeResourceResponseReceived() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getIncomeResource("token", new Link("http://something.com/anyurl"));

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        LoggingEvent loggingEvent = findLog(HMRC_INCOME_RESPONSE_RECEIVED);
        assertThat(loggingEvent.getArgumentArray()).anyMatch(this::isRequestDurationLog);
    }

    @Test
    public void shouldLogBeforeGetEmploymentResourceRequestSent() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getEmploymentResource("token", new Link("http://something.com/anyurl"));

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        assertThat(findLog(HMRC_EMPLOYMENTS_REQUEST_SENT).getFormattedMessage())
                .contains("http://something.com/anyurl");
    }

    @Test
    public void shouldLogAfterEmploymentResourceResponseReceived() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getEmploymentResource("token", new Link("http://something.com/anyurl"));

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        LoggingEvent loggingEvent = findLog(HMRC_EMPLOYMENTS_RESPONSE_RECEIVED);
        assertThat(loggingEvent.getArgumentArray()).anyMatch(this::isRequestDurationLog);
    }

    @Test
    public void shouldLogBeforeGetSelfAssessmentResourceRequestSent() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getSelfAssessmentResource("token", "2010-11", "2011-12", new Link("http://something.com/anyurl"));
        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        assertThat(findLog(HMRC_SA_REQUEST_SENT).getFormattedMessage())
                .contains("http://something.com/anyurl?fromTaxYear=2010-11&toTaxYear=2011-12");
    }

    @Test
    public void shouldLogAfterSelAssessmentResourceResponseReceived() {
        given(mockHmrcCallWrapper.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        client.getSelfAssessmentResource("token", "2010-11", "2011-12", new Link("http://something.com/anyurl"));

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        LoggingEvent loggingEvent = findLog(HMRC_SA_RESPONSE_RECEIVED);
        assertThat(loggingEvent.getArgumentArray()).anyMatch(this::isRequestDurationLog);
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
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData,
                                                         nameNormalizer,
                                                         mockHmrcCallWrapper,
                                                         mockNameMatchingCandidatesService,
                                                         mockNameMatchingPerformance,
                                                         "http://something.com/anyurl");

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
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData,
                                                         nameNormalizer,
                                                         mockHmrcCallWrapper,
                                                         mockNameMatchingCandidatesService,
                                                         mockNameMatchingPerformance,
                                                         "http://something.com/anyurl");

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
        HmrcHateoasClient client = new HmrcHateoasClient(mockRequestHeaderData,
                                                         nameNormalizer,
                                                         mockHmrcCallWrapper,
                                                         mockNameMatchingCandidatesService,
                                                         mockNameMatchingPerformance,
                                                         "http://something.com/anyurl");

        CandidateName emptyNameCandidate = new CandidateName("Bob John", "");
        given(mockNameMatchingCandidatesService.generateCandidateNames(anyString(), anyString(), anyString())).willReturn(singletonList(emptyNameCandidate));

        try {
            client.getMatchResource(individual, "any access token");
        } catch (ApplicationExceptions.HmrcNotFoundException e) {
            // Swallow exception that is not of interest to this test
        }

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        LoggingEvent loggingEvent = findLog(HMRC_MATCHING_ATTEMPT_SKIPPED);
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(loggingEvent.getFormattedMessage()).contains("Normalized name contains a blank name");
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

    @Test
    public void getMatchResource_noMatch_logUnsuccessful() {
        given(mockHmrcCallWrapper.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new ApplicationExceptions.HmrcNotFoundException(""));

        try {
            client.getMatchResource(individual, "any access token");
        } catch (ApplicationExceptions.HmrcNotFoundException e) {
            // swallowed as not of interest to test
        }

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        LoggingEvent loggingEvent = findLog(HMRC_MATCHING_UNSUCCESSFUL);

        assertThat(loggingEvent.getFormattedMessage()).contains("NR123456C");
        assertThat(loggingEvent.getArgumentArray())
                .contains(new ObjectAppendingMarker("max_attempts", 2));
    }

    @Test
    @SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void getMatchResource_matched_callsNameMatchingPerformance() {
        given(mockHmrcCallWrapper.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);

        InputNames someInputNames = new InputNames("some name", "some name");
        given(mockNameMatchingCandidatesService.generateCandidateNames(anyString(), anyString(), anyString()))
                .willReturn(singletonList(new CandidateName("anyname", "anyname", new CandidateDerivation(someInputNames, anyGenerators(), anyNameDerivation(), anyNameDerivation()))));

        client.getMatchResource(individual, "any access token");

        then(mockNameMatchingPerformance).should().hasAliases(someInputNames);
        then(mockNameMatchingPerformance).should().hasSpecialCharacters(someInputNames);
    }

    @Test
    public void getMatchResource_matched_logPerformance() {
        given(mockHmrcCallWrapper.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).willReturn(mockResponse);
        given(mockNameMatchingPerformance.hasAliases(any()))
                .willReturn(HasAliases.HAS_ALIASES);
        given(mockNameMatchingPerformance.hasSpecialCharacters(any()))
                .willReturn(HasSpecialCharacters.FIRST_ONLY);

        client.getMatchResource(individual, "any access token");

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        LoggingEvent loggingEvent = findLog(HMRC_MATCHING_SUCCESS_RECEIVED);
        assertThat(loggingEvent.getArgumentArray())
                .contains(new ObjectAppendingMarker("has_aliases", HasAliases.HAS_ALIASES),
                          new ObjectAppendingMarker("special_characters", HasSpecialCharacters.FIRST_ONLY));
    }

    @Test
    @SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void getMatchResource_noMatch_callsNameMatchingPerformance() {
        given(mockHmrcCallWrapper.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new ApplicationExceptions.HmrcNotFoundException(""));

        InputNames someInputNames = new InputNames("some name", "some name");
        given(mockNameMatchingCandidatesService.generateCandidateNames(anyString(), anyString(), anyString()))
                .willReturn(singletonList(new CandidateName("anyname", "anyname", new CandidateDerivation(someInputNames, anyGenerators(), anyNameDerivation(), anyNameDerivation()))));

        try {
            client.getMatchResource(individual, "any access token");
        } catch (ApplicationExceptions.HmrcNotFoundException e) {
            // swallowed as not of interest to test
        }

        then(mockNameMatchingPerformance).should().hasAliases(someInputNames);
        then(mockNameMatchingPerformance).should().hasSpecialCharacters(someInputNames);
    }
    @Test
    public void getMatchResource_noMatch_logPerformance() {
        given(mockHmrcCallWrapper.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new ApplicationExceptions.HmrcNotFoundException(""));
        given(mockNameMatchingPerformance.hasAliases(any()))
                .willReturn(HasAliases.HAS_ALIASES);
        given(mockNameMatchingPerformance.hasSpecialCharacters(any()))
                .willReturn(HasSpecialCharacters.FIRST_ONLY);

        try {
            client.getMatchResource(individual, "any access token");
        } catch (ApplicationExceptions.HmrcNotFoundException e) {
            // swallowed as not of interest to test
        }

        then(mockAppender)
                .should(atLeastOnce())
                .doAppend(logArgumentCaptor.capture());

        LoggingEvent loggingEvent = findLog(HMRC_MATCHING_UNSUCCESSFUL);
        assertThat(loggingEvent.getArgumentArray())
                .contains(new ObjectAppendingMarker("has_aliases", HasAliases.HAS_ALIASES),
                          new ObjectAppendingMarker("special_characters", HasSpecialCharacters.FIRST_ONLY));
    }

    private LoggingEvent findLog(LogEvent logEvent) {
        List<LoggingEvent> loggingEvents = findLogs(logEvent);
        if (loggingEvents.isEmpty()) {
            fail("No log captured with event=" + logEvent);
        }
        if (loggingEvents.size() > 1) {
            fail("Matched multiple logs with event=" + logEvent);
        }
        return loggingEvents.get(0);
    }

    private List<LoggingEvent> findLogs(LogEvent logEvent) {
        return logArgumentCaptor.getAllValues().stream()
                                .filter(loggingEvent -> ArrayUtils.contains(loggingEvent.getArgumentArray(), new ObjectAppendingMarker(EVENT, logEvent)))
                                .collect(Collectors.toList());
    }

    private boolean isNameMatchingAnalysis(Object logArgument) {
        return logArgument instanceof ObjectAppendingMarker && ((ObjectAppendingMarker) logArgument).getFieldName().equals("name-matching-analysis");
    }

    private boolean isRequestDurationLog(Object logArgument) {
        return logArgument instanceof ObjectAppendingMarker && ((ObjectAppendingMarker) logArgument).getFieldName().equals("request_duration_ms");
    }

    private List<NameMatchingCandidateGenerator.Generator> anyGenerators() {
        return emptyList();
    }

    private NameDerivation anyNameDerivation() {
        return new NameDerivation(new Name(Optional.empty(), NameType.FIRST, 0, "any name"));
    }
}
