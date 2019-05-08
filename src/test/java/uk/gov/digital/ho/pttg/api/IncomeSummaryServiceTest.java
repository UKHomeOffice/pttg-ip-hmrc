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
import uk.gov.digital.ho.pttg.application.*;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcUnauthorisedException;
import uk.gov.digital.ho.pttg.application.domain.IncomeSummary;
import uk.gov.digital.ho.pttg.application.domain.Individual;
import uk.gov.digital.ho.pttg.application.retry.RetryTemplateBuilder;
import uk.gov.digital.ho.pttg.audit.AuditClient;
import uk.gov.digital.ho.pttg.audit.AuditEventType;
import uk.gov.digital.ho.pttg.audit.AuditIndividualData;

import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_INCOME_REQUEST;

@RunWith(MockitoJUnitRunner.class)
public class IncomeSummaryServiceTest {

    private static final String SOME_ACCESS_CODE = "SomeAccessCode";
    private static final int REAUTHORISING_RETRY_ATTEMPTS = 2;
    private static final int MAX_API_CALL_ATTEMPTS = 5;
    private static final int BACK_OFF_PERIOD = 1;
    private static final int MAX_DURATION_IN_MS = 10000;

    @Mock private HmrcClient mockHmrcClient;
    @Mock private HmrcAccessCodeClient mockAccessCodeClient;
    @Mock private AuditClient mockAuditClient;
    @Mock private IncomeSummary mockIncomeSummary;
    @Mock private Individual mockIndividual;
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

