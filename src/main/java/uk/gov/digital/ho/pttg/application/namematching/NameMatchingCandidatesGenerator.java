package uk.gov.digital.ho.pttg.application.namematching;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class NameMatchingCandidatesGenerator {
    private static final String NAME_SPLITTERS = "-'";
    private static final String NAME_SPLITTER_REGEX = "[" + NAME_SPLITTERS + "]";
    private static final Integer MAX_NAMES = 7;
    private static final Integer HMRC_SURNAME_LENGTH = 3;

    public static List<PersonName> generateCandidateNames(String firstName, String lastName) {

        List<PersonName> candidates = new ArrayList<>();

        candidates.addAll(generateCandidatesForMultiWordLastName(firstName, lastName));
        candidates.addAll(generateCandidates(firstName, lastName));

        if (namesContainSplitters(firstName, lastName)) {
            candidates.addAll(generateCandidatesWithSplitters(firstName, lastName));
        }

        return Collections.unmodifiableList(candidates);
    }

    private static boolean namesContainSplitters(String firstName, String lastName) {
        return StringUtils.containsAny(firstName, NAME_SPLITTERS) || StringUtils.containsAny(lastName, NAME_SPLITTERS);
    }

    private static List<PersonName> generateCandidatesWithSplitters(String firstName, String lastName) {
        Set<PersonName> candidateNames = new LinkedHashSet<>();

        candidateNames.addAll(generateCandidatesForMultiWordLastName(nameWithSplittersRemoved(firstName), nameWithSplittersRemoved(lastName)));
        candidateNames.addAll(generateCandidatesForMultiWordLastName(nameWithSplittersReplacedBySpaces(firstName), nameWithSplittersReplacedBySpaces(lastName)));
        candidateNames.addAll(generateCandidates(nameWithSplittersRemoved(firstName), nameWithSplittersRemoved(lastName)));
        candidateNames.addAll(generateCandidates(nameWithSplittersReplacedBySpaces(firstName), nameWithSplittersReplacedBySpaces(lastName)));

        return Lists.newArrayList(candidateNames);
    }

    private static String nameWithSplittersRemoved(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, "");
    }

    private static String nameWithSplittersReplacedBySpaces(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, " ");
    }

    private static List<PersonName> generateCandidates(String firstName, String lastName) {
        List<String> fullListOfNames = splitIntoDistinctNames(firstName, lastName);
        List<String> namesToUse = removeAdditionalNamesIfOverMax(fullListOfNames);

        int numberOfNames = namesToUse.size();
        return NamePairRules.forNameCount(numberOfNames)
                .stream()
                .map(namePairRule -> namePairRule.calculateName(namesToUse))
                .collect(toList());
    }

    public static List<PersonName> generateCandidatesForMultiWordLastName(String firstName, String lastName) {
        List<PersonName> candidates = new ArrayList<>();

        if (!multiPart(lastName)) {
            return candidates;
        }

        // Create list of allowed names
        List<String> fullListOfNames = splitIntoDistinctNames(firstName, lastName);
        List<String> allowedNames = removeAdditionalNamesIfOverMax(fullListOfNames);

        // Separate names and surnames into single entities
        List<String> firstNames = new ArrayList<>(splitIntoDistinctNames(firstName));
        List<String> lastNames = new ArrayList<>(splitIntoDistinctNames(lastName));

        // Remove invalid names from name and surname list
        List<String> listOfFirstNames = removeInvalidItems(firstNames, allowedNames);
        List<String> listOfLastNames = removeInvalidItems(lastNames, allowedNames);

        // Create a combination of surnames but only with those parts of the surname shorter than 3 characters
        List<String> surnameCombinationList =
                listOfLastNames.stream()
                        .filter(surname -> surname.length() < HMRC_SURNAME_LENGTH)
                        .distinct()
                        .flatMap(surname1 -> listOfLastNames.stream()
                                .filter(surname2 -> !surname1.equals(surname2))
                                .map(surname2 -> surname1 + " " + surname2))
                        .collect(Collectors.toList());

        // By default add to the list the whole allowed surname if more than 3 parts as it won't have been covered by
        // the previous combinations
        if (listOfLastNames.size() > 2) {
            surnameCombinationList.add(0, StringUtils.join(listOfLastNames, " "));
        }

        final List<String> surnameList = new ArrayList<>(surnameCombinationList);

        candidates.addAll(
                listOfFirstNames.stream()
                        .flatMap(eachFirstName -> surnameList.stream()
                                .map(lastNameCombination -> new PersonName(eachFirstName, lastNameCombination)))
                        .collect(toList())
        );


        // Add full name to the top of the list if it has not been generated by the previous combinations
        PersonName fullname = new PersonName(StringUtils.join(listOfFirstNames, " "),
                StringUtils.join(listOfLastNames, " "));
        if (!candidates.contains(fullname)) {
            candidates.add(0, fullname);
        }


        return candidates;
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

    private static List<String> removeAdditionalNamesIfOverMax(List<String> incomingNames) {
        int numberOfNames = incomingNames.size();

        if (numberOfNames <= MAX_NAMES) {
            return incomingNames;
        }

        List<String> firstFourNames = incomingNames.subList(0, 4);
        List<String> lastThreeNames = incomingNames.subList(numberOfNames - 3, numberOfNames);

        return newArrayList(concat(firstFourNames, lastThreeNames));
    }

    private static List<String> splitIntoDistinctNames(String firstName, String lastName) {
        List<String> names = new ArrayList<>();

        names.addAll(splitIntoDistinctNames(firstName));
        names.addAll(splitIntoDistinctNames(lastName));

        return Collections.unmodifiableList(names);
    }

    private static List<String> splitIntoDistinctNames(String name) {
        if (isBlank(name)) {
            return Collections.emptyList();
        }

        String[] splitNames = name.trim().split("\\s+");

        return Arrays.asList(splitNames);
    }
}
