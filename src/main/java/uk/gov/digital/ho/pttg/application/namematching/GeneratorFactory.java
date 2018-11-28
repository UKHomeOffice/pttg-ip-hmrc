package uk.gov.digital.ho.pttg.application.namematching;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.candidates.*;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class GeneratorFactory {

    private List<NameMatchingCandidateGenerator> defaultCandidateGenerators;

    public GeneratorFactory(EntireNonAliasName entireNonAliasName,
                            EntireLastNameAndEachFirstName entireLastNameAndEachFirstName,
                            MultipleLastNames multipleLastNames,
                            AbbreviatedNames abbreviatedNames,
                            AliasCombinations aliasCombinations,
                            NameCombinations nameCombinations,
                            SpecialCharacters specialCharacters) {

        this.defaultCandidateGenerators = Arrays.asList(
                entireNonAliasName,
                entireLastNameAndEachFirstName,
                multipleLastNames,
                abbreviatedNames,
                aliasCombinations,
                nameCombinations,
                specialCharacters);
    }

    public List<NameMatchingCandidateGenerator> createGenerators(InputNames inputNames) {
        log.debug("Use default generator set for input data: {}", inputNames);
        return defaultCandidateGenerators;
    }

}
