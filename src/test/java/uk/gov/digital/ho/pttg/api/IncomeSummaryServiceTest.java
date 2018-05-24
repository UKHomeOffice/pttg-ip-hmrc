package uk.gov.digital.ho.pttg.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;
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
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_INCOME_REQUEST;

@RunWith(MockitoJUnitRunner.class)
public class IncomeSummaryServiceTest {

    private static final String TEST_ACCESS_CODE = "TestAccessCode";
    private static final int RETRY_ATTEMPTS = 2;

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

    @Captor
    private ArgumentCaptor<UUID> eventIdCaptor;

    @Captor
    private ArgumentCaptor<AuditIndividualData> auditDataCaptor;

    private IncomeSummaryService incomeSummaryService;

    @Before
    public void setUp() {
        incomeSummaryService = new IncomeSummaryService(mockHmrcClient, mockAccessCodeClient, mockAuditClient, RETRY_ATTEMPTS);
    }

    @Test
    public void shouldCallHmrcAndAuditWhenInvoked() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        final LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(TEST_ACCESS_CODE);
        when(mockHmrcClient.getIncome(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate)).thenReturn(mockIncomeSummary);

        // when
        final IncomeSummary incomeSummary = incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, toDate);

        // then
        // verify an access code is requested
        verify(mockAccessCodeClient).getAccessCode();

        // verify an income summary request is made to HMRC
        verify(mockHmrcClient).getIncome(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate);

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
        when(mockHmrcClient.getIncome(eq(TEST_ACCESS_CODE), eq(mockIndividual), eq(fromDate), isNull(LocalDate.class))).thenReturn(mockIncomeSummary);

        // when
        final IncomeSummary incomeSummary = incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, null);

        // then
        // verify an access code is requested
        verify(mockAccessCodeClient).getAccessCode();

        // verify an income summary request is made to HMRC
        verify(mockHmrcClient).getIncome(eq(TEST_ACCESS_CODE), eq(mockIndividual), eq(fromDate), isNull(LocalDate.class));

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
        when(mockHmrcClient.getIncome(TEST_ACCESS_CODE, individual, fromDate, null)).thenReturn(mockIncomeSummary);

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
        when(mockHmrcClient.getIncome(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate))
                .thenThrow(new ApplicationExceptions.HmrcUnauthorisedException("test"))
                .thenReturn(mockIncomeSummary);

        // when
        final IncomeSummary incomeSummary = incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, toDate);

        // then
        // verify an access code is requested
        verify(mockAccessCodeClient, times(2)).getAccessCode();

        // verify an income summary request is made to HMRC
        verify(mockHmrcClient, times(2)).getIncome(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate);

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
        when(mockHmrcClient.getIncome(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate))
                .thenThrow(new IllegalArgumentException())
                .thenReturn(mockIncomeSummary);

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
        verify(mockHmrcClient).getIncome(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate);

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
        when(mockHmrcClient.getIncome(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST))
                .thenReturn(mockIncomeSummary);

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
        verify(mockHmrcClient).getIncome(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate);

        // verify an audit call is made
        verify(mockAuditClient).add(isA(AuditEventType.class), isA(UUID.class), isA(AuditIndividualData.class));

        // verify no more interactions with mocks
        verifyNoMoreInteractions(mockAccessCodeClient, mockAuditClient, mockHmrcClient, mockIncomeSummary);
    }

    @Test
    public void shouldThrowExceptionIfHttpServerErrorException() {
        // given
        final LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        final LocalDate toDate = LocalDate.of(2018, Month.MAY, 1);

        when(mockAccessCodeClient.getAccessCode()).thenReturn(TEST_ACCESS_CODE);
        when(mockHmrcClient.getIncome(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
                .thenReturn(mockIncomeSummary);

        // when
        try {
            incomeSummaryService.getIncomeSummary(mockIndividual, fromDate, toDate);
            fail("A `HttpServerErrorException.class` should have been thrown.");
        } catch (final HttpServerErrorException e) {
            // success
        }

        // then
        // verify an access code is requested
        verify(mockAccessCodeClient).getAccessCode();

        // verify an income summary request is made to HMRC
        verify(mockHmrcClient).getIncome(TEST_ACCESS_CODE, mockIndividual, fromDate, toDate);

        // verify an audit call is made
        verify(mockAuditClient).add(isA(AuditEventType.class), isA(UUID.class), isA(AuditIndividualData.class));

        // verify no more interactions with mocks
        verifyNoMoreInteractions(mockAccessCodeClient, mockAuditClient, mockHmrcClient, mockIncomeSummary);
    }
}