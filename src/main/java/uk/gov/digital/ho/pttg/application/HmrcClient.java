package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;
import uk.gov.digital.ho.pttg.application.domain.IncomeSummary;
import uk.gov.digital.ho.pttg.application.domain.Individual;

import java.time.LocalDate;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.application.HmrcClientFunctions.getTaxYear;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_CALL_SKIPPED_SMOKE_TEST;

@Service
@Slf4j
public class HmrcClient {

    private final HmrcHateoasClient hateoasClient;
    private final int maximumTaxYearHistory;
    private final LocalDate payeDataEpoch;
    private final RequestHeaderData requestHeaderData;

    /*
        Hypermedia paths and links
    */
    private static final String INDIVIDUAL = "individual";
    private static final String INCOME = "income";
    private static final String EMPLOYMENTS = "employments";
    private static final String SELF_ASSESSMENT = "selfAssessment";
    private static final String PAYE_INCOME = "paye";
    private static final String PAYE_EMPLOYMENT = "paye";
    private static final String SA_SELF_EMPLOYMENTS = "selfEmployments";

    public HmrcClient(HmrcHateoasClient hateoasClient,
                      @Value("${hmrc.self-assessment.tax-years.history.maximum}") int maximumSelfAssessmentTaxYearHistory,
                      @Value("#{T(java.time.LocalDate).parse('${hmrc.paye.data.epoch}')}") LocalDate payeDataEpoch,
                      RequestHeaderData requestHeaderData) {
        this.hateoasClient = hateoasClient;
        this.maximumTaxYearHistory = maximumSelfAssessmentTaxYearHistory;
        this.payeDataEpoch = payeDataEpoch;
        this.requestHeaderData = requestHeaderData;
    }

    public IncomeSummary populateIncomeSummary(String accessToken, Individual suppliedIndividual, LocalDate fromDate, LocalDate toDate, IncomeSummaryContext context) {

        log.debug("Attempt to retrieve HMRC data for {}", suppliedIndividual.getNino());

        getHmrcData(accessToken, suppliedIndividual, fromDate, toDate, context);

        context.enrichIncomeData();

        IncomeSummary incomeSummary = new IncomeSummary(
                context.payeIncome(),
                context.selfAssessmentSelfEmploymentIncome(),
                context.employments(),
                context.getIndividual());

        log.debug("Successfully retrieved HMRC data for {}", suppliedIndividual.getNino());

        return incomeSummary;
    }

    private void getHmrcData(String accessToken, Individual suppliedIndividual, LocalDate fromDate, LocalDate toDate, IncomeSummaryContext context) {
        if (requestHeaderData.isASmokeTest()) {
            log.info("Skipped HMRC calls because request is a smoke test", value(EVENT, HMRC_CALL_SKIPPED_SMOKE_TEST));
            return;
        }
        storeMatchResource(suppliedIndividual, accessToken, context);

        storeIndividualResource(accessToken, context);
        storeIncomeResource(accessToken, context);
        storeEmploymentResource(accessToken, context);
        storeSelfAssessmentResource(accessToken, fromDate, toDate, context);
        storePayeData(accessToken, fromDate, toDate, context);

        storeSelfAssessmentSelfEmploymentIncome(accessToken, context);
    }

    private void storePayeData(String accessToken, LocalDate fromDate, LocalDate toDate, IncomeSummaryContext context) {
        LocalDate payeFromDate = fromDate.isAfter(payeDataEpoch) ? fromDate : payeDataEpoch;

        storePayeIncome(payeFromDate, toDate, accessToken, context);
        storeEmployments(payeFromDate, toDate, accessToken, context);
    }

    private void storeEmployments(LocalDate fromDate, LocalDate toDate, String accessToken, IncomeSummaryContext context) {
        if (context.needsEmployments()) {
            context.employments(hateoasClient.getEmployments(fromDate, toDate, accessToken, context.getEmploymentLink(PAYE_EMPLOYMENT)));
        }
    }

    private void storePayeIncome(LocalDate fromDate, LocalDate toDate, String accessToken, IncomeSummaryContext context) {
        if (context.needsPayeIncome()) {
            context.payeIncome(hateoasClient.getPayeIncome(fromDate, toDate, accessToken, context.getIncomeLink(PAYE_INCOME)));
        }
    }

    private void storeSelfAssessmentSelfEmploymentIncome(String accessToken, IncomeSummaryContext context) {
        if (context.needsSelfAssessmentSelfEmploymentIncome()) {
            context.selfAssessmentSelfEmploymentIncome(hateoasClient.getSelfAssessmentSelfEmploymentIncome(accessToken, context.getSelfAssessmentLink(SA_SELF_EMPLOYMENTS)));
        }
    }

    private void storeMatchResource(Individual individual, String accessToken, IncomeSummaryContext context) {
        if (context.needsMatchResource()) {
            context.matchResource(hateoasClient.getMatchResource(individual, accessToken));
        }
    }

    private void storeIndividualResource(String accessToken, IncomeSummaryContext context) {
        if (context.needsIndividualResource()) {
            context.individualResource(hateoasClient.getIndividualResource(accessToken, context.getMatchLink(INDIVIDUAL)));
        }
    }

    private void storeIncomeResource(String accessToken, IncomeSummaryContext context) {
        if (context.needsIncomeResource()) {
            context.incomeResource(hateoasClient.getIncomeResource(accessToken, context.getIndividualLink(INCOME)));
        }
    }

    private void storeEmploymentResource(String accessToken, IncomeSummaryContext context) {
        if (context.needsEmploymentResource()) {
            context.employmentResource(hateoasClient.getEmploymentResource(accessToken, context.getIndividualLink(EMPLOYMENTS)));
        }
    }

    private void storeSelfAssessmentResource(String accessToken, LocalDate fromDate, LocalDate toDate, IncomeSummaryContext context) {
        String toTaxYear = getTaxYear(toDate);
        String fromTaxYear = getTaxYear(fromDate);
        if (isTooLongAgo(fromTaxYear)) {
            fromTaxYear = earliestAllowedTaxYear();
        }

        storeSelfAssessmentResource(accessToken, fromTaxYear, toTaxYear, context);
    }

    private void storeSelfAssessmentResource(String accessToken, String fromTaxYear, String toTaxYear, IncomeSummaryContext context) {
        if (context.needsSelfAssessmentResource()) {
            context.selfAssessmentResource(hateoasClient.getSelfAssessmentResource(accessToken, fromTaxYear, toTaxYear, context.getIncomeLink(SELF_ASSESSMENT)));
        }
    }

    private String earliestAllowedTaxYear() {
        return getTaxYear(LocalDate.now().minusYears(maximumTaxYearHistory));
    }

    private boolean isTooLongAgo(String taxYear) {
        return Integer.parseInt(taxYear.substring(0, 4)) < Integer.parseInt(earliestAllowedTaxYear().substring(0, 4));
    }
}
