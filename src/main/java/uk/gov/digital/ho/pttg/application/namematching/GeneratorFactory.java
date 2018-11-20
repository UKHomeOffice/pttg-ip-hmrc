package uk.gov.digital.ho.pttg.application.namematching;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator;

import java.util.Arrays;
import java.util.List;

import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.*;

@Component
public class GeneratorFactory {

    private List<NameMatchingCandidateGenerator> defaultCandidateGenerators;

    public GeneratorFactory(@Qualifier(ENTIRE_NON_ALIAS_NAME_GENERATOR) NameMatchingCandidateGenerator entireNonAliasName,
                            @Qualifier(ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME_GENERATOR) NameMatchingCandidateGenerator entireLastNameAndEachFirstName,
                            @Qualifier(MULTIPLE_LAST_NAMES_GENERATOR) NameMatchingCandidateGenerator multipleLastNames,
                            @Qualifier(ABBREVIATED_NAMES_GENERATOR) NameMatchingCandidateGenerator abbreviatedNames,
                            @Qualifier(ALIAS_COMBINATIONS_GENERATOR) NameMatchingCandidateGenerator aliasCombinations,
                            @Qualifier(NAME_COMBINATIONS_GENERATOR) NameMatchingCandidateGenerator nameCombinations,
                            @Qualifier(SPECIAL_CHARACTERS_GENERATOR) NameMatchingCandidateGenerator specialCharacters) {

        this.defaultCandidateGenerators = Arrays.asList(
                entireNonAliasName,
                entireLastNameAndEachFirstName,
                multipleLastNames,
                abbreviatedNames,
                aliasCombinations,
                nameCombinations,
                specialCharacters);
    }

    List<NameMatchingCandidateGenerator> createGenerators(InputNames inputNames) {
        return defaultCandidateGenerators;
    }

}
