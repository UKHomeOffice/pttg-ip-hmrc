package uk.gov.digital.ho.pttg.dto.saselfemployment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class SelfEmploymentTaxReturn {
    private final String taxYear;
    private final List<SelfEmployment> selfEmployments;
}
