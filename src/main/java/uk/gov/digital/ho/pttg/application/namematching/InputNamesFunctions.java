package uk.gov.digital.ho.pttg.application.namematching;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

final class InputNamesFunctions {

    private static final String NAME_SPLITTERS = "-'.";
    private static final String DIACRITICS = "";
    private static final String UMLAUT = "";

    private static final String ANY_LETTER_INCLUDING_UNICODE_MATCHER = "\\p{L}\\p{M}*+";
    private static final String FULL_STOP_SPACE_MATCHER = "\\.\\s+";
    private static final String FULL_STOP_SPACE_BETWEEN_NAMES_PATTERN = ANY_LETTER_INCLUDING_UNICODE_MATCHER + FULL_STOP_SPACE_MATCHER + ANY_LETTER_INCLUDING_UNICODE_MATCHER;

    private static final String FULL_STOP_SPACE_NEGATIVE_LOOK_BEHIND = "(?<!(\\.|\\s))";

    private InputNamesFunctions() {
        // Don't allow instantiation (even using reflection)
        throw new UnsupportedOperationException("Companion class for InputNames - do not instantiate");
    }

    static List<String> splitIntoDistinctNames(String combinedNames) {

        if (isBlank(combinedNames)) {
            return Collections.emptyList();
        }

        String[] splitNames = combinedNames.trim().split("\\s+");

        return Arrays.asList(splitNames);
    }

    static boolean hasFullStopSpace(String name) {
        return Pattern.compile(FULL_STOP_SPACE_BETWEEN_NAMES_PATTERN).matcher(name).find();
    }

    static boolean hasNameSplitter(String name) {
        return StringUtils.containsAny(name, NAME_SPLITTERS);
    }

    static boolean hasDiacritics(String name) {
        return StringUtils.containsAny(name, DIACRITICS);
    }

    static boolean hasUmnlauts(String name) {
        return StringUtils.containsAny(name, UMLAUT);
    }
}
