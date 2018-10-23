package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.reverse;

final class AliasSurnameCombinationsFunctions {

    private AliasSurnameCombinationsFunctions() {
        throw new UnsupportedOperationException("Helper class for AliasSurnameCombinationsFunctions containing only static methods - no need to instantiate.");
    }

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


    static List<CandidateName> firstNameCombinations(List<String> firstNames) {
        List<CandidateName> candidateNames = new ArrayList<>();

        for (String firstName : firstNames) {
            for (String otherFirstName : removeName(firstName, firstNames)) {
                candidateNames.add(new CandidateName(firstName, otherFirstName));
            }
        }
        return candidateNames;
    }


    static List<CandidateName> nonAliasSurnameAsFirstNameCombinations(InputNames inputNames) {
        List<CandidateName> candidateNames = new ArrayList<>();

        for (String lastName : inputNames.lastNames()) {
            for (String otherName : removeName(lastName, inputNames.allNonAliasNames())) {
                candidateNames.add(new CandidateName(lastName, otherName));
            }
        }

        return candidateNames;
    }

    static List<CandidateName> aliasSurnameAsFirstNameCombinations(InputNames inputNames) {
        List<CandidateName> candidateNames = new ArrayList<>();

        for (String aliasSurname : inputNames.aliasSurnames()) {
            for (String nonAliasName : removeName(aliasSurname, inputNames.allNames())) {
                candidateNames.add(new CandidateName(aliasSurname, nonAliasName));
            }
        }
        return candidateNames;
    }
}
