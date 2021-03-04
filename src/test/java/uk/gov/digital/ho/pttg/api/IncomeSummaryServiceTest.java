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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcUnauthorisedException;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.InsufficientTimeException;
import uk.gov.digital.ho.pttg.application.HmrcAccessCodeClient;
import uk.gov.digital.ho.pttg.application.HmrcClient;
import uk.gov.digital.ho.pttg.application.HmrcRetryTemplateFactory;
import uk.gov.digital.ho.pttg.application.IncomeSummaryContext;
import uk.gov.digital.ho.pttg.application.domain.IncomeSummary;
import uk.gov.digital.ho.pttg.application.domain.Individual;
import uk.gov.digital.ho.pttg.application.retry.RetryTemplateBuilder;
import uk.gov.digital.ho.pttg.audit.AuditClient;
import uk.gov.digital.ho.pttg.audit.AuditEventType;
import uk.gov.digital.ho.pttg.audit.AuditIndividualData;

import java.time.LocalDate;
import java.util.UUID;

import static java.time.Month.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_INCOME_REQUEST;

@RunWith(MockitoJUnitRunner.class)
public class IncomeSummaryServiceTest {

    private static final int REAUTHORISING_RETRY_ATTEMPTS = 2;
    private static final int MAX_API_CALL_ATTEMPTS = 5;
    private static final int BACK_OFF_PERIOD = 1;

    private static final String SOME_ACCESS_CODE = "SomeAccessCode";
    private static final LocalDate SOME_FROM_DATE = LocalDate.of(2018, JANUARY, 1);
    private static final LocalDate SOME_TO_DATE = LocalDate.of(2018, MAY, 1);
    private static final Individual SOME_INDIVIDUAL = new Individual("some first name", "some last name", "some nino", LocalDate.now(), "");
    private static final String LOG_TEST_APPENDER = "tester";

    @Mock private HmrcClient mockHmrcClient;
    @Mock private HmrcAccessCodeClient mockAccessCodeClient;
    @Mock private AuditClient mockAuditClient;
    @Mock private IncomeSummary mockIncomeSummary;
    @Mock private Appender<ILoggingEvent> mockAppender;
    @Mock private RequestHeaderData mockRequestHeaderData;

    @Captor private ArgumentCaptor<UUID> eventIdCaptor;
    @Captor private ArgumentCaptor<AuditIndividualData> auditDataCaptor;

    private IncomeSummaryService incomeSummaryService;

    @Before
    public void setUp() {

        RetryTemplate reauthorisingRetryTemplate = new RetryTemplateBuilder(REAUTHORISING_RETRY_ATTEMPTS)
                                                               .retryHmrcUnauthorisedException()
                                                               .build();
        HmrcRetryTemplateFactory hmrcRetryTemplateFactory = new HmrcRetryTemplateFactory(MAX_API_CALL_ATTEMPTS, BACK_OFF_PERIOD);

        incomeSummaryService = new IncomeSummaryService(
                mockHmrcClient,
                mockAccessCodeClient,
                mockAuditClient,
                reauthorisingRetryTemplate,
                REAUTHORISING_RETRY_ATTEMPTS,
                mockRequestHeaderData,
                hmrcRetryTemplateFactory);

        given(mockAccessCodeClient.getAccessCode())
                .willReturn(SOME_ACCESS_CODE);
    }

