package uk.gov.digital.ho.pttg.application.namematching;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public class CandidateDerivation {

    @JsonProperty(value = "generators")
    private List<Integer> generators;

    @JsonProperty(value = "firstName")
    private Derivation firstName;

    @JsonProperty(value = "lastName")
    private Derivation lastName;
}
