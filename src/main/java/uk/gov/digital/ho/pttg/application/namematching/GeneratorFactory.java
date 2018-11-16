package uk.gov.digital.ho.pttg.application.namematching;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator;

import java.util.List;

@Component
public class GeneratorFactory {

    private List<NameMatchingCandidateGenerator> candidateGenerators;

    public GeneratorFactory(List<NameMatchingCandidateGenerator> candidateGenerators) {
        this.candidateGenerators = candidateGenerators;
    }

    List<NameMatchingCandidateGenerator> createGenerators(InputNames inputNames) {
        return candidateGenerators;
    }
}