        given(mockAccessCodeClient.getAccessCode()).willReturn(SOME_ACCESS_CODE);
        given(mockRequestHeaderData.serviceMaxDuration()).willReturn(MAX_DURATION_IN_MS);
    }

    @Test
    public void shouldCallCollaborators() {

        LocalDate someFromDate = LocalDate.of(2018, Month.JANUARY, 1);
        LocalDate someToDate = LocalDate.of(2018, Month.MAY, 1);
        Individual someIndividual = new Individual("some first name", "some last name", "some nino", LocalDate.now(), "");

        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), any(Individual.class), eq(someFromDate), eq(someToDate), any(IncomeSummaryContext.class)))
                .willReturn(mockIncomeSummary);

        incomeSummaryService.getIncomeSummary(someIndividual, someFromDate, someToDate);

        then(mockAccessCodeClient).should().getAccessCode();
        then(mockHmrcClient).should().populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(someIndividual), eq(someFromDate), eq(someToDate), any(IncomeSummaryContext.class));
        then(mockAuditClient).should().add(any(AuditEventType.class), any(UUID.class), any(AuditIndividualData.class));
    }

    @Test
    public void shouldAllowOptionalToDate() {

        LocalDate someFromDate = LocalDate.of(2018, Month.JANUARY, 1);
        Individual someIndividual = new Individual("some first name", "some last name", "some nino", LocalDate.now(), "");

        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), any(Individual.class), eq(someFromDate), isNull(), any(IncomeSummaryContext.class)))
                .willReturn(mockIncomeSummary);

        incomeSummaryService.getIncomeSummary(someIndividual, someFromDate, null);

        then(mockHmrcClient).should().populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(someIndividual), eq(someFromDate), isNull(), any(IncomeSummaryContext.class));
    }

    @Test
    public void shouldAudit() {

        LocalDate someFromDate = LocalDate.of(2018, Month.JANUARY, 1);
        LocalDate dateOfBirth = LocalDate.of(1990, Month.DECEMBER, 25);
        String firstName = "FirstName";
        String lastName = "LastName";
        String nino = "Nino";
        Individual individual = new Individual(firstName, lastName, nino, dateOfBirth, "");

        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(individual), eq(someFromDate), isNull(), any(IncomeSummaryContext.class)))
                .willReturn(mockIncomeSummary);

        incomeSummaryService.getIncomeSummary(individual, someFromDate, null);

        then(mockAuditClient).should().add(eq(HMRC_INCOME_REQUEST), eventIdCaptor.capture(), auditDataCaptor.capture());

        assertThat(eventIdCaptor.getValue()).isNotNull();

        AuditIndividualData auditData = auditDataCaptor.getValue();

        assertThat(auditData).isNotNull();
        assertThat(auditData.getMethod()).isEqualTo("get-hmrc-data");
        assertThat(auditData.getForename()).isEqualTo(firstName);
        assertThat(auditData.getSurname()).isEqualTo(lastName);
        assertThat(auditData.getNino()).isEqualTo(nino);
        assertThat(auditData.getDateOfBirth()).isEqualTo(dateOfBirth);
    }

    @Test
    public void shouldRetryAllCollaboratorCallsIfHmrcServiceUnauthorizedStatusResponse() {

        LocalDate someDate = LocalDate.of(2018, Month.JANUARY, 1);
        Individual someIndividual = new Individual("some first name", "some last name", "some nino", LocalDate.now(), "");

        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(someIndividual), eq(someDate), eq(someDate), any(IncomeSummaryContext.class)))
                .willThrow(new HmrcUnauthorisedException("test"))
                .willReturn(mockIncomeSummary);

        IncomeSummary incomeSummary = incomeSummaryService.getIncomeSummary(someIndividual, someDate, someDate);

        assertThat(mockIncomeSummary).isEqualTo(incomeSummary);

        then(mockAccessCodeClient).should(times(2)).getAccessCode();
        then(mockAccessCodeClient).should().loadLatestAccessCode();
        then(mockHmrcClient).should(times(2)).populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(someIndividual), eq(someDate), eq(someDate), any(IncomeSummaryContext.class));
        then(mockAuditClient).should(times(2)).add(isA(AuditEventType.class), isA(UUID.class), isA(AuditIndividualData.class));
    }

    @Test
    public void shouldThrowExceptionIfUnexpectedExceptionFromHmrcService() {

        LocalDate someDate = LocalDate.of(2018, Month.JANUARY, 1);
        Individual someIndividual = new Individual("some first name", "some last name", "some nino", LocalDate.now(), "");

        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(someIndividual), eq(someDate), eq(someDate), any(IncomeSummaryContext.class)))
                .willThrow(new IllegalArgumentException());

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> incomeSummaryService.getIncomeSummary(someIndividual, someDate, someDate));

    }

    @Test(expected = HttpServerErrorException.class)
    public void shouldThrowExceptionIfHttpServerErrorException() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        final LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(SOME_ACCESS_CODE);
        when(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(mockIndividual), eq(fromDate), eq(toDate), any(IncomeSummaryContext.class)))
                .thenThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));
        when(mockIndividual.getFirstName()).thenReturn("Arthur");
        when(mockIndividual.getLastName()).thenReturn("Bobbins");

        // when
        incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, toDate);
    }

    @Test
    public void shouldThrowExceptionIfOtherHttpClientErrorException() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        final LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(SOME_ACCESS_CODE);
        when(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(mockIndividual), eq(fromDate), eq(toDate), any(IncomeSummaryContext.class)))
                .thenThrow(new HttpClientErrorException(BAD_REQUEST));
        when(mockIndividual.getFirstName()).thenReturn("Arthur");
        when(mockIndividual.getLastName()).thenReturn("Bobbins");

        // when
        try {
            incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, toDate);
            fail("A `HttpClientErrorException` should have been thrown");
        } catch (final HttpClientErrorException e) {
            // success
        }

        // then
        // verify an access code is requested
        verify(mockAccessCodeClient).getAccessCode();

        // verify an income summary request is made to HMRC
        verify(mockHmrcClient).populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(mockIndividual), eq(fromDate), eq(toDate), any(IncomeSummaryContext.class));

        // verify an audit call is made
        verify(mockAuditClient).add(isA(AuditEventType.class), isA(UUID.class), isA(AuditIndividualData.class));

        // verify no more interactions with mocks
        verifyNoMoreInteractions(mockAccessCodeClient, mockAuditClient, mockHmrcClient, mockIncomeSummary);
    }

    @Test
    public void shouldThrowExceptionIfHttpServerErrorExceptionFromHmrcService() {

        LocalDate someDate = LocalDate.of(2018, Month.JANUARY, 1);
        Individual someIndividual = new Individual("some first name", "some last name", "some nino", LocalDate.now(), "");

        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(someIndividual), eq(someDate), eq(someDate), any(IncomeSummaryContext.class)))
                .willThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> incomeSummaryService.getIncomeSummary(someIndividual, someDate, someDate))
                .isInstanceOf(HttpServerErrorException.class);
    }

    @Test
    public void shouldRetryApiCallOnUnexpectedError() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        final LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(SOME_ACCESS_CODE);
        when(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(mockIndividual), eq(fromDate), eq(toDate), any(IncomeSummaryContext.class)))
                .thenThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));
        when(mockIndividual.getFirstName()).thenReturn("Arthur");
        when(mockIndividual.getLastName()).thenReturn("Bobbins");

        // when
        try {
            incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, toDate);
        } catch (HttpServerErrorException e) {
            // Ignore expected exception
        }
        // Verify api retry
        verify(mockHmrcClient, times(MAX_API_CALL_ATTEMPTS)).populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(mockIndividual), eq(fromDate), eq(toDate), any(IncomeSummaryContext.class));

        verify(mockAccessCodeClient).getAccessCode();
        verify(mockAuditClient).add(isA(AuditEventType.class), isA(UUID.class), isA(AuditIndividualData.class));
        verifyNoMoreInteractions(mockAccessCodeClient, mockAuditClient, mockHmrcClient, mockIncomeSummary);
    }

    @Test
    public void shouldNotRetryApiCallOnHmrcNotFoundException() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        final LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(SOME_ACCESS_CODE);
        when(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(mockIndividual), eq(fromDate), eq(toDate), any(IncomeSummaryContext.class)))
                .thenThrow(new ApplicationExceptions.HmrcNotFoundException("message"));
        when(mockIndividual.getFirstName()).thenReturn("Arthur");
        when(mockIndividual.getLastName()).thenReturn("Bobbins");

        // when
        try {
            incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, toDate);
        } catch (ApplicationExceptions.HmrcNotFoundException e) {
            // Ignore expected exception
        }
        // Verify api retry
        verify(mockHmrcClient, times(1)).populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(mockIndividual), eq(fromDate), eq(toDate), any(IncomeSummaryContext.class));

        verify(mockAccessCodeClient).getAccessCode();
        verify(mockAuditClient).add(isA(AuditEventType.class), isA(UUID.class), isA(AuditIndividualData.class));
        verifyNoMoreInteractions(mockAccessCodeClient, mockAuditClient, mockHmrcClient, mockIncomeSummary);
    }

    @Test
    public void shouldNotRetryApiCallOnHmrcProxyForbiddenException() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        final LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(SOME_ACCESS_CODE);
        when(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(mockIndividual), eq(fromDate), eq(toDate), any(IncomeSummaryContext.class)))
                .thenThrow(new ApplicationExceptions.ProxyForbiddenException("message"));
        when(mockIndividual.getFirstName()).thenReturn("Arthur");
        when(mockIndividual.getLastName()).thenReturn("Bobbins");

        // when
        try {
            incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, toDate);
        } catch (ApplicationExceptions.ProxyForbiddenException e) {
            // Ignore expected exception
        }
        // Verify api retry
        verify(mockHmrcClient, times(1)).populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(mockIndividual), eq(fromDate), eq(toDate), any(IncomeSummaryContext.class));

        verify(mockAccessCodeClient).getAccessCode();
        verify(mockAuditClient).add(isA(AuditEventType.class), isA(UUID.class), isA(AuditIndividualData.class));
        verifyNoMoreInteractions(mockAccessCodeClient, mockAuditClient, mockHmrcClient, mockIncomeSummary);
    }

    @Test
    public void shouldLogInfoWhenRetryingApiCallOnUnexpectedError() {
        // given
        LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(SOME_ACCESS_CODE);
        when(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(mockIndividual), eq(fromDate), eq(toDate), any(IncomeSummaryContext.class)))
                .thenThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));
        when(mockIndividual.getFirstName()).thenReturn("Arthur");
        when(mockIndividual.getLastName()).thenReturn("Bobbins");

        Logger rootLogger = (Logger) LoggerFactory.getLogger(IncomeSummaryService.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);

        // when
        try {
            incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, toDate);
        } catch (HttpServerErrorException e) {
            // Ignore expected exception
        }

        verifyHmrcCallMessage("HMRC call attempt 1");
        verifyHmrcCallMessage("HMRC call attempt 2");
        verifyHmrcCallMessage("HMRC call attempt 3");
        verifyHmrcCallMessage("HMRC call attempt 4");
        verifyHmrcCallMessage("HMRC call attempt 5");
    }

    @Test
    public void shouldNotProduceIncomeSummaryWhenSufficientRetriesButInsufficientTime() {

        RetryTemplate reauthorisingRetryTemplate = new RetryTemplateBuilder(REAUTHORISING_RETRY_ATTEMPTS)
                                                           .retryHmrcUnauthorisedException()
                                                           .build();
        HmrcRetryTemplateFactory hmrcRetryTemplateFactory = new HmrcRetryTemplateFactory(Integer.MAX_VALUE, BACK_OFF_PERIOD);
        RetryTemplate retryTemplate = hmrcRetryTemplateFactory.createInstance(100);

        HmrcRetryTemplateFactory mockHmrcRetryTemplateFactory = mock(HmrcRetryTemplateFactory.class);

        Logger rootLogger = (Logger) LoggerFactory.getLogger(IncomeSummaryService.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);

        incomeSummaryService = new IncomeSummaryService(
                mockHmrcClient,
                mockAccessCodeClient,
                mockAuditClient,
                reauthorisingRetryTemplate,
                REAUTHORISING_RETRY_ATTEMPTS,
                mockRequestHeaderData,
                mockHmrcRetryTemplateFactory);


        LocalDate someDate = LocalDate.of(2018, Month.JANUARY, 1);
        Individual someIndividual = new Individual("some first name", "some last name", "some nino", LocalDate.now(), "");

        given(mockRequestHeaderData.serviceMaxDuration())
                .willReturn(100);
        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(someIndividual), eq(someDate), eq(someDate), any(IncomeSummaryContext.class)))
                .willThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));
        given(mockHmrcRetryTemplateFactory.createInstance(100))
                .willReturn(retryTemplate);

        assertThatExceptionOfType(HttpServerErrorException.class)
                .isThrownBy(() -> incomeSummaryService.getIncomeSummary(someIndividual, someDate, someDate));

        verify(mockAppender, never()).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("HMRC call attempt " + Integer.MAX_VALUE);
        }));
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


        LocalDate someDate = LocalDate.of(2018, Month.JANUARY, 1);
        Individual someIndividual = new Individual("some first name", "some last name", "some nino", LocalDate.now(), "");

        given(mockRequestHeaderData.serviceMaxDuration()).willReturn(Integer.MAX_VALUE);
        given(mockHmrcClient.populateIncomeSummary(eq(SOME_ACCESS_CODE), eq(someIndividual), eq(someDate), eq(someDate), any(IncomeSummaryContext.class)))
                .willThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));

        assertThatExceptionOfType(HttpServerErrorException.class)
                .isThrownBy(() -> incomeSummaryService.getIncomeSummary(someIndividual, someDate, someDate));

        verify(mockAppender, never()).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("HMRC call attempt " + (MAX_API_CALL_ATTEMPTS + 1));
        }));
    }

    private void verifyHmrcCallMessage(String message) {
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals(message) &&
                           ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }
}
