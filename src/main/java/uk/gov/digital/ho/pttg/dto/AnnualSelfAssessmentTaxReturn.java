package uk.gov.digital.ho.pttg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@EqualsAndHashCode
public class AnnualSelfAssessmentTaxReturn {
    @JsonProperty private String taxYear;
    @JsonProperty private BigDecimal selfEmploymentProfit;
    @JsonProperty private BigDecimal summaryIncome;

    public AnnualSelfAssessmentTaxReturn(String taxYear, BigDecimal selfEmploymentProfit, BigDecimal summaryIncome) {
        this.taxYear = taxYear;
        this.selfEmploymentProfit = selfEmploymentProfit != null ? selfEmploymentProfit : new BigDecimal("0");
        this.summaryIncome = summaryIncome != null ? summaryIncome : new BigDecimal("0");
    }
}
