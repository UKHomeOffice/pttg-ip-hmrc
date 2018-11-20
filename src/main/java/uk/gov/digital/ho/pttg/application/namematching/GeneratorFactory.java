package uk.gov.digital.ho.pttg.application.namematching;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator;

import java.util.Arrays;
import java.util.List;

@Component
public class GeneratorFactory {

    private List<NameMatchingCandidateGenerator> defaultCandidateGenerators;

    public GeneratorFactory(NameMatchingCandidateGenerator entireNonAliasName,
                            NameMatchingCandidateGenerator entireLastNameAndEachFirstName,
                            NameMatchingCandidateGenerator multipleLastNames,
                            NameMatchingCandidateGenerator namesWithFullStopSpaceCombinations,
                            NameMatchingCandidateGenerator aliasCombinations,
                            NameMatchingCandidateGenerator nameCombinations,
                            NameMatchingCandidateGenerator specialCharacters) {

        this.defaultCandidateGenerators = Arrays.asList(
                entireNonAliasName,
                entireLastNameAndEachFirstName,
                multipleLastNames,
                namesWithFullStopSpaceCombinations,
                aliasCombinations,
                nameCombinations,
                specialCharacters);
    }

    List<NameMatchingCandidateGenerator> createGenerators(InputNames inputNames) {
        return defaultCandidateGenerators;
    }

}
