package uk.gov.digital.ho.pttg.application;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.hateoas.Resource;
import uk.gov.digital.ho.pttg.dto.AnnualSelfAssessmentTaxReturn;
import uk.gov.digital.ho.pttg.dto.EmbeddedIndividual;
import uk.gov.digital.ho.pttg.dto.Employment;
import uk.gov.digital.ho.pttg.dto.Income;

import java.util.List;

@Accessors(fluent = true)
@Getter
public class IncomeSummaryContext {

    private Resource<String> matchResource;
    private Resource<EmbeddedIndividual> individualResource;
    private Resource<String> incomeResource;
    private Resource<String> employmentResource;
    private Resource<String> selfAssessmentResource;

    private List<Income> payeIncome;
    private List<Employment> employments;
    private List<AnnualSelfAssessmentTaxReturn> selfAssessmentIncome;
    private List<AnnualSelfAssessmentTaxReturn> summarySelfAssessmentIncome;


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
        return payeIncome == null;
    }

    boolean needsEmployments() {
        return employments == null;
    }

    boolean needsSelfAssessmentIncome() {
        return selfAssessmentIncome == null;
    }

    boolean needsSummarySelfAssessmentIncome() {
        return summarySelfAssessmentIncome == null;
    }

    void setMatchResource(Resource<String> matchResource) {
        this.matchResource = matchResource;
    }

    void setIndividualResource(Resource<EmbeddedIndividual> individualResource) {
        this.individualResource = individualResource;
    }

    void setIncomeResource(Resource<String> incomeResource) {
        this.incomeResource = incomeResource;
    }

    void setEmploymentResource(Resource<String> employmentResource) {
        this.employmentResource = employmentResource;
    }

    void setSelfAssessmentResource(Resource<String> selfAssessmentResource) {
        this.selfAssessmentResource = selfAssessmentResource;
    }

    void setPayeIncome(List<Income> payeIncome) {
        this.payeIncome = payeIncome;
    }

    void setEmployments(List<Employment> employments) {
        this.employments = employments;
    }

    void setSelfAssessmentIncome(List<AnnualSelfAssessmentTaxReturn> selfAssessmentIncome) {
        this.selfAssessmentIncome = selfAssessmentIncome;
    }

    void setSummarySelfAssessmentIncome(List<AnnualSelfAssessmentTaxReturn> summarySelfAssessmentIncome) {
        this.summarySelfAssessmentIncome = summarySelfAssessmentIncome;
    }

}

