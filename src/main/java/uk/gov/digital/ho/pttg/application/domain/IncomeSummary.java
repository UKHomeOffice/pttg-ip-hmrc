package uk.gov.digital.ho.pttg.application.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.digital.ho.pttg.dto.AnnualSelfAssessmentTaxReturn;
import uk.gov.digital.ho.pttg.dto.Employment;
import uk.gov.digital.ho.pttg.dto.Income;

import java.util.List;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class IncomeSummary {
    private final List<Income> paye;
    private final List<AnnualSelfAssessmentTaxReturn> selfAssessment;
    private final List<Employment> employments;
    private final Individual individual;
}


