package uk.gov.digital.ho.pttg.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.pttg.application.domain.Individual;

@AllArgsConstructor
@Getter
public class EmbeddedIndividual {
    private final Individual individual;
}
