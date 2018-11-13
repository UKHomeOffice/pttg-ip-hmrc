package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

public interface NameMatchingCandidateGenerator {

    int ENTIRE_NON_ALIAS_NAME_STRATEGY_PRIORITY = 1;
    int ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME_PRIORITY = 2;
    int MULTIPLE_NAMES_STRATEGY_PRIORITY = 3;
    int NAMES_WITH_FULL_STOP_SPACE_COMBINATIONS_STRATEGY_PRIORITY = 4;
    int ALIAS_COMBINATIONS_STRATEGY_PRIORITY = 5;
    int NAME_MATCHING_STRATEGY_PRIORITY = 6;
    int SPECIAL_CHARACTERS_STRATEGY_PRIORITY = 7;

    List<CandidateName> generateCandidates(InputNames inputNames);

}
