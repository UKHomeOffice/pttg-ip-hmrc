package uk.gov.digital.ho.pttg.application;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.hateoas.Link;
import uk.gov.digital.ho.pttg.application.domain.Individual;

import java.time.LocalDate;
import java.time.Month;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.eq;
import static uk.gov.digital.ho.pttg.application.HmrcClientFunctions.getTaxYear;

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
        hmrcClient = new HmrcClient(mockHmrcHateoasClient, 6, DEFAULT_PAYE_EPOCH);
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

        LocalDate fromDate = LocalDate.now().minusYears(2);
        LocalDate toDate = LocalDate.now().minusYears(3);
        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        String expectedFromTaxYear = getTaxYear(fromDate);
        String expectedToTaxYear = getTaxYear(toDate);
        then(mockHmrcHateoasClient)
                .should()
                .getSelfAssessmentResource(anyString(), eq(expectedFromTaxYear), eq(expectedToTaxYear), any(Link.class));
    }

    @Test
    public void populateIncomeSummary_fromDate6TaxYearsAgo_requestAllTaxYears() {
        given(mockIncomeSummaryContext.needsSelfAssessmentResource()).willReturn(true);
        given(mockIncomeSummaryContext.getIncomeLink(anyString())).willReturn(anyLink);

        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusYears(6);

        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        String expectedFromTaxYear = getTaxYear(fromDate);
        String expectedToTaxYear = getTaxYear(toDate);
        then(mockHmrcHateoasClient)
                .should()
                .getSelfAssessmentResource(anyString(), eq(expectedFromTaxYear), eq(expectedToTaxYear), any(Link.class));
    }

    @Test
    public void populateIncomeSummary_fromDate7TaxYearsAgo_toDateSixTaxYearsAgo() {
        given(mockIncomeSummaryContext.needsSelfAssessmentResource()).willReturn(true);
        given(mockIncomeSummaryContext.getIncomeLink(anyString())).willReturn(anyLink);

        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusYears(7);

        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        String expectedFromTaxYear = getTaxYear(fromDate.plusYears(1));
        String expectedToTaxYear = getTaxYear(toDate);
        then(mockHmrcHateoasClient)
                .should()
                .getSelfAssessmentResource(anyString(), eq(expectedFromTaxYear), eq(expectedToTaxYear), any(Link.class));
    }

    @Test
    public void populateIncomeSummary_toTaxYear5YearsAgo_fromTaxYear6YearsBeforeToYear_fromTaxYearIs6BeforeTaxYearForToday() {
        given(mockIncomeSummaryContext.needsSelfAssessmentResource()).willReturn(true);
        given(mockIncomeSummaryContext.getIncomeLink(anyString())).willReturn(anyLink);

        LocalDate toDate = LocalDate.now().minusYears(5);
        LocalDate fromDate = toDate.minusYears(6);

        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        String expectedFromTaxYear = getTaxYear(LocalDate.now().minusYears(6));
        String expectedToTaxYear = getTaxYear(toDate);
        then(mockHmrcHateoasClient)
                .should()
                .getSelfAssessmentResource(anyString(), eq(expectedFromTaxYear), eq(expectedToTaxYear), any(Link.class));
    }

    @Test
    public void populateIncomeSummary_maximumHistoryIs1000_doNotRestrictTaxYears() {
        given(mockIncomeSummaryContext.needsSelfAssessmentResource()).willReturn(true);
        given(mockIncomeSummaryContext.getIncomeLink(anyString())).willReturn(anyLink);

        LocalDate toDate = LocalDate.of(2019, Month.JANUARY, 1); // Tax year 2018-19
        LocalDate fromDate = LocalDate.of(2000, Month.JANUARY, 1); // Tax year 1999-00

        HmrcClient hmrcClient = new HmrcClient(mockHmrcHateoasClient, 1000, DEFAULT_PAYE_EPOCH);

        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        then(mockHmrcHateoasClient)
                .should()
                .getSelfAssessmentResource(anyString(), eq("1999-00"), eq("2018-19"), any(Link.class));
    }
}
