package uk.gov.digital.ho.pttg.application.namematching;

import org.apache.commons.lang3.StringUtils;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class NameMatchingCandidatesGenerator {
    private static final String NAME_SPLITTERS = "-'";
    private static final String NAME_SPLITTER_REGEX = "[" + NAME_SPLITTERS + "]";
    private static final Integer MAX_NAMES = NamePairRules.getMaxNameCount();

    public static List<String> generateCandidateNames(String firstName, String lastName) {
        validateNames(firstName, lastName);

        List<String> candidates;

        if (namesContainSplitters(firstName, lastName)) {
            candidates = generateCandidatesWithSplitters(firstName, lastName);
        } else {
            candidates = generateCandidates(firstName, lastName);
        }

        return Collections.unmodifiableList(candidates);
    }

    private static boolean namesContainSplitters(String firstName, String lastName) {
        return StringUtils.containsAny(firstName, NAME_SPLITTERS) || StringUtils.containsAny(lastName, NAME_SPLITTERS);
    }

    private static List<String> generateCandidatesWithSplitters(String firstName, String lastName) {
        List<String> candidateNames = new ArrayList<>();

        candidateNames.addAll(generateCandidates(nameWithSplittersRemoved(firstName), nameWithSplittersRemoved(lastName)));
        candidateNames.addAll(generateCandidates(nameWithSplittersAsSpaces(firstName), nameWithSplittersAsSpaces(lastName)));

        return candidateNames;
    }

    private static String nameWithSplittersRemoved(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, "");
    }

    private static String nameWithSplittersAsSpaces(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, " ");
    }

    private static List<String> generateCandidates(String firstName, String lastName) {
        List<String> allNames = findAllNames(firstName, lastName);
        validateSplitNames(allNames);
        return NamePairRules.forNameCount(allNames.size())
                .stream()
                .map(namePairRule -> namePairRule.calculateName(allNames))
                .collect(toList());
    }

    private static void validateNames(String firstName, String lastName) {
        if (isBlank(firstName) && isBlank(lastName)) {
            throw new IllegalArgumentException("At least one name is required");
        }
    }

    private static void validateSplitNames(List<String> allNames) {
        if (allNames.size() > MAX_NAMES) {
            throw new ApplicationExceptions.TooManyNamesException(String.format("Too many names: maximum is %d", MAX_NAMES));
        }
    }

    private static List<String> findAllNames(String firstName, String lastName) {
        List<String> allNames = new ArrayList<>();

        allNames.addAll(splitIntoDistinctNames(firstName));
        allNames.addAll(splitIntoDistinctNames(lastName));

        return Collections.unmodifiableList(allNames);
    }

    private static List<String> splitIntoDistinctNames(String name) {
        if (isBlank(name)) {
            return Collections.emptyList();
        }

        String trimmedFirstName = name.trim();
        String[] splitNames = trimmedFirstName.split("\\s+");

        return Arrays.asList(splitNames);
    }
}
