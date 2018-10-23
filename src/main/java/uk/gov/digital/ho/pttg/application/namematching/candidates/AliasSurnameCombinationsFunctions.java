package uk.gov.digital.ho.pttg.application.namematching.candidates;

import java.util.List;
import java.util.stream.Collectors;

class AliasSurnameCombinationsFunctions {

    static List<String> removeName(String nameToRemove, List<String> names) {
        return names.stream()
                .filter(name -> !name.equals(nameToRemove))
                .collect(Collectors.toList());
    }
}