    @Test
    public void shouldCallCollaborators() {

        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), any(Individual.class), eq(SOME_FROM_DATE), eq(SOME_TO_DATE), any(IncomeSummaryContext.class)))
                .willReturn(mockIncomeSummary);

        incomeSummaryService.getIncomeSummary(SOME_INDIVIDUAL, SOME_FROM_DATE, SOME_TO_DATE);

        then(mockAccessCodeClient).should().getAccessCode();
        then(mockAccessCodeClient).shouldHaveNoMoreInteractions();

        then(mockHmrcClient).should().populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(SOME_FROM_DATE), eq(SOME_TO_DATE), any(IncomeSummaryContext.class));
        then(mockHmrcClient).shouldHaveNoMoreInteractions();

        then(mockAuditClient).should().add(any(AuditEventType.class), any(UUID.class), isNull());
        then(mockAuditClient).shouldHaveNoMoreInteractions();
    }

    @Test
    public void shouldAllowOptionalToDate() {

        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), any(Individual.class), eq(SOME_FROM_DATE), isNull(), any(IncomeSummaryContext.class)))
                .willReturn(mockIncomeSummary);

        incomeSummaryService.getIncomeSummary(SOME_INDIVIDUAL, SOME_FROM_DATE, null);

        then(mockHmrcClient).should().populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(SOME_FROM_DATE), isNull(), any(IncomeSummaryContext.class));
    }

    @Test
    public void shouldNotAuditPII() {

        LocalDate dateOfBirth = LocalDate.of(1990, DECEMBER, 25);
        String firstName = "FirstName";
        String lastName = "LastName";
        String nino = "Nino";
        Individual individual = new Individual(firstName, lastName, nino, dateOfBirth, "");

        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(individual), eq(SOME_FROM_DATE), isNull(), any(IncomeSummaryContext.class)))
                .willReturn(mockIncomeSummary);

        incomeSummaryService.getIncomeSummary(individual, SOME_FROM_DATE, null);

        then(mockAuditClient).should().add(eq(HMRC_INCOME_REQUEST), eventIdCaptor.capture(), auditDataCaptor.capture());

        assertThat(eventIdCaptor.getValue()).isNotNull();

        AuditIndividualData auditData = auditDataCaptor.getValue();

        assertThat(auditData).isNull();
    }

    @Test
    public void shouldRetryAllCollaboratorCallsIfHmrcServiceUnauthorizedStatusResponse() {

        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(SOME_FROM_DATE), eq(SOME_TO_DATE), any(IncomeSummaryContext.class)))
                .willThrow(new HmrcUnauthorisedException("test"))
                .willReturn(mockIncomeSummary);

        IncomeSummary incomeSummary = incomeSummaryService.getIncomeSummary(SOME_INDIVIDUAL, SOME_FROM_DATE, SOME_TO_DATE);

        assertThat(mockIncomeSummary).isEqualTo(incomeSummary);

        then(mockAccessCodeClient).should(times(2)).getAccessCode();
        then(mockAccessCodeClient).should().loadLatestAccessCode();
        then(mockHmrcClient).should(times(2)).populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(SOME_FROM_DATE), eq(SOME_TO_DATE), any(IncomeSummaryContext.class));
        then(mockAuditClient).should(times(2)).add(isA(AuditEventType.class), isA(UUID.class), isNull());
    }

    @Test
    public void shouldThrowExceptionIfUnexpectedExceptionFromHmrcService() {

        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(SOME_FROM_DATE), eq(SOME_TO_DATE), any(IncomeSummaryContext.class)))
                .willThrow(new IllegalArgumentException());

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> incomeSummaryService.getIncomeSummary(SOME_INDIVIDUAL, SOME_FROM_DATE, SOME_TO_DATE));
    }

    @Test
    public void shouldThrowExceptionIfHttpServerErrorException() {

        given(mockAccessCodeClient.getAccessCode())
                .willReturn(SOME_ACCESS_CODE);
        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(SOME_FROM_DATE), eq(SOME_TO_DATE), any(IncomeSummaryContext.class)))
                .willThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));

        assertThatExceptionOfType(HttpServerErrorException.class)
                .isThrownBy(() -> incomeSummaryService.getIncomeSummary(SOME_INDIVIDUAL, SOME_FROM_DATE, SOME_TO_DATE));
    }

    @Test
    public void shouldThrowExceptionIfOtherHttpClientErrorException() {

        given(mockAccessCodeClient.getAccessCode())
                .willReturn(SOME_ACCESS_CODE);
        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(SOME_FROM_DATE), eq(SOME_TO_DATE), any(IncomeSummaryContext.class)))
                .willThrow(new HttpClientErrorException(BAD_REQUEST));

        assertThatExceptionOfType(HttpClientErrorException.class)
                .isThrownBy(() -> incomeSummaryService.getIncomeSummary(SOME_INDIVIDUAL, SOME_FROM_DATE, SOME_TO_DATE));
    }

    @Test
    public void shouldThrowExceptionIfHttpServerErrorExceptionFromHmrcService() {

        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(SOME_FROM_DATE), eq(SOME_TO_DATE), any(IncomeSummaryContext.class)))
                .willThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> incomeSummaryService.getIncomeSummary(SOME_INDIVIDUAL, SOME_FROM_DATE, SOME_TO_DATE))
                .isInstanceOf(HttpServerErrorException.class);
    }

    @Test
    public void shouldRetryApiCallOnHttpServerErrorException() {

        given(mockAccessCodeClient.getAccessCode())
                .willReturn(SOME_ACCESS_CODE);
        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(SOME_FROM_DATE), eq(SOME_TO_DATE), any(IncomeSummaryContext.class)))
                .willThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));

        when_getIncomeSummary_throws(HttpServerErrorException.class);

        then(mockHmrcClient)
                .should(times(MAX_API_CALL_ATTEMPTS))
                .populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(SOME_FROM_DATE), eq(SOME_TO_DATE), any(IncomeSummaryContext.class));

        then(mockHmrcClient).shouldHaveNoMoreInteractions();
    }

    @Test
    public void shouldNotRetryApiCallOnHttpClientErrorException() {

        given(mockAccessCodeClient.getAccessCode())
                .willReturn(SOME_ACCESS_CODE);
        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(SOME_FROM_DATE), eq(SOME_TO_DATE), any(IncomeSummaryContext.class)))
                .willThrow(new HttpClientErrorException(NOT_FOUND));

        when_getIncomeSummary_throws(HttpClientErrorException.class);

        then(mockHmrcClient)
                .should(times(1))
                .populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(SOME_FROM_DATE), eq(SOME_TO_DATE), any(IncomeSummaryContext.class));

        then(mockHmrcClient).shouldHaveNoMoreInteractions();
    }

    @Test
    public void shouldNotRetryApiCallOnRuntimeException() {

        given(mockAccessCodeClient.getAccessCode())
                .willReturn(SOME_ACCESS_CODE);
        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(SOME_FROM_DATE), eq(SOME_TO_DATE), any(IncomeSummaryContext.class)))
                .willThrow(new RuntimeException("message"));

        when_getIncomeSummary_throws(RuntimeException.class);

        then(mockHmrcClient)
                .should(times(1))
                .populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(SOME_FROM_DATE), eq(SOME_TO_DATE), any(IncomeSummaryContext.class));

        then(mockHmrcClient).shouldHaveNoMoreInteractions();
    }

    @Test
    public void shouldLogInfoWhenRetryingApiCallOnUnexpectedError() {

        given(mockAccessCodeClient.getAccessCode()).willReturn(SOME_ACCESS_CODE);
        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(SOME_FROM_DATE), eq(SOME_TO_DATE), any(IncomeSummaryContext.class)))
                .willThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));

        mockAppender.setName(LOG_TEST_APPENDER);

        Logger logger = (Logger) LoggerFactory.getLogger(IncomeSummaryService.class);
        logger.setLevel(Level.INFO);
        logger.addAppender(mockAppender);

        when_getIncomeSummary_throws(HttpServerErrorException.class);

        then_verifyHmrcCallMessage("HMRC call attempt 1");
        then_verifyHmrcCallMessage("HMRC call attempt 2");
        then_verifyHmrcCallMessage("HMRC call attempt 3");
        then_verifyHmrcCallMessage("HMRC call attempt 4");
        then_verifyHmrcCallMessage("HMRC call attempt 5");

        logger.detachAppender(LOG_TEST_APPENDER);
    }

    @Test
    public void shouldNotProduceIncomeSummaryWhenSufficientRetriesButInsufficientTime() {

        RetryTemplate reauthorisingRetryTemplate = new RetryTemplateBuilder(REAUTHORISING_RETRY_ATTEMPTS)
                                                           .retryHmrcUnauthorisedException()
                                                           .build();
        HmrcRetryTemplateFactory hmrcRetryTemplateFactory = new HmrcRetryTemplateFactory(Integer.MAX_VALUE, BACK_OFF_PERIOD);
        RetryTemplate retryTemplate = hmrcRetryTemplateFactory.createInstance();

        HmrcRetryTemplateFactory mockHmrcRetryTemplateFactory = mock(HmrcRetryTemplateFactory.class);

        mockAppender.setName(LOG_TEST_APPENDER);

        Logger logger = (Logger) LoggerFactory.getLogger(IncomeSummaryService.class);
        logger.setLevel(Level.INFO);
        logger.addAppender(mockAppender);

        incomeSummaryService = new IncomeSummaryService(
                mockHmrcClient,
                mockAccessCodeClient,
                mockAuditClient,
                reauthorisingRetryTemplate,
                REAUTHORISING_RETRY_ATTEMPTS,
                mockRequestHeaderData,
                mockHmrcRetryTemplateFactory);


        LocalDate someDate = LocalDate.of(2018, JANUARY, 1);

        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(someDate), eq(someDate), any(IncomeSummaryContext.class)))
                .willThrow(new InsufficientTimeException("Simulating insufficient time as part of test"));
        given(mockHmrcRetryTemplateFactory.createInstance())
                .willReturn(retryTemplate);

        assertThatExceptionOfType(InsufficientTimeException.class)
                .isThrownBy(() -> incomeSummaryService.getIncomeSummary(SOME_INDIVIDUAL, someDate, someDate));

        then(mockAppender)
                .should(never())
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;
                    return loggingEvent.getFormattedMessage().equals("HMRC call attempt 2");
                }));

        logger.detachAppender(LOG_TEST_APPENDER);
    }

    @Test
    public void shouldNotProduceIncomeSummaryWhenInsufficientRetriesButSufficientTime() {

        RetryTemplate reauthorisingRetryTemplate = new RetryTemplateBuilder(REAUTHORISING_RETRY_ATTEMPTS)
                                                           .retryHmrcUnauthorisedException()
                                                           .build();

        HmrcRetryTemplateFactory hmrcRetryTemplateFactory = new HmrcRetryTemplateFactory(MAX_API_CALL_ATTEMPTS, BACK_OFF_PERIOD);

        incomeSummaryService = new IncomeSummaryService(
                mockHmrcClient,
                mockAccessCodeClient,
                mockAuditClient,
                reauthorisingRetryTemplate,
                REAUTHORISING_RETRY_ATTEMPTS,
                mockRequestHeaderData,
                hmrcRetryTemplateFactory);

        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(SOME_INDIVIDUAL), eq(SOME_FROM_DATE), eq(SOME_TO_DATE), any(IncomeSummaryContext.class)))
                .willThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));

        assertThatExceptionOfType(HttpServerErrorException.class)
                .isThrownBy(() -> incomeSummaryService.getIncomeSummary(SOME_INDIVIDUAL, SOME_FROM_DATE, SOME_TO_DATE));

        then(mockAppender)
                .should(never())
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;
                    return loggingEvent.getFormattedMessage().equals("HMRC call attempt " + (MAX_API_CALL_ATTEMPTS + 1));
                }));
    }

    private void then_verifyHmrcCallMessage(String message) {
        then(mockAppender)
                .should()
                .doAppend(argThat(argument -> {
                    LoggingEvent loggingEvent = (LoggingEvent) argument;
                    return loggingEvent.getFormattedMessage().equals(message) &&
                           ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
                }));
    }

    private void when_getIncomeSummary_throws(Class exceptionType) {

        try {
            incomeSummaryService.getIncomeSummary(SOME_INDIVIDUAL, SOME_FROM_DATE, SOME_TO_DATE);
            fail("Should have received a " + exceptionType.getName());
        } catch(Exception e1) {
            assertThat(e1.getClass()).isEqualTo(exceptionType);
        }
    }
}
