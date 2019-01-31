package uk.gov.digital.ho.pttg.application;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import uk.gov.digital.ho.pttg.application.domain.Individual;
import uk.gov.digital.ho.pttg.dto.AnnualSelfAssessmentTaxReturn;
import uk.gov.digital.ho.pttg.dto.EmbeddedIndividual;
import uk.gov.digital.ho.pttg.dto.Employment;
import uk.gov.digital.ho.pttg.dto.Income;

import java.util.List;

@Getter
@Setter
@Accessors(fluent = true)
public class IncomeSummaryContext {

    private Resource<String> matchResource;
    private Resource<EmbeddedIndividual> individualResource;
    private Resource<String> incomeResource;
    private Resource<String> employmentResource;
    private Resource<String> selfAssessmentResource;

    private List<Income> payeIncome;
    private List<Employment> employments;
    private List<AnnualSelfAssessmentTaxReturn> selfAssessmentSelfEmploymentIncome;

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
