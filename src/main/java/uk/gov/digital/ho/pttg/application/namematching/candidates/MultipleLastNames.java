package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.Collections;
import java.util.List;

import static uk.gov.digital.ho.pttg.application.namematching.candidates.MultipleLastNamesFunctions.generateAllLastNameCombinations;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.MultipleLastNamesFunctions.generateLastNameCombinations;

@Component
public class MultipleLastNames implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {

        if (!inputNames.multiPartLastName()) {
            return Collections.emptyList();
        }

        List<String> lastNameCombinations = generateLastNameCombinations(inputNames.lastNames());

        return generateAllLastNameCombinations(inputNames.firstNames(), lastNameCombinations);
    }
}
