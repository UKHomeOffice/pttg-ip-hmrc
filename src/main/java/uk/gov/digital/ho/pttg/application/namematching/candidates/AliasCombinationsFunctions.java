package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class AliasCombinationsFunctions {

    private AliasCombinationsFunctions() {
        throw new UnsupportedOperationException("Helper class for AliasSurnameCombinationsFunctions containing only static methods - no need to instantiate.");
    }

    static List<String> removeName(String nameToRemove, List<String> names) {
        List<String> filteredNames = new ArrayList<>(names);
        filteredNames.remove(nameToRemove);
        return filteredNames;
    }

    static List<CandidateName> nonAliasFirstAliasLastCombinations(List<String> nonAliasNames, List<String> aliasSurnames) { // TODO OJR 2018/10/23 Change input to be InputNames
        List<CandidateName> candidateNames = new ArrayList<>();

        List<String> reversedAliasSurnames = new ArrayList<>(aliasSurnames);
        Collections.reverse(reversedAliasSurnames);

        for(String aliasSurname: reversedAliasSurnames){
            for (String nonAliasName : nonAliasNames) {
                candidateNames.add(new CandidateName(nonAliasName, aliasSurname));
            }
        }

        return candidateNames;
    }


    static List<CandidateName> firstNameCombinations(List<String> firstNames) {// TODO OJR 2018/10/23 Change input to be InputNames
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
            for (String otherName : inputNames.firstNames()) {
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

    static List<CandidateName> nonAliasFirstNamesAndLastNameCombinations(List<String> allNonAliasNames, List<String> lastNames) {// TODO OJR 2018/10/23 Change input to be InputNames
        List<CandidateName> candidateNames = new ArrayList<>();

        List<String> reversedLastNames = new ArrayList<>(lastNames);
        Collections.reverse(reversedLastNames);

        for (String lastName : reversedLastNames) {
            for (String otherName : removeName(lastName, allNonAliasNames)) {
                candidateNames.add(new CandidateName(otherName, lastName));
            }
        }
        return candidateNames;
    }
}
