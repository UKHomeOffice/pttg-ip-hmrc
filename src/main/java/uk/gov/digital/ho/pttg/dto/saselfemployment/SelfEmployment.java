package uk.gov.digital.ho.pttg.dto.saselfemployment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class SelfEmployment {
    private final BigDecimal selfEmploymentProfit;
}
