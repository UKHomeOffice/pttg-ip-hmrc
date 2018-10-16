package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingFunctions.removeAdditionalNamesIfOverMax;

@Component
public class MultipleLastNames implements NameMatchingCandidateGenerator {


    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {
        List<CandidateName> candidates = new ArrayList<>();

        if (!inputNames.multiPartLastName()) {
            return candidates;
        }

        InputNames largestAllowedName = removeAdditionalNamesIfOverMax(inputNames);

        List<String> lastNameCombinations = generateLastNameCombinations(largestAllowedName.lastNames());

        // By default add to the list the whole allowed surname if more than 3 parts are present
        // as it won't have been covered by the previous combinations
        addMultiPartLastNameToCombination(lastNameCombinations, largestAllowedName.lastNames());

        candidates.addAll(
                largestAllowedName.firstNames().stream()
                        .flatMap(firstName -> lastNameCombinations.stream()
                                .map(lastNameCombination -> new CandidateName(firstName, lastNameCombination)))
                        .collect(toList())
        );

        addFullNameIfNotAlreadyPresent(candidates, largestAllowedName);

        return candidates;

    }

    private static void addMultiPartLastNameToCombination(List<String> lastNameCombinations, List<String> listOfLastNames) {
        final int MIN_NAMES_FOR_MULTIPART = 3;

        if (listOfLastNames.size() >= MIN_NAMES_FOR_MULTIPART) {
            lastNameCombinations.add(0, StringUtils.join(listOfLastNames, " "));
        }
    }

    private static void addFullNameIfNotAlreadyPresent(List<CandidateName> candidates, InputNames inputNames) {
        CandidateName fullName = new CandidateName(inputNames.fullFirstName(), inputNames.fullLastName());

        if (!candidates.contains(fullName)) {
            candidates.add(0, fullName);
        }
    }

    private static List<String> generateLastNameCombinations(List<String> lastNames) {
        final Integer HMRC_SURNAME_LENGTH = 3;

        return lastNames.stream()
                .filter(lastName -> lastName.length() < HMRC_SURNAME_LENGTH)
                .distinct()
                .flatMap(lastName1 -> lastNames.stream()
                        .filter(lastName2 -> !lastName1.equals(lastName2))
                        .map(lastName2 -> lastName1 + " " + lastName2))
                .collect(Collectors.toList());
    }
}
