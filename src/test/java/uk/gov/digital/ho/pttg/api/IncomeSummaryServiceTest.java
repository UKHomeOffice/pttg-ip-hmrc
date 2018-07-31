package uk.gov.digital.ho.pttg.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.digital.ho.pttg.application.HmrcAccessCodeClient;
import uk.gov.digital.ho.pttg.application.HmrcClient;
import uk.gov.digital.ho.pttg.application.IncomeSummaryContext;
import uk.gov.digital.ho.pttg.audit.AuditClient;
import uk.gov.digital.ho.pttg.audit.AuditEventType;
import uk.gov.digital.ho.pttg.audit.AuditIndividualData;
import uk.gov.digital.ho.pttg.dto.IncomeSummary;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcUnauthorisedException;
import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_INCOME_REQUEST;

@RunWith(MockitoJUnitRunner.class)
public class IncomeSummaryServiceTest {

    private static final String SOME_ACCESS_CODE = "SomeAccessCode";
    private static final int RETRY_ATTEMPTS = 2;

    @Mock private HmrcClient mockHmrcClient;
    @Mock private HmrcAccessCodeClient mockAccessCodeClient;
    @Mock private AuditClient mockAuditClient;
    @Mock private IncomeSummary mockIncomeSummary;

    @Captor private ArgumentCaptor<UUID> eventIdCaptor;
    @Captor private ArgumentCaptor<AuditIndividualData> auditDataCaptor;

    private IncomeSummaryService incomeSummaryService;

    @Before
    public void setUp() {
        incomeSummaryService = new IncomeSummaryService(mockHmrcClient, mockAccessCodeClient, mockAuditClient, RETRY_ATTEMPTS);

        given(mockAccessCodeClient.getAccessCode()).willReturn(SOME_ACCESS_CODE);
    }

    @Test
    public void shouldCallCollaborators() {

        LocalDate someFromDate = LocalDate.of(2018, Month.JANUARY, 1);
        LocalDate someToDate = LocalDate.of(2018, Month.MAY, 1);
        Individual someIndividual = new Individual("some first name", "some last name", "some nino", LocalDate.now());

        given(mockHmrcClient.getIncomeSummary(eq(SOME_ACCESS_CODE), any(Individual.class), eq(someFromDate), eq(someToDate), any(IncomeSummaryContext.class)))
                .willReturn(mockIncomeSummary);

        incomeSummaryService.getIncomeSummary(someIndividual, someFromDate, someToDate);

        verify(mockAccessCodeClient).getAccessCode();
        verify(mockHmrcClient).getIncomeSummary(eq(SOME_ACCESS_CODE), eq(someIndividual), eq(someFromDate), eq(someToDate), any(IncomeSummaryContext.class));
        verify(mockAuditClient).add(any(AuditEventType.class), any(UUID.class), any(AuditIndividualData.class));
    }

    @Test
    public void shouldAllowOptionalToDate() {

        LocalDate someFromDate = LocalDate.of(2018, Month.JANUARY, 1);
        Individual someIndividual = new Individual("some first name", "some last name", "some nino", LocalDate.now());

        given(mockHmrcClient.getIncomeSummary(eq(SOME_ACCESS_CODE), any(Individual.class), eq(someFromDate), isNull(), any(IncomeSummaryContext.class)))
                .willReturn(mockIncomeSummary);

        incomeSummaryService.getIncomeSummary(someIndividual, someFromDate, null);

        verify(mockHmrcClient).getIncomeSummary(eq(SOME_ACCESS_CODE), eq(someIndividual), eq(someFromDate), isNull(), any(IncomeSummaryContext.class));
    }

    @Test
    public void shouldAudit() {

        LocalDate someFromDate = LocalDate.of(2018, Month.JANUARY, 1);
        LocalDate dateOfBirth = LocalDate.of(1990, Month.DECEMBER, 25);
        String firstName = "FirstName";
        String lastName = "LastName";
        String nino = "Nino";
        Individual individual = new Individual(firstName, lastName, nino, dateOfBirth);

        given(mockHmrcClient.getIncomeSummary(eq(SOME_ACCESS_CODE), eq(individual), eq(someFromDate), isNull(), any(IncomeSummaryContext.class)))
                .willReturn(mockIncomeSummary);

        incomeSummaryService.getIncomeSummary(individual, someFromDate, null);

        verify(mockAuditClient).add(eq(HMRC_INCOME_REQUEST), eventIdCaptor.capture(), auditDataCaptor.capture());

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
        Individual someIndividual = new Individual("some first name", "some last name", "some nino", LocalDate.now());

        given(mockHmrcClient.getIncomeSummary(eq(SOME_ACCESS_CODE), eq(someIndividual), eq(someDate), eq(someDate), any(IncomeSummaryContext.class)))
                .willThrow(new HmrcUnauthorisedException("test"))
                .willReturn(mockIncomeSummary);

        IncomeSummary incomeSummary = incomeSummaryService.getIncomeSummary(someIndividual, someDate, someDate);

        assertThat(mockIncomeSummary).isEqualTo(incomeSummary);

        verify(mockAccessCodeClient, times(2)).getAccessCode();
        verify(mockHmrcClient, times(2)).getIncomeSummary(eq(SOME_ACCESS_CODE), eq(someIndividual), eq(someDate), eq(someDate), any(IncomeSummaryContext.class));
        verify(mockAuditClient, times(2)).add(isA(AuditEventType.class), isA(UUID.class), isA(AuditIndividualData.class));
    }

    @Test
    public void shouldThrowExceptionIfUnexpectedExceptionFromHmrcService() {

        LocalDate someDate = LocalDate.of(2018, Month.JANUARY, 1);
        Individual someIndividual = new Individual("some first name", "some last name", "some nino", LocalDate.now());

        given(mockHmrcClient.getIncomeSummary(eq(SOME_ACCESS_CODE), eq(someIndividual), eq(someDate), eq(someDate), any(IncomeSummaryContext.class)))
                .willThrow(new IllegalArgumentException());

        assertThatThrownBy(() -> incomeSummaryService.getIncomeSummary(someIndividual, someDate, someDate))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowExceptionIfHttpServerErrorExceptionFromHmrcService() {

        LocalDate someDate = LocalDate.of(2018, Month.JANUARY, 1);
        Individual someIndividual = new Individual("some first name", "some last name", "some nino", LocalDate.now());

        given(mockHmrcClient.getIncomeSummary(eq(SOME_ACCESS_CODE), eq(someIndividual), eq(someDate), eq(someDate), any(IncomeSummaryContext.class)))
                .willThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> incomeSummaryService.getIncomeSummary(someIndividual, someDate, someDate))
                .isInstanceOf(HttpServerErrorException.class);
    }
}