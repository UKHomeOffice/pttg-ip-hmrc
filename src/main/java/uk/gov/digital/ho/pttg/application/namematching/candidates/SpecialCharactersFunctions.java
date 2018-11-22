package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.apache.commons.lang3.StringUtils;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

public final class SpecialCharactersFunctions {

    static final String NAME_SPLITTERS = "-'.";
    private static final String NAME_SPLITTER_REGEX = "[" + NAME_SPLITTERS + "]";

    private SpecialCharactersFunctions() {
        // Don't allow instantiation (even using reflection)
        throw new UnsupportedOperationException("Companion class for SpecialCharacters - do not instantiate");
    }

    static boolean namesAreEmpty(InputNames inputNames) {
        return inputNames.rawFirstNames().isEmpty() && inputNames.rawLastNames().isEmpty();
    }

    static boolean namesAreNotEmpty(InputNames inputNames) {
        return !namesAreEmpty(inputNames);
    }

    public static String nameWithSplittersRemoved(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, "");
    }

    static InputNames nameWithSplittersRemoved(InputNames inputNames) {

        return new InputNames(
                nameWithSplittersRemoved(inputNames.fullFirstName()),
                nameWithSplittersRemoved(inputNames.fullLastName()),
                nameWithSplittersRemoved(inputNames.fullAliasNames()));
    }

    public static String nameWithSplittersReplacedBySpaces(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, " ");
    }

    static InputNames nameWithSplittersReplacedBySpaces(InputNames inputNames) {

        return new InputNames(
                nameWithSplittersReplacedBySpaces(inputNames.fullFirstName()),
                nameWithSplittersReplacedBySpaces(inputNames.fullLastName()),
                nameWithSplittersReplacedBySpaces(inputNames.fullAliasNames()));
    }

    static boolean namesContainSplitters(InputNames inputNames) {
        return StringUtils.containsAny(inputNames.fullName(), NAME_SPLITTERS) || StringUtils.containsAny(inputNames.fullAliasNames(), NAME_SPLITTERS);
    }
}
