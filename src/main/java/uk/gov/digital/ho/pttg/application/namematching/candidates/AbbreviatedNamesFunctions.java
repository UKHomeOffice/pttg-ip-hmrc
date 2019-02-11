package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public final class AbbreviatedNamesFunctions {

    private static final String ANY_LETTER_INCLUDING_UNICODE_MATCHER = "\\p{L}\\p{M}*+";
    private static final String ABBREVIATION_SPACE_MATCHER = "[.']\\s+";

    private static final String ABBREVIATION_SPACE_BETWEEN_NAMES_PATTERN = ANY_LETTER_INCLUDING_UNICODE_MATCHER + ABBREVIATION_SPACE_MATCHER + ANY_LETTER_INCLUDING_UNICODE_MATCHER;
    private static final Pattern ABBREVIATION_SPACE_REGEX_PATTERN = Pattern.compile(ABBREVIATION_SPACE_BETWEEN_NAMES_PATTERN);

    private static final String ABBREVIATION_SPACE_NEGATIVE_LOOK_BEHIND = "(?<!(\\.|'|\\s))";
    private static final String SPACE_NOT_PRECEDED_BY_ABBREVIATION_OR_SPACE_PATTERN = ABBREVIATION_SPACE_NEGATIVE_LOOK_BEHIND + "\\s+";

    static boolean doesNotContainAbbreviatedNames(InputNames inputNames) {
        if (nameContainsAbbreviationSpaceBetweenNames(inputNames.fullFirstName())) {
            return false;
        }
        if (nameContainsAbbreviationSpaceBetweenNames(inputNames.fullLastName())) {
            return false;
        }
        return !nameContainsAbbreviationSpaceBetweenNames(inputNames.fullAliasNames());
    }

    private static boolean nameContainsAbbreviationSpaceBetweenNames(String name) {
        return ABBREVIATION_SPACE_REGEX_PATTERN.matcher(name).find();
    }

    public static List<String> splitAroundAbbreviatedNames(String names) {

        if (names.isEmpty()) {
            return emptyList();
        }

        String[] splitNames = names.split(SPACE_NOT_PRECEDED_BY_ABBREVIATION_OR_SPACE_PATTERN);

        return Arrays.stream(splitNames)
                .map(AbbreviatedNamesFunctions::removeMultipleSpaces)
                .collect(Collectors.toList());
    }

    private static String removeMultipleSpaces(String name) {
        return name.replaceAll("\\s+", " ");
    }
}
