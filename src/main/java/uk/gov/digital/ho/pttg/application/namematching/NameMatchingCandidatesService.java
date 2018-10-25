package uk.gov.digital.ho.pttg.application.namematching;

import org.springframework.stereotype.Service;
import uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingCandidateServiceFunctions.deduplicate;

@Service
public class NameMatchingCandidatesService {

    private NameMatchingCandidateGenerator nameCombinations;
    private NameMatchingCandidateGenerator multipleLastNames;
    private NameMatchingCandidateGenerator specialCharacters;
    private NameMatchingCandidateGenerator aliasCombinations;

    public NameMatchingCandidatesService(NameMatchingCandidateGenerator nameCombinations,
                                         NameMatchingCandidateGenerator multipleLastNames,
                                         NameMatchingCandidateGenerator specialCharacters,
                                         NameMatchingCandidateGenerator aliasCombinations) {
        this.nameCombinations = nameCombinations;
        this.multipleLastNames = multipleLastNames;
        this.specialCharacters = specialCharacters;
        this.aliasCombinations = aliasCombinations;
    }

    public List<CandidateName> generateCandidateNames(String firstNames, String lastNames, String aliasSurnames) {
        InputNames inputNames = new InputNames(firstNames, lastNames, aliasSurnames);

        List<CandidateName> candidates = new ArrayList<>(multipleLastNames.generateCandidates(inputNames));

        if (inputNames.hasAliasSurnames()) {
            candidates.addAll(aliasCombinations.generateCandidates(inputNames));
        } else {
            candidates.addAll(nameCombinations.generateCandidates(inputNames));
        }

        candidates.addAll(specialCharacters.generateCandidates(inputNames));

        return Collections.unmodifiableList(deduplicate(candidates));
    }

}
