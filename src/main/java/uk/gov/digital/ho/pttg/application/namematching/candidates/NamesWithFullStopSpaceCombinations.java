package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Collections.emptyList;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.NAMES_WITH_FULL_STOP_SPACE_COMBINATIONS_GENERATOR_PRIORITY;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NamesWithFullStopSpaceCombinationsFunctions.doesNotContainFullStopSpaceBetweenNames;

@Component
@Order(value = NAMES_WITH_FULL_STOP_SPACE_COMBINATIONS_GENERATOR_PRIORITY)
public class NamesWithFullStopSpaceCombinations implements NameMatchingCandidateGenerator {

    private final NameCombinations nameCombinations;
    private final AliasCombinations aliasCombinations;

    public NamesWithFullStopSpaceCombinations(NameCombinations nameCombinations, AliasCombinations aliasCombinations) {
        this.nameCombinations = nameCombinations;
        this.aliasCombinations = aliasCombinations;
    }

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {
        if (doesNotContainFullStopSpaceBetweenNames(inputNames)) {
            return emptyList();
        }

        InputNames abbreviatedNames = inputNames.groupByAbbreviatedNames();

        if (abbreviatedNames.hasAliasSurnames()) {
            return aliasCombinations.generateCandidates(abbreviatedNames);
        }

        return nameCombinations.generateCandidates(abbreviatedNames);
    }

}
