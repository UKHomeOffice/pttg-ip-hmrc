package uk.gov.digital.ho.pttg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class AnnualSelfAssessmentTaxReturn {
    private final String taxYear;
    private final BigDecimal selfEmploymentProfit;
    private final BigDecimal summaryIncome;
}
