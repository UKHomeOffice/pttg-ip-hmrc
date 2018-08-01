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
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcNotFoundException;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcUnauthorisedException;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.ProxyForbiddenException;
import uk.gov.digital.ho.pttg.application.HmrcAccessCodeClient;
import uk.gov.digital.ho.pttg.application.HmrcClient;
import uk.gov.digital.ho.pttg.audit.AuditClient;
import uk.gov.digital.ho.pttg.audit.AuditEventType;
import uk.gov.digital.ho.pttg.audit.AuditIndividualData;
import uk.gov.digital.ho.pttg.dto.IncomeSummary;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_INCOME_REQUEST;

@RunWith(MockitoJUnitRunner.class)
public class IncomeSummaryServiceTest {

    private static final String TEST_ACCESS_CODE = "TestAccessCode";
    private static final int REAUTHORISING_RETRY_ATTEMPTS = 2;
    private static final int MAX_API_CALL_ATTEMPTS = 5;
    private static final int BACK_OFF_PERIOD = 1;

    @Mock
    private HmrcClient mockHmrcClient;

    @Mock
    private HmrcAccessCodeClient mockAccessCodeClient;

    @Mock
    private AuditClient mockAuditClient;

    @Mock
    private IncomeSummary mockIncomeSummary;

    @Mock
    private Individual mockIndividual;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<UUID> eventIdCaptor;

    @Captor
    private ArgumentCaptor<AuditIndividualData> auditDataCaptor;

    private IncomeSummaryService incomeSummaryService;

    @Before
    public void setUp() {
        incomeSummaryService = new IncomeSummaryService(mockHmrcClient, mockAccessCodeClient, mockAuditClient, REAUTHORISING_RETRY_ATTEMPTS,
                MAX_API_CALL_ATTEMPTS, BACK_OFF_PERIOD);
    }

    @Test
    public void shouldCallHmrcAndAuditWhenInvoked() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        final LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(TEST_ACCESS_CODE);
        when(mockHmrcClient.getIncomeSummary(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate)).thenReturn(mockIncomeSummary);
        when(mockIndividual.getFirstName()).thenReturn("Arthur");
        when(mockIndividual.getLastName()).thenReturn("Bobbins");

