package uk.gov.digital.ho.pttg.application;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cglib.core.Local;
import org.springframework.hateoas.Link;
import uk.gov.digital.ho.pttg.application.domain.Individual;

import java.time.LocalDate;
import java.time.Month;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HmrcClientTest {

    private static final LocalDate DEFAULT_PAYE_EPOCH = LocalDate.of(2013, Month.MARCH, 31);

    @Mock private Link anyLink;
    @Mock private Individual anyIndividual;
    @Mock private HmrcHateoasClient mockHmrcHateoasClient;
    @Mock private IncomeSummaryContext mockIncomeSummaryContext;

    private HmrcClient hmrcClient;

    @Before
    public void setUp() {
        hmrcClient = new HmrcClient(mockHmrcHateoasClient, DEFAULT_PAYE_EPOCH);
    }

    @Test
    @SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void shouldGetSelfEmployment() {
        given(mockIncomeSummaryContext.needsSelfAssessmentSelfEmploymentIncome()).willReturn(true);
        given(mockIncomeSummaryContext.getSelfAssessmentLink(any(String.class))).willReturn(anyLink);

        hmrcClient.populateIncomeSummary("some access token", anyIndividual, LocalDate.now(), LocalDate.now(), mockIncomeSummaryContext);

        then(mockIncomeSummaryContext).should().needsSelfAssessmentSelfEmploymentIncome();
        then(mockHmrcHateoasClient).should().getSelfAssessmentSelfEmploymentIncome(anyString(), any(Link.class));
    }

    @Test
    public void populateIncomeSummary_givenDates_requestSelfAssessmentByTaxYear() {
        given(mockIncomeSummaryContext.needsSelfAssessmentResource()).willReturn(true);
        given(mockIncomeSummaryContext.getIncomeLink(anyString())).willReturn(anyLink);

        LocalDate fromDate = LocalDate.of(2018, Month.JANUARY, 1);
        LocalDate toDate = LocalDate.of(2019, Month.JANUARY, 1);
        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        then(mockHmrcHateoasClient)
                .should()
                .getSelfAssessmentResource(anyString(), eq("2017-18"), eq("2018-19"), any(Link.class));
    }

    @Test
    public void populateIncomeSummary_payeIncome_fromDateAfterEpoch_useInRequest() {
        given(mockIncomeSummaryContext.needsPayeIncome()).willReturn(true);
        given(mockIncomeSummaryContext.getIncomeLink(anyString())).willReturn(anyLink);

        LocalDate fromDate = DEFAULT_PAYE_EPOCH.plusDays(1);
        LocalDate toDate = LocalDate.now();

        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        then(mockHmrcHateoasClient)
                .should()
                .getPayeIncome(eq(fromDate), eq(toDate), anyString(), any(Link.class));
    }

    @Test
    public void populateIncomeSummary_payeIncome_fromDateOnEpoch_useInRequest() {
        given(mockIncomeSummaryContext.needsPayeIncome()).willReturn(true);
        given(mockIncomeSummaryContext.getIncomeLink(anyString())).willReturn(anyLink);

        LocalDate fromDate = DEFAULT_PAYE_EPOCH;
        LocalDate toDate = LocalDate.now();

        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        then(mockHmrcHateoasClient)
                .should()
                .getPayeIncome(eq(fromDate), eq(toDate), anyString(), any(Link.class));
    }

    @Test
    public void populateIncomeSummary_payeIncome_fromDateBeforeEpoch_useEpoch() {
        given(mockIncomeSummaryContext.needsPayeIncome()).willReturn(true);
        given(mockIncomeSummaryContext.getIncomeLink(anyString())).willReturn(anyLink);

        LocalDate fromDate = DEFAULT_PAYE_EPOCH.minusDays(1);
        LocalDate toDate = LocalDate.now();

        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        then(mockHmrcHateoasClient)
                .should()
                .getPayeIncome(eq(DEFAULT_PAYE_EPOCH), eq(toDate), anyString(), any(Link.class));
    }

    @Test
    public void populateIncomeSummary_payeIncome_epochLocalDateMin_doNotRestrictFromDate() {
        given(mockIncomeSummaryContext.needsPayeIncome()).willReturn(true);
        given(mockIncomeSummaryContext.getIncomeLink(anyString())).willReturn(anyLink);

        LocalDate fromDate = LocalDate.of(1900, Month.JANUARY, 1);
        LocalDate toDate = LocalDate.now();

        HmrcClient hmrcClient = new HmrcClient(mockHmrcHateoasClient, LocalDate.MIN);
        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        then(mockHmrcHateoasClient)
                .should()
                .getPayeIncome(eq(fromDate), eq(toDate), anyString(), any(Link.class));
    }

    @Test
    public void populateIncomeSummary_payeEmployments_fromDateAfterEpoch_useInRequest() {
        given(mockIncomeSummaryContext.needsEmployments()).willReturn(true);
        given(mockIncomeSummaryContext.getEmploymentLink(anyString())).willReturn(anyLink);

        LocalDate fromDate = DEFAULT_PAYE_EPOCH.plusDays(1);
        LocalDate toDate = LocalDate.now();

        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        then(mockHmrcHateoasClient)
                .should()
                .getEmployments(eq(fromDate), eq(toDate), anyString(), any(Link.class));
    }

    @Test
    public void populateIncomeSummary_payeEmployments_fromDateOnEpoch_useInRequest() {
        given(mockIncomeSummaryContext.needsEmployments()).willReturn(true);
        given(mockIncomeSummaryContext.getEmploymentLink(anyString())).willReturn(anyLink);

        LocalDate fromDate = DEFAULT_PAYE_EPOCH;
        LocalDate toDate = LocalDate.now();

        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        then(mockHmrcHateoasClient)
                .should()
                .getEmployments(eq(fromDate), eq(toDate), anyString(), any(Link.class));
    }

    @Test
    public void populateIncomeSummary_payeEmployments_fromDateBeforeEpoch_useEpoch() {
        given(mockIncomeSummaryContext.needsEmployments()).willReturn(true);
        given(mockIncomeSummaryContext.getEmploymentLink(anyString())).willReturn(anyLink);

        LocalDate fromDate = DEFAULT_PAYE_EPOCH.minusDays(1);
        LocalDate toDate = LocalDate.now();

        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        then(mockHmrcHateoasClient)
                .should()
                .getEmployments(eq(DEFAULT_PAYE_EPOCH), eq(toDate), anyString(), any(Link.class));
    }

    @Test
    public void populateIncomeSummary_payeEmployments_epochLocalDateMin_doNotRestrictFromDate() {
        given(mockIncomeSummaryContext.needsEmployments()).willReturn(true);
        given(mockIncomeSummaryContext.getEmploymentLink(anyString())).willReturn(anyLink);

        LocalDate fromDate = LocalDate.of(1900, Month.JANUARY, 1);
        LocalDate toDate = LocalDate.now();

        HmrcClient hmrcClient = new HmrcClient(mockHmrcHateoasClient, LocalDate.MIN);
        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        then(mockHmrcHateoasClient)
                .should()
                .getEmployments(eq(fromDate), eq(toDate), anyString(), any(Link.class));
    }
}
