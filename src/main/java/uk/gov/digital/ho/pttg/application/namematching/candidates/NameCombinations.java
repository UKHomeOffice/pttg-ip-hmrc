package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;
import uk.gov.digital.ho.pttg.application.namematching.Name;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.InputNamesFunctions.combine;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.CandidateFunctions.removeAdditionalNamesIfOverMax;

@Component
public class NameCombinations implements NameMatchingCandidateGenerator {

    static final int MAX_NAMES = 7;
    static final int MAX_LAST_NAMES = 3;

    @Override
    public List<CandidateName> generateCandidates(InputNames originalNames, InputNames namesToProcess) {

        if (namesToProcess.hasAliasSurnames()) {
            return emptyList();
        }

        InputNames largestAllowedName = removeAdditionalNamesIfOverMax(namesToProcess, MAX_NAMES, MAX_LAST_NAMES);

        List<Name> namesToUse = combine(largestAllowedName.firstNames(), largestAllowedName.lastNames());

        return NamePairRules.forNameCount(namesToUse.size())
                .stream()
                .map(namePair -> namePair.calculateName(originalNames, namesToUse))
                .collect(toList());
    }
}
