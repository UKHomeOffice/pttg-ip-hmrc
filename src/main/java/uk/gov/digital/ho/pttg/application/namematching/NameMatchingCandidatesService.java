package uk.gov.digital.ho.pttg.application.namematching;

import org.springframework.stereotype.Service;
import uk.gov.digital.ho.pttg.application.namematching.candidates.MultipleLastNames;
import uk.gov.digital.ho.pttg.application.namematching.candidates.NameCombinations;
import uk.gov.digital.ho.pttg.application.namematching.candidates.SpecialCharacters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingFunctions.deduplicate;

@Service
public class NameMatchingCandidatesService {

    private NameCombinations nameCombinations;
    private MultipleLastNames multipleLastNames;
    private SpecialCharacters specialCharacters;

    public NameMatchingCandidatesService(NameCombinations nameCombinations, MultipleLastNames multipleLastNames, SpecialCharacters specialCharacters) {
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
