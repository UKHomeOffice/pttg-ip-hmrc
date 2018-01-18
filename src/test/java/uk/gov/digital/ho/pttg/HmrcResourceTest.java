package uk.gov.digital.ho.pttg;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.api.HmrcResource;
import uk.gov.digital.ho.pttg.application.HmrcAccessCodeClient;
import uk.gov.digital.ho.pttg.application.HmrcClient;
import uk.gov.digital.ho.pttg.audit.AuditClient;
import uk.gov.digital.ho.pttg.audit.AuditIndividualData;
import uk.gov.digital.ho.pttg.dto.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_INCOME_REQUEST;

@RunWith(MockitoJUnitRunner.class)
public class HmrcResourceTest {

    private static final String SOME_ACCESS_CODE = "some access code";
    private static final String FIRST_NAME = "Den";
    private static final String LAST_NAME = "Chimes";
    private static final String NINO = "AA654321AA";
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1975, 6, 21);
    private static final LocalDate FROM_DATE = LocalDate.of(2016, 6, 21);
    private static final LocalDate TO_DATE = LocalDate.of(2016, 6, 21);

    @Mock private HmrcClient mockHmrcClient;
    @Mock private AuditClient mockAuditClient;
    @Mock private HmrcAccessCodeClient mockHmrcAccessCodeClient;

    @Captor private ArgumentCaptor<UUID> captorRequestEventId;
    @Captor private ArgumentCaptor<AuditIndividualData> captorRequestAuditData;

    private HmrcResource resource;

    @Before
    public void setup() {
        resource = new HmrcResource(mockHmrcClient, mockHmrcAccessCodeClient, mockAuditClient);
    }

    @Test
    public void testCollaboratorsWhenCreatingCase() {

        when(mockHmrcAccessCodeClient.getAccessCode()).thenReturn(SOME_ACCESS_CODE);
        when(mockHmrcClient.getIncome(SOME_ACCESS_CODE, new Individual(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH), FROM_DATE, TO_DATE)).thenReturn(buildIncomeSummary());

        resource.getHmrcData(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH, FROM_DATE, TO_DATE);

        verify(mockAuditClient).add(eq(HMRC_INCOME_REQUEST), any(UUID.class), any(AuditIndividualData.class));
        verify(mockHmrcAccessCodeClient).getAccessCode();
        verify(mockHmrcClient).getIncome("some access code", new Individual(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH), FROM_DATE, TO_DATE);
    }

    @Test
    public void shouldAllowOptionalToDate() {

        when(mockHmrcAccessCodeClient.getAccessCode()).thenReturn(SOME_ACCESS_CODE);
        when(mockHmrcClient.getIncome(SOME_ACCESS_CODE, new Individual(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH), FROM_DATE, null)).thenReturn(buildIncomeSummary());

        resource.getHmrcData(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH, FROM_DATE, null);

        verify(mockHmrcClient).getIncome("some access code", new Individual(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH), FROM_DATE, null);
    }

    @Test
    public void shouldAuditHmrcRequestAndHmrcResponse() throws IOException {

        when(mockHmrcAccessCodeClient.getAccessCode()).thenReturn(SOME_ACCESS_CODE);
        when(mockHmrcClient.getIncome(SOME_ACCESS_CODE, new Individual(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH), FROM_DATE, null)).thenReturn(buildIncomeSummary());

        resource.getHmrcData(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH, FROM_DATE, null);

        verify(mockAuditClient).add(eq(HMRC_INCOME_REQUEST), captorRequestEventId.capture(), captorRequestAuditData.capture());

        assertThat(captorRequestEventId.getValue()).isNotNull();

        assertThat(captorRequestAuditData.getValue().getMethod()).isEqualTo("get-hmrc-data");
        assertThat(captorRequestAuditData.getValue().getNino()).isEqualTo(NINO);
        assertThat(captorRequestAuditData.getValue().getForename()).isEqualTo(FIRST_NAME);
        assertThat(captorRequestAuditData.getValue().getSurname()).isEqualTo(LAST_NAME);
        assertThat(captorRequestAuditData.getValue().getDateOfBirth()).isEqualTo(DATE_OF_BIRTH);
    }

    private IncomeSummary buildIncomeSummary() {
        final ImmutableList<Income> incomes = ImmutableList.of(new Income("payref", new BigDecimal(4.5), new BigDecimal(6.5), "2017-01-01", 1, null, "PAYE_WEEKLY"));
        final Employer employer = new Employer("payref", "Cadburys", new Address("line1", "line2", "line3", "line4", "line5", "S102BB"));
        final ImmutableList<Employment> employment = ImmutableList.of(new Employment("WEEKLY", "2016-6-21", "2016-6-21", employer));
        final ImmutableList<String> selfAssessment = ImmutableList.of("2013-03-03", "2015-06-06", "2017-01-01");
        return new IncomeSummary(incomes, selfAssessment, employment, new Individual(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH));
    }

}