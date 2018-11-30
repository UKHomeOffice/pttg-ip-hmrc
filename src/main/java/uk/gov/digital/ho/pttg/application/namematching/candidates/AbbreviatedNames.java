package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Collections.emptyList;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AbbreviatedNamesFunctions.doesNotContainAbbreviatedNames;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator.ABBREVIATED_NAMES;

@Component
public class AbbreviatedNames implements NameMatchingCandidateGenerator {

    private final NameCombinations nameCombinations;
    private final AliasCombinations aliasCombinations;

    public AbbreviatedNames(NameCombinations nameCombinations, AliasCombinations aliasCombinations) {
        this.nameCombinations = nameCombinations;
        this.aliasCombinations = aliasCombinations;
    }

    @Override
    public List<CandidateName> generateCandidates(InputNames originalNames, InputNames requiredByInterface) {
        if (doesNotContainAbbreviatedNames(originalNames)) {
            return emptyList();
        }

        InputNames abbreviatedNames = originalNames.groupByAbbreviatedNames();

        List<CandidateName> candidateNames;

        if (abbreviatedNames.hasAliasSurnames()) {
            candidateNames = aliasCombinations.generateCandidates(originalNames, abbreviatedNames);
        } else {
            candidateNames = nameCombinations.generateCandidates(originalNames, abbreviatedNames);
        }

        candidateNames.forEach(candidateName -> candidateName.derivation().addGenerator(ABBREVIATED_NAMES));

        return candidateNames;
    }

}
