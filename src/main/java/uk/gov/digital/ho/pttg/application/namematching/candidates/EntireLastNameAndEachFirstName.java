package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Collections.singletonList;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.MultipleLastNamesFunctions.generateAllLastNameCombinations;

@Component
public class EntireLastNameAndEachFirstName implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {
        return generateAllLastNameCombinations(inputNames.firstNames(), singletonList(inputNames.fullLastName()));
    }

}
