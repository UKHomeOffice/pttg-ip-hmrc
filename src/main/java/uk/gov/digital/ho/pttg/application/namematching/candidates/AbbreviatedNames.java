package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Collections.emptyList;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AbbreviatedNamesFunctions.doesNotContainAbbreviatedNames;

@Component
public class AbbreviatedNames implements NameMatchingCandidateGenerator {

    private final NameCombinations nameCombinations;
    private final AliasCombinations aliasCombinations;

    public AbbreviatedNames(NameCombinations nameCombinations, AliasCombinations aliasCombinations) {
        this.nameCombinations = nameCombinations;
        this.aliasCombinations = aliasCombinations;
    }

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {
        if (doesNotContainAbbreviatedNames(inputNames)) {
            return emptyList();
        }

        InputNames abbreviatedNames = inputNames.groupByAbbreviatedNames();

        if (abbreviatedNames.hasAliasSurnames()) {
            return aliasCombinations.generateCandidates(abbreviatedNames);
        }

        return nameCombinations.generateCandidates(abbreviatedNames);
    }

}
