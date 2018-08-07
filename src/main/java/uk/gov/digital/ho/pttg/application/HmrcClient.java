package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.pttg.dto.Employment;
import uk.gov.digital.ho.pttg.dto.Income;
import uk.gov.digital.ho.pttg.dto.IncomeSummary;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class HmrcClient {

    private final HmrcHateoasClient hateoasClient;
    private final NinoUtils ninoUtils;

    private static final String DEFAULT_PAYMENT_FREQUENCY = "ONE_OFF";

    /*
        Hypermedia paths and links
    */
    static final String INDIVIDUAL = "individual";
    static final String INCOME = "income";
    static final String EMPLOYMENTS = "employments";
    static final String SELF_ASSESSMENT = "selfAssessment";
    static final String PAYE_INCOME = "paye";
    static final String PAYE_EMPLOYMENT = "paye";
    static final String SELF_EMPLOYMENTS = "selfEmployments";


    public HmrcClient(HmrcHateoasClient hateoasClient,
                      NinoUtils ninoUtils) {
        this.hateoasClient = hateoasClient;
        this.ninoUtils = ninoUtils;
    }

    public IncomeSummary getIncomeSummary(String accessToken, Individual suppliedIndividual, LocalDate fromDate, LocalDate toDate, IncomeSummaryContext context) {

        String redactedNino = ninoUtils.redact(suppliedIndividual.getNino());

        log.info("Attempt to retrieve HMRC data for {}", redactedNino);

        getHmrcData(accessToken, suppliedIndividual, fromDate, toDate, context);

        enrichIncomeData(context.payeIncome(), context.employments());

        log.info("Successfully retrieved HMRC data for {}", redactedNino);

        return new IncomeSummary(
                context.payeIncome(),
                context.selfAssessmentIncome(),
                context.employments(),
                context.individualResource().getContent().getIndividual());
    }

    private void getHmrcData(String accessToken, Individual suppliedIndividual, LocalDate fromDate, LocalDate toDate, IncomeSummaryContext context) {
        String nino = suppliedIndividual.getNino();

        storeMatchResource(suppliedIndividual, accessToken, context);

        storeIndividualResource(nino, accessToken, context);
        storeIncomeResource(nino, accessToken, context);
        storeEmploymentResource(nino, accessToken, context);
        storeSelfAssessmentResource(nino, accessToken, fromDate, toDate, context);

        storePayeIncome(fromDate, toDate, accessToken, context);
        storeEmployments(fromDate, toDate, accessToken, context);
        storeSelfAssessmentIncome(accessToken, context);

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

    private void storeMatchResource(Individual individual, String accessToken, IncomeSummaryContext context) {
        if (context.needsMatchResource()) {
            context.setMatchResource(hateoasClient.getMatchResource(individual, accessToken));
        }
    }

    private void storeIndividualResource(String nino, String accessToken, IncomeSummaryContext context) {
        if (context.needsIndividualResource()) {
            context.setIndividualResource(hateoasClient.getIndividualResource(nino, accessToken, context.matchResource().getLink(INDIVIDUAL)));
        }
    }

    private void storeIncomeResource(String nino, String accessToken, IncomeSummaryContext context) {
        if (context.needsIncomeResource()) {
            context.setIncomeResource(hateoasClient.getIncomeResource(nino, accessToken, context.individualResource().getLink(INCOME)));
        }
    }

    private void storeEmploymentResource(String nino, String accessToken, IncomeSummaryContext context) {
        if (context.needsEmploymentResource()) {
            context.setEmploymentResource(hateoasClient.getEmploymentResource(nino, accessToken, context.individualResource().getLink(EMPLOYMENTS)));
        }
    }

    private void storeSelfAssessmentResource(String nino, String accessToken, LocalDate fromDate, LocalDate toDate, IncomeSummaryContext context) {
        if (context.needsSelfAssessmentResource()) {
            context.setSelfAssessmentResource(hateoasClient.getSelfAssessmentResource(nino, accessToken, fromDate, toDate, context.incomeResource().getLink(SELF_ASSESSMENT)));
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
