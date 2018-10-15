package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingFunctions.removeAdditionalNamesIfOverMax;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingFunctions.splitTwoIntoDistinctNames;

@Component
public class NameCombinations implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(String firstName, String lastName) {
        List<String> fullListOfNames = splitTwoIntoDistinctNames(firstName, lastName);
        List<String> namesToUse = removeAdditionalNamesIfOverMax(fullListOfNames);

        int numberOfNames = namesToUse.size();
        return NamePairRules.forNameCount(numberOfNames)
                .stream()
                .map(namePairRule -> namePairRule.calculateName(namesToUse))
                .collect(toList());
    }
}
