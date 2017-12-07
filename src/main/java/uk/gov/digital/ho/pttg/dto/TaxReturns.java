package uk.gov.digital.ho.pttg.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.ResourceSupport;

import java.util.List;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaxReturns extends ResourceSupport {
    private final List<TaxReturn> taxReturns;
}

