package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingFunctions.removeAdditionalNamesIfOverMax;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingFunctions.splitIntoDistinctNames;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingFunctions.splitTwoIntoDistinctNames;

@Component
public class MultipleLastNames implements NameMatchingCandidateGenerator {

    private static final Integer HMRC_SURNAME_LENGTH = 3;

    @Override
    public List<CandidateName> generateCandidates(String firstName, String lastName) {
        List<CandidateName> candidates = new ArrayList<>();

        if (!multiPart(lastName)) {
            return candidates;
        }

        List<String> fullListOfNames = splitTwoIntoDistinctNames(firstName, lastName);
        List<String> allowedNames = removeAdditionalNamesIfOverMax(fullListOfNames);

        List<String> nonSanitisedFirstNames = new ArrayList<>(splitIntoDistinctNames(firstName));
        List<String> nonSanitisedLastNames = new ArrayList<>(splitIntoDistinctNames(lastName));

        List<String> listOfFirstNames = removeInvalidItems(nonSanitisedFirstNames, allowedNames);
        List<String> listOfLastNames = removeInvalidItems(nonSanitisedLastNames, allowedNames);

        List<String> surnameCombinationList = generateSurnameCombinations(listOfLastNames);

        // By default add to the list the whole allowed surname if more than 3 parts are present
        // as it won't have been covered by the previous combinations
        addMultiPartSurnameToCombination(surnameCombinationList, listOfLastNames);

        final List<String> surnameList = new ArrayList<>(surnameCombinationList);

        candidates.addAll(
                listOfFirstNames.stream()
                        .flatMap(eachFirstName -> surnameList.stream()
                                .map(lastNameCombination -> new CandidateName(eachFirstName, lastNameCombination)))
                        .collect(toList())
        );

        addFullNameIfNotAlreadyPresent(candidates, listOfFirstNames, listOfLastNames);

        return candidates;

    }

    private static boolean multiPart(String lastName) {
        return lastName.trim().matches(".*\\s+.*");
    }

    private static void addMultiPartSurnameToCombination(List<String> surnameCombinationList, List<String> listOfLastNames) {
        if (listOfLastNames.size() > 2) {
            surnameCombinationList.add(0, StringUtils.join(listOfLastNames, " "));
        }
    }

    private static void addFullNameIfNotAlreadyPresent(List<CandidateName> candidates, List<String> listOfFirstNames, List<String> listOfLastNames) {
        CandidateName fullname = new CandidateName(
                StringUtils.join(listOfFirstNames, " "),
                StringUtils.join(listOfLastNames, " ")
        );
        if (!candidates.contains(fullname)) {
            candidates.add(0, fullname);
        }
    }

    private static List<String> generateSurnameCombinations(List<String> listOfLastNames) {
        return listOfLastNames.stream()
                .filter(surname -> surname.length() < HMRC_SURNAME_LENGTH)
                .distinct()
                .flatMap(surname1 -> listOfLastNames.stream()
                        .filter(surname2 -> !surname1.equals(surname2))
                        .map(surname2 -> surname1 + " " + surname2))
                .collect(Collectors.toList());
    }

    private static List<String> removeInvalidItems(List<String> firstName, List<String> allowedItems) {
        Set<String> availableItems = new HashSet<>(allowedItems);

        return firstName.stream()
                .filter(availableItems::contains)
                .collect(toList());
    }

}
