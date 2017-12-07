package uk.gov.digital.ho.pttg.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.ResourceSupport;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SelfAssessment extends ResourceSupport{
    private final TaxReturns selfAssessment;
}


