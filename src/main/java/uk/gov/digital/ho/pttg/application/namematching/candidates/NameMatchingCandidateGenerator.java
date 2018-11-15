package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

public interface NameMatchingCandidateGenerator {

    int ENTIRE_NON_ALIAS_NAME_GENERATOR_PRIORITY = 1;
    int ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME_GENERATOR_PRIORITY = 2;
    int MULTIPLE_NAMES_GENERATOR_PRIORITY = 3;
    int NAMES_WITH_FULL_STOP_SPACE_COMBINATIONS_GENERATOR_PRIORITY = 4;
    int ALIAS_COMBINATIONS_GENERATOR_PRIORITY = 5;
    int NAME_MATCHING_GENERATOR_PRIORITY = 6;
    int SPECIAL_CHARACTERS_GENERATOR_PRIORITY = 7;

    enum Generator {
        ENTIRE_NON_ALIAS_NAME,
        ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME,
        MULTIPLE_NAMES,
        NAMES_WITH_FULL_STOP_SPACE_COMBINATIONS,
        ALIAS_COMBINATIONS,
        NAME_MATCHING,
        SPECIAL_CHARACTERS
    }

    List<CandidateName> generateCandidates(InputNames inputNames);

}
