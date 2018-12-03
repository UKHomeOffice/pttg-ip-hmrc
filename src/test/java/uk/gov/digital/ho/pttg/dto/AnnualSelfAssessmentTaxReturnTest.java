package uk.gov.digital.ho.pttg.dto;

import org.junit.Test;

import static java.math.BigDecimal.*;
import static org.assertj.core.api.Assertions.assertThat;

public class AnnualSelfAssessmentTaxReturnTest {

    @Test
    public void shouldNotDefaultSelfEmploymentProfit() {
        AnnualSelfAssessmentTaxReturn annualSelfAssessmentTaxReturn = new AnnualSelfAssessmentTaxReturn(
                "some tax year",
                ONE,
                TEN);

        assertThat(annualSelfAssessmentTaxReturn.getSelfEmploymentProfit()).isEqualTo(ONE);
        assertThat(annualSelfAssessmentTaxReturn.getSummaryIncome()).isEqualTo(TEN);
    }

    @Test
    public void shouldDefaultSelfEmploymentProfit() {
        AnnualSelfAssessmentTaxReturn annualSelfAssessmentTaxReturn = new AnnualSelfAssessmentTaxReturn(
                "some tax year",
                null,
                null);

        assertThat(annualSelfAssessmentTaxReturn.getSelfEmploymentProfit()).isEqualTo(ZERO);
        assertThat(annualSelfAssessmentTaxReturn.getSummaryIncome()).isEqualTo(ZERO);
    }
}