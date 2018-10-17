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

    public NameMatchingCandidatesService(NameMatchingCandidateGenerator nameCombinations, NameMatchingCandidateGenerator multipleLastNames, NameMatchingCandidateGenerator specialCharacters) {
        this.nameCombinations = nameCombinations;
        this.multipleLastNames = multipleLastNames;
        this.specialCharacters = specialCharacters;
    }

    public List<CandidateName> generateCandidateNames(String firstNames, String lastNames) {

        List<CandidateName> candidates = new ArrayList<>();

        InputNames inputNames = new InputNames(firstNames, lastNames);

        candidates.addAll(multipleLastNames.generateCandidates(inputNames));
        candidates.addAll(nameCombinations.generateCandidates(inputNames));
        candidates.addAll(specialCharacters.generateCandidates(inputNames));

        return Collections.unmodifiableList(deduplicate(candidates));
    }

}
