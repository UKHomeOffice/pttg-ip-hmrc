package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.digital.ho.pttg.application.namematching.candidates.AliasSurnameCombinationsFunctions.nonAliasFirstAliasLastCombinations;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AliasSurnameCombinationsFunctions.removeName;

public class AliasSurnameCombinations implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {
        List<CandidateName> candidateNames = new ArrayList<>();

        if (inputNames.aliasSurnames().isEmpty()) {
            return candidateNames;
        }

        for (String firstName : inputNames.firstNames()) {
            candidateNames.add(new CandidateName(firstName, inputNames.fullLastName()));
        }

        candidateNames.addAll(nonAliasFirstAliasLastCombinations(inputNames.allNonAliasNames(), inputNames.aliasSurnames()));

        for (String firstName : inputNames.firstNames()) {
            for (String otherFirstName : removeName(firstName, inputNames.firstNames())) {
                candidateNames.add(new CandidateName(firstName, otherFirstName));
            }
        }

        for (String lastName : inputNames.lastNames()) {
            for (String otherName : removeName(lastName, inputNames.allNonAliasNames())) {
                candidateNames.add(new CandidateName(lastName, otherName));
            }
        }

        for (String aliasSurname : inputNames.aliasSurnames()) {
            for (String nonAliasName : removeName(aliasSurname, inputNames.allNames())) {
                candidateNames.add(new CandidateName(aliasSurname, nonAliasName));
            }
        }

        return candidateNames;
    }

}
