package uk.gov.digital.ho.pttg.application.namematching;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NameMatchingCandidateServiceFunctions {
    static List<CandidateName> deduplicate(List<CandidateName> candidateNames) {
        Set<CandidateName> seenHmrcEquivalentNames = new HashSet<>();
        return candidateNames.stream()
                .filter(name -> seenHmrcEquivalentNames.add(name.hmrcNameMatchingEquivalent()))
                .collect(Collectors.toList());
    }
}
