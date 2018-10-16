package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.digital.ho.pttg.application.namematching.CandidateFunctions.removeAdditionalNamesIfOverMax;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.MultipleLastNamesFunctions.*;

@Component
public class MultipleLastNames implements NameMatchingCandidateGenerator {


    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {
        List<CandidateName> candidates = new ArrayList<>();

        if (!inputNames.multiPartLastName()) {
            return candidates;
        }

        InputNames largestAllowedName = removeAdditionalNamesIfOverMax(inputNames);

        List<String> lastNameCombinations = generateLastNameCombinations(largestAllowedName.lastNames());

        // By default add to the list the whole allowed surname if more than 3 parts are present
        // as it won't have been covered by the previous combinations
        lastNameCombinations = addMultiPartLastNameToCombination(lastNameCombinations, largestAllowedName.lastNames());

        candidates = addAllLastNameCombinations(candidates, largestAllowedName.firstNames(), lastNameCombinations);

        candidates = addFullNameIfNotAlreadyPresent(candidates, largestAllowedName);

        return candidates;

    }

}
