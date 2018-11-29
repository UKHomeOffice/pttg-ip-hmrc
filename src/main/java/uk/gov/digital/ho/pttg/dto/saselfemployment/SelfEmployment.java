package uk.gov.digital.ho.pttg.dto.saselfemployment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelfEmployment {
    private BigDecimal selfEmploymentProfit;

    @JsonProperty
    private void setSelfEmploymentProfit(BigDecimal selfEmploymentProfit) {
        this.selfEmploymentProfit = selfEmploymentProfit != null ? selfEmploymentProfit : new BigDecimal("0");
    }

}
