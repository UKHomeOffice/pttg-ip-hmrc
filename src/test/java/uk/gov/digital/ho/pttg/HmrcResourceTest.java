package uk.gov.digital.ho.pttg;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.api.HmrcResource;
import uk.gov.digital.ho.pttg.application.HmrcClient;
import uk.gov.digital.ho.pttg.audit.AuditService;
import uk.gov.digital.ho.pttg.dto.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_INCOME_REQUEST;
import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_INCOME_RESPONSE;

@RunWith(MockitoJUnitRunner.class)
public class HmrcResourceTest {


    public static final String FIRST_NAME = "Den";
    public static final String LAST_NAME = "Chimes";
    public static final String NINO = "AA654321AA";
    public static final LocalDate DATE_OF_BIRTH = LocalDate.of(1975, 6, 21);
    public static final LocalDate FROM_DATE = LocalDate.of(2016, 6, 21);
    public static final LocalDate TO_DATE = LocalDate.of(2016, 6, 21);

    @Mock private HmrcClient mockClient;
    @Mock private AuditService mockAuditService;

    @Captor private ArgumentCaptor<UUID> captorRequestEventId;
    @Captor private ArgumentCaptor<UUID> captorResponseEventId;
    @Captor private ArgumentCaptor<Map<String, Object>> captorRequestAuditData;
    @Captor private ArgumentCaptor<Map<String, Object>> captorResponseAuditData;

    @InjectMocks
    private HmrcResource resource;

    @Test
    public void testCollaboratorsWhenCreatingCase() {

        when(mockClient.getIncome(new Individual(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH), FROM_DATE, TO_DATE)).thenReturn(buildIncomeSummary());

        resource.getHmrcData(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH, FROM_DATE, TO_DATE);

        verify(mockClient).getIncome(new Individual(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH), FROM_DATE, TO_DATE);
        verify(mockAuditService).add(eq(HMRC_INCOME_REQUEST), Matchers.any(UUID.class), Matchers.any(Map.class));
        verify(mockAuditService).add(eq(HMRC_INCOME_RESPONSE), Matchers.any(UUID.class), Matchers.any(Map.class));
    }

    @Test
    public void shouldAllowOptionalToDate() {

        when(mockClient.getIncome(new Individual(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH), FROM_DATE, null)).thenReturn(buildIncomeSummary());

        resource.getHmrcData(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH, FROM_DATE, null);

        verify(mockClient).getIncome(new Individual(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH), FROM_DATE, null);
    }

    @Test
    public void shouldAuditHmrcRequestAndHmrcResponse() {

        when(mockClient.getIncome(new Individual(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH), FROM_DATE, null)).thenReturn(buildIncomeSummary());

        resource.getHmrcData(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH, FROM_DATE, null);

        verify(mockAuditService).add(eq(HMRC_INCOME_REQUEST), captorRequestEventId.capture(), captorRequestAuditData.capture());
        verify(mockAuditService).add(eq(HMRC_INCOME_RESPONSE), captorResponseEventId.capture(), captorResponseAuditData.capture());

        assertThat(captorRequestEventId.getValue()).isEqualTo(captorResponseEventId.getValue());

        Map<String, Object> requestAuditData = captorRequestAuditData.getValue();

        assertThat(requestAuditData.get("method")).isEqualTo("get-hmrc-data");
        assertThat(requestAuditData.get("nino")).isEqualTo(NINO);
        assertThat(requestAuditData.get("forename")).isEqualTo(FIRST_NAME);
        assertThat(requestAuditData.get("surname")).isEqualTo(LAST_NAME);
        assertThat(requestAuditData.get("dateOfBirth")).isEqualTo(DATE_OF_BIRTH);

        Map<String, Object> responseAuditData = captorResponseAuditData.getValue();

        assertThat(responseAuditData.get("method")).isEqualTo("get-hmrc-data");
        assertThat(responseAuditData.get("response")).isInstanceOf(IncomeSummary.class);

        IncomeSummary is = (IncomeSummary) responseAuditData.get("response");
        assertThat(is.getIncome().size()).isEqualTo(1);
        assertThat(is.getIncome().get(0)).isEqualTo(new Income("payref", new BigDecimal(4.5), new BigDecimal(6.5), "2017-01-01", 1, null));
        assertThat(is.getEmployments().size()).isEqualTo(1);

        Employment employment = is.getEmployments().get(0);

        assertThat(employment.getPayFrequency()).isEqualTo("WEEKLY");
        assertThat(employment.getStartDate()).isEqualTo("2016-6-21");
        assertThat(employment.getEndDate()).isEqualTo("2016-6-21");

        Employer employer = employment.getEmployer();

        assertThat(employer.getPayeReference()).isEqualTo("payref");
        assertThat(employer.getName()).isEqualTo("Cadburys");
        assertThat(employer.getAddress().getLine1()).isEqualTo("line1");
        assertThat(employer.getAddress().getLine2()).isEqualTo("line2");
        assertThat(employer.getAddress().getLine3()).isEqualTo("line3");
        assertThat(employer.getAddress().getLine4()).isEqualTo("line4");
        assertThat(employer.getAddress().getLine5()).isEqualTo("line5");
        assertThat(employer.getAddress().getPostcode()).isEqualTo("S102BB");
    }

    private IncomeSummary buildIncomeSummary() {
        final ImmutableList<Income> incomes = ImmutableList.of(new Income("payref", new BigDecimal(4.5), new BigDecimal(6.5), "2017-01-01", 1, null));
        final Employer employer = new Employer("payref", "Cadburys", new Address("line1", "line2", "line3", "line4", "line5", "S102BB"));
        final ImmutableList<Employment> employment = ImmutableList.of(new Employment("WEEKLY", "2016-6-21", "2016-6-21", employer));
        return new IncomeSummary(incomes, employment);
    }

}