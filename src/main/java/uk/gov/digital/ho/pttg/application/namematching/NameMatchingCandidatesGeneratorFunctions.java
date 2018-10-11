package uk.gov.digital.ho.pttg.application.namematching;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class NameMatchingCandidatesGeneratorFunctions {

    static List<PersonName> deduplicate(List<PersonName> candidateNames) {
        Set<PersonName> seenHmrcEquivalentNames = new HashSet<>();
        return candidateNames.stream()
                .filter(name -> seenHmrcEquivalentNames.add(name.hmrcNameMatchingEquivalent()))
                .collect(Collectors.toList());
    }

}
