package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

public interface NameMatchingCandidateGenerator {

    enum Generator {
        ENTIRE_NON_ALIAS_NAME,
        ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME,
        MULTIPLE_NAMES,
        ABBREVIATED_NAMES,
        ALIAS_COMBINATIONS,
        NAME_COMBINATIONS,
        SPLITTERS_REMOVED,
        SPLITTERS_REPLACED
    }

    List<CandidateName> generateCandidates(InputNames originalNames, InputNames namesToProcess);

}