        // when
        final IncomeSummary incomeSummary = incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, toDate);

        // then
        // verify an access code is requested
        verify(mockAccessCodeClient).getAccessCode();

        // verify an income summary request is made to HMRC
        verify(mockHmrcClient).getIncomeSummary(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate);

        // verify output matches value from HMRC call
        assertThat(mockIncomeSummary).isEqualTo(incomeSummary);

        // verify an audit call is made
        verify(mockAuditClient).add(isA(AuditEventType.class), isA(UUID.class), isA(AuditIndividualData.class));

        // verify no more interactions with mocks
        verifyNoMoreInteractions(mockAccessCodeClient, mockAuditClient, mockHmrcClient, mockIncomeSummary);
    }

    @Test
    public void shouldAllowOptionalToDate() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(TEST_ACCESS_CODE);
        when(mockHmrcClient.getIncomeSummary(eq(TEST_ACCESS_CODE), eq(mockIndividual), eq(fromDate), isNull())).thenReturn(mockIncomeSummary);
        when(mockIndividual.getFirstName()).thenReturn("Arthur");
        when(mockIndividual.getLastName()).thenReturn("Bobbins");

        // when
        final IncomeSummary incomeSummary = incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, null);

        // then
        // verify an access code is requested
        verify(mockAccessCodeClient).getAccessCode();

        // verify an income summary request is made to HMRC
        verify(mockHmrcClient).getIncomeSummary(eq(TEST_ACCESS_CODE), eq(mockIndividual), eq(fromDate), isNull());

        // verify output matches value from HMRC call
        assertThat(mockIncomeSummary).isEqualTo(incomeSummary);

        // verify an audit call is made
        verify(mockAuditClient).add(isA(AuditEventType.class), isA(UUID.class), isA(AuditIndividualData.class));

        // verify no more interactions with mocks
        verifyNoMoreInteractions(mockAccessCodeClient, mockAuditClient, mockHmrcClient, mockIncomeSummary);
    }

    @Test
    public void shouldAuditIncomingHmrcRequestCorrectly() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);

        final String expectedAuditMethod = "get-hmrc-data";

        final LocalDate dateOfBirth = LocalDate.of(1990, Month.DECEMBER, 25);
        final String firstName = "FirstName";
        final String lastName = "LastName";
        final String nino = "Nino";
        final Individual individual = new Individual(firstName, lastName, nino, dateOfBirth);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(TEST_ACCESS_CODE);
        when(mockHmrcClient.getIncomeSummary(TEST_ACCESS_CODE, individual, fromDate, null)).thenReturn(mockIncomeSummary);

        // when
        incomeSummaryService.getIncomeSummary(individual, fromDate, null);

        // then
        // verify audit client is called
        verify(mockAuditClient).add(eq(HMRC_INCOME_REQUEST), eventIdCaptor.capture(), auditDataCaptor.capture());

        // verify correct information is audited
        assertThat(eventIdCaptor.getValue()).isNotNull();

        final AuditIndividualData auditData = auditDataCaptor.getValue();
        assertThat(auditData).isNotNull();
        assertThat(auditData.getMethod()).isEqualTo(expectedAuditMethod);
        assertThat(auditData.getForename()).isEqualTo(firstName);
        assertThat(auditData.getSurname()).isEqualTo(lastName);
        assertThat(auditData.getNino()).isEqualTo(nino);
        assertThat(auditData.getDateOfBirth()).isEqualTo(dateOfBirth);
    }

    @Test
    public void shouldRetryHmrcCallsIfUnauthorizedStatusResponse() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        final LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(TEST_ACCESS_CODE);
        when(mockHmrcClient.getIncomeSummary(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate))
                .thenThrow(new HmrcUnauthorisedException("test"))
                .thenReturn(mockIncomeSummary);
        when(mockIndividual.getFirstName()).thenReturn("Arthur");
        when(mockIndividual.getLastName()).thenReturn("Bobbins");

        // when
        final IncomeSummary incomeSummary = incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, toDate);

        // then
        // verify an access code is requested
        verify(mockAccessCodeClient, times(2)).getAccessCode();

        // verify an income summary request is made to HMRC
        verify(mockHmrcClient, times(2)).getIncomeSummary(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate);

        // verify an audit call is made
        verify(mockAuditClient, times(2)).add(isA(AuditEventType.class), isA(UUID.class), isA(AuditIndividualData.class));

        // verify output matches value from HMRC call
        assertThat(mockIncomeSummary).isEqualTo(incomeSummary);

        // verify no more interactions with mocks
        verifyNoMoreInteractions(mockAccessCodeClient, mockAuditClient, mockHmrcClient, mockIncomeSummary);
    }

    @Test
    public void shouldThrowExceptionIfUnexpectedExceptionFromHmrcClient() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        final LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(TEST_ACCESS_CODE);
        when(mockHmrcClient.getIncomeSummary(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate))
                .thenThrow(new IllegalArgumentException());
        when(mockIndividual.getFirstName()).thenReturn("Arthur");
        when(mockIndividual.getLastName()).thenReturn("Bobbins");

        // when
        try {
            incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, toDate);
            fail("A `IllegalArgumentException` should have been thrown.");
        } catch (final IllegalArgumentException e) {
            // success
        }

        // then
        // verify an access code is requested
        verify(mockAccessCodeClient).getAccessCode();

        // verify an income summary request is made to HMRC
        verify(mockHmrcClient).getIncomeSummary(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate);

        // verify an audit call is made
        verify(mockAuditClient).add(isA(AuditEventType.class), isA(UUID.class), isA(AuditIndividualData.class));

        // verify no more interactions with mocks
        verifyNoMoreInteractions(mockAccessCodeClient, mockAuditClient, mockHmrcClient, mockIncomeSummary);
    }

    @Test
    public void shouldThrowExceptionIfOtherHttpClientErrorException() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        final LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(TEST_ACCESS_CODE);
        when(mockHmrcClient.getIncomeSummary(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
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
        verify(mockHmrcClient).getIncomeSummary(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate);

        // verify an audit call is made
        verify(mockAuditClient).add(isA(AuditEventType.class), isA(UUID.class), isA(AuditIndividualData.class));

        // verify no more interactions with mocks
        verifyNoMoreInteractions(mockAccessCodeClient, mockAuditClient, mockHmrcClient, mockIncomeSummary);
    }

    @Test(expected = HttpServerErrorException.class)
    public void shouldThrowExceptionIfHttpServerErrorException() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        final LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(TEST_ACCESS_CODE);
        when(mockHmrcClient.getIncomeSummary(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        when(mockIndividual.getFirstName()).thenReturn("Arthur");
        when(mockIndividual.getLastName()).thenReturn("Bobbins");

        // when
            incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, toDate);
    }

    @Test
    public void shouldRetryApiCallOnUnexpectedError() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        final LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(TEST_ACCESS_CODE);
        when(mockHmrcClient.getIncomeSummary(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        when(mockIndividual.getFirstName()).thenReturn("Arthur");
        when(mockIndividual.getLastName()).thenReturn("Bobbins");

        // when
        try {
            incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, toDate);
        } catch (HttpServerErrorException e) {
            // Ignore expected exception
        }
        // Verify api retry
        verify(mockHmrcClient, times(MAX_API_CALL_ATTEMPTS)).getIncomeSummary(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate);

        verify(mockAccessCodeClient).getAccessCode();
        verify(mockAuditClient).add(isA(AuditEventType.class), isA(UUID.class), isA(AuditIndividualData.class));
        verifyNoMoreInteractions(mockAccessCodeClient, mockAuditClient, mockHmrcClient, mockIncomeSummary);
    }

    @Test
    public void shouldNotRetryApiCallOnHmrcNotFoundException() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        final LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(TEST_ACCESS_CODE);
        when(mockHmrcClient.getIncomeSummary(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate))
                .thenThrow(new HmrcNotFoundException("message"));
        when(mockIndividual.getFirstName()).thenReturn("Arthur");
        when(mockIndividual.getLastName()).thenReturn("Bobbins");

        // when
        try {
            incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, toDate);
        } catch (HmrcNotFoundException e) {
            // Ignore expected exception
        }
        // Verify api retry
        verify(mockHmrcClient, times(1)).getIncomeSummary(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate);

        verify(mockAccessCodeClient).getAccessCode();
        verify(mockAuditClient).add(isA(AuditEventType.class), isA(UUID.class), isA(AuditIndividualData.class));
        verifyNoMoreInteractions(mockAccessCodeClient, mockAuditClient, mockHmrcClient, mockIncomeSummary);
    }

    @Test
    public void shouldNotRetryApiCallOnHmrcProxyForbiddenException() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        final LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(TEST_ACCESS_CODE);
        when(mockHmrcClient.getIncomeSummary(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate))
                .thenThrow(new ProxyForbiddenException("message"));
        when(mockIndividual.getFirstName()).thenReturn("Arthur");
        when(mockIndividual.getLastName()).thenReturn("Bobbins");

        // when
        try {
            incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, toDate);
        } catch (ProxyForbiddenException e) {
            // Ignore expected exception
        }
        // Verify api retry
        verify(mockHmrcClient, times(1)).getIncomeSummary(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate);

        verify(mockAccessCodeClient).getAccessCode();
        verify(mockAuditClient).add(isA(AuditEventType.class), isA(UUID.class), isA(AuditIndividualData.class));
        verifyNoMoreInteractions(mockAccessCodeClient, mockAuditClient, mockHmrcClient, mockIncomeSummary);
    }

    @Test
    public void shouldLogInfoWhenRetryingApiCallOnUnexpectedError() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        final LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(TEST_ACCESS_CODE);
        when(mockHmrcClient.getIncomeSummary(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
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

        verifyHmrcCallMessage("HMRC call attempt 1 of 5");
        verifyHmrcCallMessage("HMRC call attempt 2 of 5");
        verifyHmrcCallMessage("HMRC call attempt 3 of 5");
        verifyHmrcCallMessage("HMRC call attempt 4 of 5");
        verifyHmrcCallMessage("HMRC call attempt 5 of 5");
    }

    private void verifyHmrcCallMessage(String message) {
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals(message) &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
        }));
    }
}
