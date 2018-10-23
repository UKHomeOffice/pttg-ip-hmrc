package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.digital.ho.pttg.application.namematching.candidates.AliasCombinationsFunctions.*;

public class AliasCombinations implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {
        List<CandidateName> candidateNames = new ArrayList<>();

        if (inputNames.aliasSurnames().isEmpty()) {
            return candidateNames;
        }

        candidateNames.addAll(nonAliasFirstNamesAndLastNameCombinations(inputNames));

        candidateNames.addAll(nonAliasFirstAliasLastCombinations(inputNames));

        candidateNames.addAll(firstNameCombinations(inputNames));

        candidateNames.addAll(nonAliasSurnameAsFirstNameCombinations(inputNames));

        candidateNames.addAll(aliasSurnameAsFirstNameCombinations(inputNames));

        return candidateNames;
    }

}