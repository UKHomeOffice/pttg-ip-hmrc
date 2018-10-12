package uk.gov.digital.ho.pttg.application.namematching;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.pttg.application.namematching.candidates.NameCombinations;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingFunctions.*;

@Service
public class NameMatchingCandidatesService {
    private static final String NAME_SPLITTERS = "-'";
    private static final String NAME_SPLITTER_REGEX = "[" + NAME_SPLITTERS + "]";
    private static final Integer MAX_NAMES = 7;
    private static final Integer HMRC_SURNAME_LENGTH = 3;

    private NameCombinations nameCombinations;

    public NameMatchingCandidatesService(NameCombinations nameCombinations) {
        this.nameCombinations = nameCombinations;
    }

    public List<PersonName> generateCandidateNames(String firstName, String lastName) {

        List<PersonName> candidates = new ArrayList<>();

        candidates.addAll(generateCandidatesForMultiWordLastName(firstName, lastName));
        candidates.addAll(nameCombinations.generateCandidates(firstName, lastName));

        if (namesContainSplitters(firstName, lastName)) {
            candidates.addAll(generateCandidatesWithSplitters(firstName, lastName));
        }

        return Collections.unmodifiableList(candidates);
    }

    private static boolean namesContainSplitters(String firstName, String lastName) {
        return StringUtils.containsAny(firstName, NAME_SPLITTERS) || StringUtils.containsAny(lastName, NAME_SPLITTERS);
    }

    private List<PersonName> generateCandidatesWithSplitters(String firstName, String lastName) {
        Set<PersonName> candidateNames = new LinkedHashSet<>();

        candidateNames.addAll(generateCandidatesForMultiWordLastName(nameWithSplittersRemoved(firstName), nameWithSplittersRemoved(lastName)));
        candidateNames.addAll(generateCandidatesForMultiWordLastName(nameWithSplittersReplacedBySpaces(firstName), nameWithSplittersReplacedBySpaces(lastName)));
        candidateNames.addAll(nameCombinations.generateCandidates(nameWithSplittersRemoved(firstName), nameWithSplittersRemoved(lastName)));
        candidateNames.addAll(nameCombinations.generateCandidates(nameWithSplittersReplacedBySpaces(firstName), nameWithSplittersReplacedBySpaces(lastName)));

        return Lists.newArrayList(candidateNames);
    }

    private static String nameWithSplittersRemoved(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, "");
    }

    private static String nameWithSplittersReplacedBySpaces(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, " ");
    }

    static List<PersonName> generateCandidatesForMultiWordLastName(String firstName, String lastName) {
        List<PersonName> candidates = new ArrayList<>();

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
                                .map(lastNameCombination -> new PersonName(eachFirstName, lastNameCombination)))
                        .collect(toList())
        );

        addFullNameIfNotAlreadyPresent(candidates, listOfFirstNames, listOfLastNames);

        return candidates;
    }

    private static void addMultiPartSurnameToCombination(List<String> surnameCombinationList, List<String> listOfLastNames) {
        if (listOfLastNames.size() > 2) {
            surnameCombinationList.add(0, StringUtils.join(listOfLastNames, " "));
        }
    }

    private static void addFullNameIfNotAlreadyPresent(List<PersonName> candidates, List<String> listOfFirstNames, List<String> listOfLastNames) {
        PersonName fullname = new PersonName(
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

    private static boolean multiPart(String lastName) {
        return lastName.trim().matches(".*\\s+.*");
    }

}
