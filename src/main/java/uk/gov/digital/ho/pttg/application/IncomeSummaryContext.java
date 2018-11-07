package uk.gov.digital.ho.pttg.application;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import uk.gov.digital.ho.pttg.application.domain.Individual;
import uk.gov.digital.ho.pttg.dto.AnnualSelfAssessmentTaxReturn;
import uk.gov.digital.ho.pttg.dto.EmbeddedIndividual;
import uk.gov.digital.ho.pttg.dto.Employment;
import uk.gov.digital.ho.pttg.dto.Income;

import java.util.List;

@Accessors(fluent = true)
public class IncomeSummaryContext {

    private Resource<String> matchResource;
    private Resource<EmbeddedIndividual> individualResource;
    private Resource<String> incomeResource;
    private Resource<String> employmentResource;
    private Resource<String> selfAssessmentResource;

    @Getter
    private List<Income> payeIncome;
    @Getter
    private List<Employment> employments;
    @Getter
    private List<AnnualSelfAssessmentTaxReturn> selfAssessmentSelfEmploymentIncome;
    @Getter
    private List<AnnualSelfAssessmentTaxReturn> selfAssessmentSummaryIncome;


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

    boolean needsSelfAssessmentSelfEmploymentIncome() {
        return selfAssessmentSelfEmploymentIncome == null;
    }

    boolean needsSelfAssessmentSummaryIncome() {
        return selfAssessmentSummaryIncome == null;
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

    void setSelfAssessmentSelfEmploymentIncome(List<AnnualSelfAssessmentTaxReturn> selfAssessmentSelfEmploymentIncome) {
        this.selfAssessmentSelfEmploymentIncome = selfAssessmentSelfEmploymentIncome;
    }

    void setSelfAssessmentSummaryIncome(List<AnnualSelfAssessmentTaxReturn> selfAssessmentSummaryIncome) {
        this.selfAssessmentSummaryIncome = selfAssessmentSummaryIncome;
    }

    Individual getIndividual() {
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


}

