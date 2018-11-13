package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.CandidateFunctions.removeAdditionalNamesIfOverMax;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.NAME_MATCHING_STRATEGY_PRIORITY;

@Component
@Order(value = NAME_MATCHING_STRATEGY_PRIORITY)
public class NameCombinations implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {

        if (inputNames.hasAliasSurnames()) {
            return emptyList();
        }

        InputNames largestAllowedName = removeAdditionalNamesIfOverMax(inputNames);
        List<String> namesToUse = largestAllowedName.allNonAliasNames();

        int numberOfNames = namesToUse.size();
        return NamePairRules.forNameCount(numberOfNames)
                .stream()
                .map(namePairRule -> namePairRule.calculateName(namesToUse))
                .collect(toList());
    }
}
