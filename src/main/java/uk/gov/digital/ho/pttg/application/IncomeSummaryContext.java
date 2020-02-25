package uk.gov.digital.ho.pttg.application;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import uk.gov.digital.ho.pttg.application.domain.Individual;
import uk.gov.digital.ho.pttg.dto.AnnualSelfAssessmentTaxReturn;
import uk.gov.digital.ho.pttg.dto.EmbeddedIndividual;
import uk.gov.digital.ho.pttg.dto.Employment;
import uk.gov.digital.ho.pttg.dto.Income;

import java.util.*;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.application.IncomeDataTypes.PAYE;
import static uk.gov.digital.ho.pttg.application.IncomeDataTypes.SELF_ASSESSMENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_EMPLOYERLESS_EMPLOYMENT;

@Getter
@Setter
@Accessors(fluent = true)
@Slf4j
public class IncomeSummaryContext {

    private static final String DEFAULT_PAYMENT_FREQUENCY = "ONE_OFF";

    private Resource<String> matchResource;
    private Resource<EmbeddedIndividual> individualResource;
    private Resource<String> incomeResource;
    private Resource<String> employmentResource;
    private Resource<String> selfAssessmentResource;

    private List<Income> payeIncome;
    private List<Employment> employments;
    private List<AnnualSelfAssessmentTaxReturn> selfAssessmentSelfEmploymentIncome;


    public IncomeSummaryContext() {
        payeIncome = new ArrayList<>();
        employments = new ArrayList<>();
        selfAssessmentSelfEmploymentIncome = new ArrayList<>();
    }

    public void payeIncome(List<Income> payeIncome) {
        this.payeIncome = Objects.requireNonNull(payeIncome);
    }

    public void employments(List<Employment> employments) {
        this.employments = Objects.requireNonNull(employments);
    }

    public void selfAssessmentSelfEmploymentIncome(List<AnnualSelfAssessmentTaxReturn> selfAssessmentSelfEmploymentIncome) {
        this.selfAssessmentSelfEmploymentIncome = Objects.requireNonNull(selfAssessmentSelfEmploymentIncome);
    }

    boolean needsMatchResource() {
        return matchResource == null;
    }

    boolean needsIndividualResource() {
        return individualResource == null;
    }

    boolean needsIncomeResource() {
        return incomeResource == null;
    }

    boolean needsEmploymentResource() {
        return employmentResource == null;
    }

    boolean needsSelfAssessmentResource() {
        return selfAssessmentResource == null;
    }

    boolean needsPayeIncome() {
        return payeIncome.isEmpty();
    }

    boolean needsEmployments() {
        return employments.isEmpty();
    }

    boolean needsSelfAssessmentSelfEmploymentIncome() {
        return selfAssessmentSelfEmploymentIncome.isEmpty();
    }

    public Individual getIndividual() {
        return individualResource.getContent().getIndividual();
    }

    Link getMatchLink(String rel) {
        return matchResource.getLink(rel);
    }

    Link getEmploymentLink(String rel) {
        return employmentResource.getLink(rel);
    }

    Link getIndividualLink(String rel) {
        return individualResource.getLink(rel);
    }

    Link getIncomeLink(String rel) {
        return incomeResource.getLink(rel);
    }

    Link getSelfAssessmentLink(String rel) {
        return selfAssessmentResource.getLink(rel);
    }

    public List<IncomeDataTypes> availableIncomeData() {
        List<IncomeDataTypes> available = new ArrayList<>();

        if (!needsPayeIncome()) {
            available.add(PAYE);
        }

        if (!needsSelfAssessmentSelfEmploymentIncome()) {
            available.add(SELF_ASSESSMENT);
        }

        return Collections.unmodifiableList(available);
    }


    void enrichIncomeData() {
        Map<String, String> employerPaymentRefMap = createEmployerPaymentRefMap();
        addPaymentFrequency(employerPaymentRefMap);
    }

    void addPaymentFrequency(Map<String, String> employerPaymentRefMap) {
        payeIncome.forEach(income -> income.setPaymentFrequency(employerPaymentRefMap.get(income.getEmployerPayeReference())));
    }

    Map<String, String> createEmployerPaymentRefMap() {
        Map<String, String> paymentFrequency = new HashMap<>();

        for (Employment employment : employments) {

            if (employment.withoutEmployer()) {
                log.warn("HMRC Employer data without an Employer", value(EVENT, HMRC_EMPLOYERLESS_EMPLOYMENT));
                continue;
            }

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
