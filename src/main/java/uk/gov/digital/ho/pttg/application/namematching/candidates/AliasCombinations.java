package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.digital.ho.pttg.application.namematching.candidates.AliasCombinationsFunctions.*;

@Component
public class AliasCombinations implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(InputNames originalNames, InputNames namesToProcess) {
        List<CandidateName> candidateNames = new ArrayList<>();

        if (!namesToProcess.hasAliasSurnames()) {
            return candidateNames;
        }

        candidateNames.addAll(nonAliasFirstNamesAndLastNameCombinations(namesToProcess));

        candidateNames.addAll(nonAliasFirstAliasLastCombinations(namesToProcess));

        candidateNames.addAll(firstNameCombinations(namesToProcess));

        candidateNames.addAll(nonAliasSurnameAsFirstNameCombinations(namesToProcess));

        candidateNames.addAll(aliasSurnameAsFirstNameCombinations(namesToProcess));

        return candidateNames;
    }

}
