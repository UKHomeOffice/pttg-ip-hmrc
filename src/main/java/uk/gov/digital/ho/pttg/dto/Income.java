package uk.gov.digital.ho.pttg.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class Income {
    private final String employerPayeReference;
    private final BigDecimal taxablePayment;
    private final BigDecimal nonTaxablePayment;
    private final String paymentDate;
    private Integer weekPayNumber;
    private Integer monthPayNumber;

    @Setter
    private String paymentFrequency;
}
