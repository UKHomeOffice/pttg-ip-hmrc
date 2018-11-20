package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

public interface NameMatchingCandidateGenerator {

    String ENTIRE_NON_ALIAS_NAME_GENERATOR = "EntireNonAliasName";
    String ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME_GENERATOR = "EntireLastNameAndEachFirstName";
    String MULTIPLE_LAST_NAMES_GENERATOR = "MultipleLastNames";
    String ABBREVIATED_NAMES_GENERATOR = "AbbreviatedNames";
    String ALIAS_COMBINATIONS_GENERATOR = "AliasCombinations";
    String NAME_COMBINATIONS_GENERATOR = "NameCombinations";
    String SPECIAL_CHARACTERS_GENERATOR = "SpecialCharacters";

    enum Generator {
        ENTIRE_NON_ALIAS_NAME,
        ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME,
        MULTIPLE_NAMES,
        ABBREVIATED_NAMES,
        ALIAS_COMBINATIONS,
        NAME_MATCHING,
        SPLITTERS_REMOVED,
        SPLITTERS_REPLACED
    }

    List<CandidateName> generateCandidates(InputNames inputNames);

}
