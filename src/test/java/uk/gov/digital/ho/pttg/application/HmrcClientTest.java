package uk.gov.digital.ho.pttg.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;
import uk.gov.digital.ho.pttg.application.domain.Individual;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static uk.gov.digital.ho.pttg.application.HmrcClientFunctions.getTaxYear;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_CALL_SKIPPED_SMOKE_TEST;

public class HmrcClientTest {

    private static final LocalDate DEFAULT_PAYE_EPOCH = LocalDate.of(2013, Month.MARCH, 31);
    private static final LocalDate ANY_DATE = LocalDate.now();

    @Mock private Link anyLink;
    @Mock private Individual anyIndividual;
    @Mock private HmrcHateoasClient mockHmrcHateoasClient;
    @Mock private IncomeSummaryContext mockIncomeSummaryContext;
    @Mock private RequestHeaderData mockRequestHeaderData;
    @Mock private Appender<ILoggingEvent> mockLogAppender;

    private HmrcClient hmrcClient;

    @Rule
    public MockitoRule unnecessaryStubsRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Before
    public void setUp() {
        hmrcClient = new HmrcClient(mockHmrcHateoasClient, 6, DEFAULT_PAYE_EPOCH, mockRequestHeaderData);

        Logger logger = (Logger) LoggerFactory.getLogger(HmrcClient.class);
        logger.setLevel(Level.INFO);
        logger.addAppender(mockLogAppender);
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
    public void populateIncomeSummary_isASmokeTest_skipHmrcCalls() {
        lenientStubs();
        given_contextNeedsAllResources();
        given(mockRequestHeaderData.isASmokeTest()).willReturn(true);

        hmrcClient.populateIncomeSummary("any access token", anyIndividual, ANY_DATE, ANY_DATE, mockIncomeSummaryContext);

        then(mockHmrcHateoasClient).shouldHaveZeroInteractions();
        then_context_shouldNeverBeUpdated();
    }

    /**
     * Ordinarily Mockito complains if you have any stubs that don't get called. However the test case for
     * populateIncomeSummary_isASmokeTest_skipHmrcCalls tests that "isASmokeTest" stops HMRC being called.
     * Setting mockIncomeSummaryContext.needsEmploymentResource etc to return false (or not stubbing at all) would have
     * the same effect but we'd be testing the wrong thing. Therefore we need to stub these to return true if called so that
     * the correct behaviour is under test.
     */
    private void lenientStubs() {
        unnecessaryStubsRule.strictness(Strictness.LENIENT);
    }

    @Test
    public void populateIncomeSummary_isASmokeTest_logSkippedHmrCall() {
        given(mockRequestHeaderData.isASmokeTest()).willReturn(true);

        hmrcClient.populateIncomeSummary("any access token", anyIndividual, ANY_DATE, ANY_DATE, mockIncomeSummaryContext);

        ArgumentCaptor<LoggingEvent> logCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
        then(mockLogAppender).should().doAppend(logCaptor.capture());

        LoggingEvent logEvent = logCaptor.getValue();
        assertThat(logEvent.getFormattedMessage()).containsIgnoringCase("Smoke Test")
                                                  .containsIgnoringCase("skipped");
        assertThat(logEvent.getArgumentArray()).contains(new ObjectAppendingMarker(EVENT, HMRC_CALL_SKIPPED_SMOKE_TEST));
    }

    private void given_contextNeedsAllResources() {
        given(mockIncomeSummaryContext.needsEmploymentResource()).willReturn(true);
        given(mockIncomeSummaryContext.needsEmployments()).willReturn(true);
        given(mockIncomeSummaryContext.needsIncomeResource()).willReturn(true);
        given(mockIncomeSummaryContext.needsIndividualResource()).willReturn(true);
        given(mockIncomeSummaryContext.needsMatchResource()).willReturn(true);
        given(mockIncomeSummaryContext.needsPayeIncome()).willReturn(true);
        given(mockIncomeSummaryContext.needsSelfAssessmentResource()).willReturn(true);
        given(mockIncomeSummaryContext.needsSelfAssessmentSelfEmploymentIncome()).willReturn(true);
    }

    private void then_context_shouldNeverBeUpdated() {
        then(mockIncomeSummaryContext).should(never()).matchResource(any());
        then(mockIncomeSummaryContext).should(never()).individualResource(any());
        then(mockIncomeSummaryContext).should(never()).incomeResource(any());
        then(mockIncomeSummaryContext).should(never()).employmentResource(any());
        then(mockIncomeSummaryContext).should(never()).selfAssessmentResource(any());
        then(mockIncomeSummaryContext).should(never()).payeIncome(any());
        then(mockIncomeSummaryContext).should(never()).employments(any());
        then(mockIncomeSummaryContext).should(never()).selfAssessmentSelfEmploymentIncome(any());
    }
}
