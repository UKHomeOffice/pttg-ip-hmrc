package uk.gov.digital.ho.pttg.application.namematching;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator;

import java.util.List;

@Component
public class GeneratorFactory {

    private List<NameMatchingCandidateGenerator> defaultCandidateGenerators;

    public GeneratorFactory(List<NameMatchingCandidateGenerator> defaultCandidateGenerators) {
        this.defaultCandidateGenerators = defaultCandidateGenerators;
    }

    List<NameMatchingCandidateGenerator> createGenerators(InputNames inputNames) {
        return defaultCandidateGenerators;
    }
}
