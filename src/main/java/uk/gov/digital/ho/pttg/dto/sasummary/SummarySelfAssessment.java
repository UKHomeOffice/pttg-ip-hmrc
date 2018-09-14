package uk.gov.digital.ho.pttg.dto.sasummary;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.ResourceSupport;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SummarySelfAssessment extends ResourceSupport {
    private final SummaryTaxReturns selfAssessment;
}