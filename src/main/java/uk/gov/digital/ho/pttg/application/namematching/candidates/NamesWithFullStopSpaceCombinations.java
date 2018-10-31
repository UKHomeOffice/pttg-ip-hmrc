package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Collections.emptyList;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NamesWithFullStopSpaceCombinationsFunctions.doesNotContainFullStopSpaceBetweenNames;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NamesWithFullStopSpaceCombinationsFunctions.splitNamesIgnoringFullStopSpace;

@Component
public class NamesWithFullStopSpaceCombinations implements NameMatchingCandidateGenerator {

    private final NameCombinations nameCombinations;

    public NamesWithFullStopSpaceCombinations(NameCombinations nameCombinations) {
        this.nameCombinations = nameCombinations;
    }

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {
        if (doesNotContainFullStopSpaceBetweenNames(inputNames)) {
            return emptyList();
        }

        List<String> firstNames = splitNamesIgnoringFullStopSpace(inputNames.fullFirstName());
        List<String> lastNames = splitNamesIgnoringFullStopSpace(inputNames.fullLastName());

        return nameCombinations.generateCandidates(new InputNames(firstNames, lastNames));
    }

}
