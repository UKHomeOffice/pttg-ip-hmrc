package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.pttg.application.domain.IncomeSummary;
import uk.gov.digital.ho.pttg.application.domain.Individual;
import uk.gov.digital.ho.pttg.dto.Employment;
import uk.gov.digital.ho.pttg.dto.Income;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class HmrcClient {

    private final HmrcHateoasClient hateoasClient;

    private static final String DEFAULT_PAYMENT_FREQUENCY = "ONE_OFF";

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

    public IncomeSummary getIncomeSummary(String accessToken, Individual suppliedIndividual, LocalDate fromDate, LocalDate toDate, IncomeSummaryContext context) {

        log.debug("Attempt to retrieve HMRC data for {}", suppliedIndividual.getNino());

        getHmrcData(accessToken, suppliedIndividual, fromDate, toDate, context);

        enrichIncomeData(context.payeIncome(), context.employments());

        log.debug("Successfully retrieved HMRC data for {}", suppliedIndividual.getNino());

        return new IncomeSummary(
                context.payeIncome(),
                context.selfAssessmentSelfEmploymentIncome(),
                context.employments(),
                context.getIndividual());
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

    private void enrichIncomeData(List<Income> incomes, List<Employment> employments) {
        Map<String, String> employerPaymentRefMap = createEmployerPaymentRefMap(employments);
        addPaymentFrequency(incomes, employerPaymentRefMap);
    }

    void addPaymentFrequency(List<Income> incomes, Map<String, String> employerPaymentRefMap) {
        if (incomes == null) {
            return;
        }

        incomes.forEach(income -> income.setPaymentFrequency(employerPaymentRefMap.get(income.getEmployerPayeReference())));
    }

    Map<String, String> createEmployerPaymentRefMap(List<Employment> employments) {
        Map<String, String> paymentFrequency = new HashMap<>();

        for (Employment employment : employments) {

            String payeReference = employment.getEmployer().getPayeReference();

            if (StringUtils.isEmpty(employment.getPayFrequency())) {
                paymentFrequency.put(payeReference, DEFAULT_PAYMENT_FREQUENCY);
            } else {
                paymentFrequency.put(payeReference, employment.getPayFrequency());
            }

        }

        return paymentFrequency;
    }
}
