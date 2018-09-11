package uk.gov.digital.ho.pttg.dto.selfemployment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.ResourceSupport;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaxReturns extends ResourceSupport {
    private final List<TaxReturn> taxReturns;
}