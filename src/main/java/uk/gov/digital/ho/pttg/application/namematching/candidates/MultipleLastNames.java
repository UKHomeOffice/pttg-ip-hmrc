package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.MultipleLastNamesFunctions.generateNameCombinations;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.MultipleLastNamesFunctions.generateNobiliaryLastNameCombinations;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.MULTIPLE_NAMES_STRATEGY_PRIORITY;

@Component
@Order(value = MULTIPLE_NAMES_STRATEGY_PRIORITY)
public class MultipleLastNames implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {

        if (!inputNames.multiPartLastName()) {
            return emptyList();
        }

        List<CandidateName> lastNameCombinations = generateNobiliaryLastNameCombinations(inputNames, singletonList(MULTIPLE_NAMES_STRATEGY_PRIORITY), inputNames.lastNames());

        return generateNameCombinations(inputNames, lastNameCombinations);
    }
}
