package uk.gov.digital.ho.pttg.application.namematching;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class NameMatchingCandidatesGenerator {
    private static final String NAME_SPLITTERS = "-'";
    private static final String NAME_SPLITTER_REGEX = "[" + NAME_SPLITTERS + "]";
    private static final Integer MAX_NAMES = 7;

    public static List<PersonName> generateCandidateNames(String firstName, String lastName) {
        validateNames(firstName, lastName);

        List<PersonName> candidates;

        if (namesContainSplitters(firstName, lastName)) {
            candidates = generateCandidatesWithSplitters(firstName, lastName);
        } else {
            candidates = generateCandidatesForMultiWordLastName(firstName, lastName);
            candidates.addAll(generateCandidates(firstName, lastName));
        }

        return Collections.unmodifiableList(candidates);
    }

    private static void validateNames(String firstName, String lastName) {
        if (isBlank(firstName) && isBlank(lastName)) {
            throw new IllegalArgumentException("At least one name is required");
        }
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

        return ImmutableList.copyOf(candidateNames);
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

    private static List<PersonName> generateCandidatesForMultiWordLastName(String firstName, String lastName) {
        List<PersonName> candidates = new ArrayList<>();

        if (lastName.trim().matches(".*\\s+.*")) {
            List<String> listOfFirstNames = new ArrayList<>(splitIntoDistinctNames(firstName));
            if (listOfFirstNames.size() > MAX_NAMES - 1) {
                listOfFirstNames = listOfFirstNames.subList(0, MAX_NAMES - 1);
            }
            candidates.addAll(
                    listOfFirstNames.stream()
                            .map(eachFirstName -> new PersonName(eachFirstName, lastName))
                            .collect(toList())
            );
        }
        return candidates;
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
