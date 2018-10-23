package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.CandidateName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.reverse;

class AliasSurnameCombinationsFunctions {

    static List<String> removeName(String nameToRemove, List<String> names) {
        return names.stream()
                .filter(name -> !name.equals(nameToRemove))
                .collect(Collectors.toList());
    }

    static List<CandidateName> nonAliasFirstAliasLastCombinations(List<String> nonAliasNames, List<String> aliasSurnames) {
        List<CandidateName> candidateNames = new ArrayList<>();

        List<String> reversedAliasSurnames = new ArrayList<>(aliasSurnames);
        reverse(reversedAliasSurnames);

        for(String aliasSurname: reversedAliasSurnames){
            for (String nonAliasName : nonAliasNames) {
                candidateNames.add(new CandidateName(nonAliasName, aliasSurname));
            }
        }

        return candidateNames;
    }
}
