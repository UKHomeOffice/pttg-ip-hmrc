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

    public List<CandidateName> generateCandidateNames(String firstName, String lastName) {

        List<CandidateName> candidates = new ArrayList<>();

        candidates.addAll(multipleLastNames.generateCandidates(firstName, lastName));
        candidates.addAll(nameCombinations.generateCandidates(firstName, lastName));
        candidates.addAll(specialCharacters.generateCandidates(firstName, lastName));

        return Collections.unmodifiableList(deduplicate(candidates));
    }

}
