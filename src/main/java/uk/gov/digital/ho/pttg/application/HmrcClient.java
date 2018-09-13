package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.pttg.dto.*;

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
    private static final String SUMMARY_SELF_ASSESSMENT = "summary";
    private static final String PAYE_INCOME = "paye";
    private static final String PAYE_EMPLOYMENT = "paye";
    private static final String SELF_EMPLOYMENTS = "selfEmployments";

    @Value("${hmrc.sa.self-employment-only}")
    private boolean selfEmploymentOnly;


    public HmrcClient(HmrcHateoasClient hateoasClient) {
        this.hateoasClient = hateoasClient;
    }

    public IncomeSummary getIncomeSummary(String accessToken, Individual suppliedIndividual, LocalDate fromDate, LocalDate toDate, IncomeSummaryContext context) {

        log.debug("Attempt to retrieve HMRC data for {}", suppliedIndividual.getNino());

        getHmrcData(accessToken, suppliedIndividual, fromDate, toDate, context);

        enrichIncomeData(context.payeIncome(), context.employments());

        log.debug("Successfully retrieved HMRC data for {}", suppliedIndividual.getNino());

        List<AnnualSelfAssessmentTaxReturn> selfAssessmentIncome;
        if (selfEmploymentOnly) {
            selfAssessmentIncome = context.selfAssessmentIncome();
        } else {
            selfAssessmentIncome = context.summarySelfAssessmentIncome();
        }

        return new IncomeSummary(
                context.payeIncome(),
                selfAssessmentIncome,
                context.employments(),
                context.individualResource().getContent().getIndividual());
    }

    private void getHmrcData(String accessToken, Individual suppliedIndividual, LocalDate fromDate, LocalDate toDate, IncomeSummaryContext context) {
        storeMatchResource(suppliedIndividual, accessToken, context);

        storeIndividualResource(accessToken, context);
        storeIncomeResource(accessToken, context);
        storeEmploymentResource(accessToken, context);
        storeSelfAssessmentResource(accessToken, fromDate, toDate, context);

        storePayeIncome(fromDate, toDate, accessToken, context);
        storeEmployments(fromDate, toDate, accessToken, context);
        if (selfEmploymentOnly) {
            storeSelfAssessmentIncome(accessToken, context);
        } else {
            storeSummarySelfAssessmentIncome(accessToken, context);
        }

    }

    private void storeEmployments(LocalDate fromDate, LocalDate toDate, String accessToken, IncomeSummaryContext context) {
        if (context.needsEmployments()) {
            context.setEmployments(hateoasClient.getEmployments(fromDate, toDate, accessToken, context.employmentResource().getLink(PAYE_EMPLOYMENT)));
        }
    }

    private void storePayeIncome(LocalDate fromDate, LocalDate toDate, String accessToken, IncomeSummaryContext context) {
        if (context.needsPayeIncome()) {
            context.setPayeIncome(hateoasClient.getPayeIncome(fromDate, toDate, accessToken, context.incomeResource().getLink(PAYE_INCOME)));
        }
    }

    private void storeSelfAssessmentIncome(String accessToken, IncomeSummaryContext context) {
        if (context.needsSelfAssessmentIncome()) {
            context.setSelfAssessmentIncome(hateoasClient.getSelfAssessmentIncome(accessToken, context.selfAssessmentResource().getLink(SELF_EMPLOYMENTS)));
        }
    }

    private void storeSummarySelfAssessmentIncome(String accessToken, IncomeSummaryContext context) {
        if (context.needsSummarySelfAssessmentIncome()) {
            context.setSummarySelfAssessmentIncome(hateoasClient.getSummarySelfAssessmentIncome(accessToken, context.selfAssessmentResource().getLink(SUMMARY_SELF_ASSESSMENT)));
        }
    }

    private void storeMatchResource(Individual individual, String accessToken, IncomeSummaryContext context) {
        if (context.needsMatchResource()) {
            context.setMatchResource(hateoasClient.getMatchResource(individual, accessToken));
        }
    }

    private void storeIndividualResource(String accessToken, IncomeSummaryContext context) {
        if (context.needsIndividualResource()) {
            context.setIndividualResource(hateoasClient.getIndividualResource(accessToken, context.matchResource().getLink(INDIVIDUAL)));
        }
    }

    private void storeIncomeResource(String accessToken, IncomeSummaryContext context) {
        if (context.needsIncomeResource()) {
            context.setIncomeResource(hateoasClient.getIncomeResource(accessToken, context.individualResource().getLink(INCOME)));
        }
    }

    private void storeEmploymentResource(String accessToken, IncomeSummaryContext context) {
        if (context.needsEmploymentResource()) {
            context.setEmploymentResource(hateoasClient.getEmploymentResource(accessToken, context.individualResource().getLink(EMPLOYMENTS)));
        }
    }

    private void storeSelfAssessmentResource(String accessToken, LocalDate fromDate, LocalDate toDate, IncomeSummaryContext context) {
        if (context.needsSelfAssessmentResource()) {
            context.setSelfAssessmentResource(hateoasClient.getSelfAssessmentResource(accessToken, fromDate, toDate, context.incomeResource().getLink(SELF_ASSESSMENT)));
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
