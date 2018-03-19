package uk.gov.digital.ho.pttg.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AnnualSelfAssessmentTaxReturn {
    private final String taxYear;
    private final BigDecimal selfEmploymentProfit;
}
