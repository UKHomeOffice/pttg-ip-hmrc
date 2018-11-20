package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

public interface NameMatchingCandidateGenerator {

    enum Generator {
        ENTIRE_NON_ALIAS_NAME,
        ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME,
        MULTIPLE_NAMES,
        NAMES_WITH_FULL_STOP_SPACE_COMBINATIONS,
        ALIAS_COMBINATIONS,
        NAME_MATCHING,
        SPLITTERS_REMOVED,
        SPLITTERS_REPLACED
    }

    List<CandidateName> generateCandidates(InputNames inputNames);

}
