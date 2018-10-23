package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.digital.ho.pttg.application.namematching.candidates.AliasSurnameCombinationsFunctions.*;

public class AliasCombinations implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {
        List<CandidateName> candidateNames = new ArrayList<>();

        if (inputNames.aliasSurnames().isEmpty()) {
            return candidateNames;
        }

        candidateNames.addAll(nonAliasFirstNamesAndLastNameCombinations(inputNames.allNonAliasNames(), inputNames.lastNames()));

        candidateNames.addAll(nonAliasFirstAliasLastCombinations(inputNames.allNonAliasNames(), inputNames.aliasSurnames()));

        candidateNames.addAll(firstNameCombinations(inputNames.firstNames()));

        candidateNames.addAll(nonAliasSurnameAsFirstNameCombinations(inputNames));

        candidateNames.addAll(aliasSurnameAsFirstNameCombinations(inputNames));

        return candidateNames;
    }

}
