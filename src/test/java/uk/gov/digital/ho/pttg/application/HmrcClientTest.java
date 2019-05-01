package uk.gov.digital.ho.pttg.application;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.hateoas.Link;
import uk.gov.digital.ho.pttg.application.domain.Individual;

import java.time.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.eq;

@RunWith(MockitoJUnitRunner.class)
public class HmrcClientTest {

    private static final LocalDate TODAY = LocalDate.of(2018, Month.JANUARY, 1);

    @Mock private Link anyLink;
    @Mock private Individual anyIndividual;
    @Mock private HmrcHateoasClient mockHmrcHateoasClient;
    @Mock private IncomeSummaryContext mockIncomeSummaryContext;

    private HmrcClient hmrcClient;

    @Before
    public void setUp() {
        Clock mockClock = Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        hmrcClient = new HmrcClient(mockHmrcHateoasClient, mockClock);
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
    public void populateIncomeSummary_fromTaxYearIsCurrentMinus6_requestAllTaxYears() {
        given(mockIncomeSummaryContext.needsSelfAssessmentResource()).willReturn(true);
        given(mockIncomeSummaryContext.getIncomeLink(anyString())).willReturn(anyLink);

        LocalDate toDate = LocalDate.of(2018, Month.JANUARY, 1); // Tax year 2017-18

        LocalDate fromDate = LocalDate.of(2012, Month.JANUARY, 1); // Tax year 2011-12
        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        then(mockHmrcHateoasClient)
                .should()
                .getSelfAssessmentResource(anyString(), eq("2011-12"), eq("2017-18"), any(Link.class));
    }

    @Test
    public void populateIncomeSummary_fromTaxYearIsCurrentMinus7_requestOnlyLast6() {
        given(mockIncomeSummaryContext.needsSelfAssessmentResource()).willReturn(true);
        given(mockIncomeSummaryContext.getIncomeLink(anyString())).willReturn(anyLink);

        LocalDate toDate = LocalDate.of(2018, Month.JANUARY, 1); // Tax year 2017-18

        LocalDate fromDate = LocalDate.of(2011, Month.JANUARY, 1); // Tax year 2010-11
        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        then(mockHmrcHateoasClient)
                .should()
                .getSelfAssessmentResource(anyString(), eq("2011-12"), eq("2017-18"), any(Link.class));
    }

    @Test
    public void populateIncomeSummary_toTaxYear5YearsAgo_fromTaxYear6YearsBeforeToYear_fromTaxYearIs6BeforeTaxYearForToday() {
        given(mockIncomeSummaryContext.needsSelfAssessmentResource()).willReturn(true);
        given(mockIncomeSummaryContext.getIncomeLink(anyString())).willReturn(anyLink);

        LocalDate toDate = LocalDate.of(2013, Month.JANUARY, 1); // Tax year 2012-03
        LocalDate fromDate = LocalDate.of(2007, Month.JANUARY, 1); // Tax year 2006-07

        hmrcClient.populateIncomeSummary("any access token", anyIndividual, fromDate, toDate, mockIncomeSummaryContext);

        then(mockHmrcHateoasClient)
                .should()
                .getSelfAssessmentResource(anyString(), eq("2011-12"), eq("2012-13"), any(Link.class));
    }
}
