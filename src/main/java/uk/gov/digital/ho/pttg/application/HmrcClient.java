package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.pttg.application.domain.IncomeSummary;
import uk.gov.digital.ho.pttg.application.domain.Individual;

import java.time.LocalDate;

@Service
@Slf4j
public class HmrcClient {

    private final HmrcHateoasClient hateoasClient;

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

    public HmrcClient(HmrcHateoasClient hateoasClient) {
        this.hateoasClient = hateoasClient;
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
        storeMatchResource(suppliedIndividual, accessToken, context);

        storeIndividualResource(accessToken, context);
        storeIncomeResource(accessToken, context);
        storeEmploymentResource(accessToken, context);
        storeSelfAssessmentResource(accessToken, fromDate, toDate, context);

        storePayeIncome(fromDate, toDate, accessToken, context);
        storeEmployments(fromDate, toDate, accessToken, context);

        storeSelfAssessmentSelfEmploymentIncome(accessToken, context);
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
        if (context.needsSelfAssessmentResource()) {
            context.selfAssessmentResource(hateoasClient.getSelfAssessmentResource(accessToken, fromDate, toDate, context.getIncomeLink(SELF_ASSESSMENT)));
        }
    }

}
