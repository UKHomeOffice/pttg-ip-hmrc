package uk.gov.digital.ho.pttg.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.ResourceSupport;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PayeIncome extends ResourceSupport{
    private final Incomes paye;
}


