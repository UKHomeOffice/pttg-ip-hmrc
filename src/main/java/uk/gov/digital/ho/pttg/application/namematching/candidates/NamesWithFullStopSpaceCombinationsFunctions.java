package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

final class NamesWithFullStopSpaceCombinationsFunctions {

    private static final String ANY_LETTER_INCLUDING_UNICODE_MATCHER = "\\p{L}\\p{M}*+";
    private static final String FULL_STOP_SPACE_MATCHER = "\\.\\s+";
    private static final String FULL_STOP_SPACE_BETWEEN_NAMES_PATTERN = ANY_LETTER_INCLUDING_UNICODE_MATCHER + FULL_STOP_SPACE_MATCHER + ANY_LETTER_INCLUDING_UNICODE_MATCHER;

    private static final String FULL_STOP_SPACE_NEGATIVE_LOOK_BEHIND = "(?<!(\\.|\\s))";
    private static final String SPACE_NOT_PRECEDED_BY_FULL_STOP_OR_SPACE_PATTERN = FULL_STOP_SPACE_NEGATIVE_LOOK_BEHIND + "\\s+";

    static boolean doesNotContainFullStopSpaceBetweenNames(InputNames inputNames) {
        boolean fullNameContainsFullStopSpace = nameDoesNotContainFullStopSpaceBetweenNames(inputNames.fullFirstName()) && nameDoesNotContainFullStopSpaceBetweenNames(inputNames.fullLastName());
        return fullNameContainsFullStopSpace && nameDoesNotContainFullStopSpaceBetweenNames(inputNames.allAliasSurnamesAsString());
    }

    private static boolean nameDoesNotContainFullStopSpaceBetweenNames(String s) {
        return !Pattern.compile(FULL_STOP_SPACE_BETWEEN_NAMES_PATTERN).matcher(s).find();
    }

    static List<String> splitNamesIgnoringFullStopSpace(String names) {
        if (names.isEmpty()) {
            return emptyList();
        }
        String[] splitNames = names.split(SPACE_NOT_PRECEDED_BY_FULL_STOP_OR_SPACE_PATTERN);

        return Arrays.stream(splitNames)
                .map(NamesWithFullStopSpaceCombinationsFunctions::removeMultipleSpaces)
                .collect(Collectors.toList());
    }

    private static String removeMultipleSpaces(String name) {
        return name.replaceAll("\\s+", " ");
    }
}
