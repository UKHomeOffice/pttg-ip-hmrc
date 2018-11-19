package uk.gov.digital.ho.pttg.application.namematching;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
@ToString
public class CandidateDerivation {

    @JsonProperty(value = "inputNames")
    private InputNames inputNames;

    @JsonProperty(value = "generators")
    private List<Generator> generators;

    @JsonProperty(value = "firstName")
    private NameDerivation firstName;

    @JsonProperty(value = "lastName")
    private NameDerivation lastName;

    public void addGenerator(Generator generator) {
        List<Generator> enlargedGenerators = new ArrayList<>();
        enlargedGenerators.add(generator);
        enlargedGenerators.addAll(generators);
        generators = Collections.unmodifiableList(enlargedGenerators);
    }
}
