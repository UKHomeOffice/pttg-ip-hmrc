package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.MultipleLastNamesFunctions.generateNameCombinations;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.MultipleLastNamesFunctions.generateNobiliaryLastNameCombinations;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator.MULTIPLE_NAMES;

@Component
public class MultipleLastNames implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {

        if (!inputNames.multiPartLastName()) {
            return emptyList();
        }

        List<CandidateName> lastNameCombinations = generateNobiliaryLastNameCombinations(inputNames, singletonList(MULTIPLE_NAMES), inputNames.lastNames());

        return generateNameCombinations(inputNames, lastNameCombinations);
    }
}
