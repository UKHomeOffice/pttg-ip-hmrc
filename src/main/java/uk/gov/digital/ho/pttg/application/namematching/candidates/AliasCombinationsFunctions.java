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

    static List<CandidateName> nonAliasFirstAliasLastCombinations(InputNames inputNames) {
        List<CandidateName> candidateNames = new ArrayList<>();

        List<String> reversedAliasSurnames = new ArrayList<>(inputNames.rawAliasSurnames());
        Collections.reverse(reversedAliasSurnames);

        for(String aliasSurname: reversedAliasSurnames){
            for (String nonAliasName : inputNames.allNonAliasNames()) {
                candidateNames.add(new CandidateName(nonAliasName, aliasSurname));
            }
        }

        return candidateNames;
    }


    static List<CandidateName> firstNameCombinations(InputNames inputNames) {
        List<CandidateName> candidateNames = new ArrayList<>();

        for (String firstName : inputNames.rawFirstNames()) {
            for (String otherFirstName : removeName(firstName, inputNames.rawFirstNames())) {
                candidateNames.add(new CandidateName(firstName, otherFirstName));
            }
        }
        return candidateNames;
    }


    static List<CandidateName> nonAliasSurnameAsFirstNameCombinations(InputNames inputNames) {
        List<CandidateName> candidateNames = new ArrayList<>();

        for (String lastName : inputNames.rawLastNames()) {
            for (String otherName : inputNames.rawFirstNames()) {
                candidateNames.add(new CandidateName(lastName, otherName));
            }
        }

        return candidateNames;
    }

    static List<CandidateName> aliasSurnameAsFirstNameCombinations(InputNames inputNames) {
        List<CandidateName> candidateNames = new ArrayList<>();

        for (String aliasSurname : inputNames.rawAliasSurnames()) {
            for (String nonAliasName : removeName(aliasSurname, inputNames.allNames())) {
                candidateNames.add(new CandidateName(aliasSurname, nonAliasName));
            }
        }
        return candidateNames;
    }

    static List<CandidateName> nonAliasFirstNamesAndLastNameCombinations(InputNames inputNames) {
        List<CandidateName> candidateNames = new ArrayList<>();

        List<String> reversedLastNames = new ArrayList<>(inputNames.rawLastNames());
        Collections.reverse(reversedLastNames);

        for (String lastName : reversedLastNames) {
            for (String otherName : removeName(lastName, inputNames.allNonAliasNames())) {
                candidateNames.add(new CandidateName(otherName, lastName));
            }
        }
        return candidateNames;
    }
}
