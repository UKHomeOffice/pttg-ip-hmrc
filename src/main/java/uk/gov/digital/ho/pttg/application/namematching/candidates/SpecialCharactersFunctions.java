package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.apache.commons.lang3.StringUtils;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;
import uk.gov.digital.ho.pttg.application.namematching.Name;

import java.util.List;

import static uk.gov.digital.ho.pttg.application.namematching.InputNamesFunctions.splitIntoDistinctNames;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.*;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.GeneratorFunctions.analyse;

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

        List<Name> splitterlessFirstNames = analyse(inputNames.firstNames(), FIRST, splitIntoDistinctNames(nameWithSplittersRemoved(inputNames.fullFirstName())));
        List<Name> splitterlessLastNames = analyse(inputNames.lastNames(), LAST, splitIntoDistinctNames(nameWithSplittersRemoved(inputNames.fullLastName())));
        List<Name> splitterlessAliasNames = analyse(inputNames.aliasSurnames(), ALIAS, splitIntoDistinctNames(nameWithSplittersRemoved(inputNames.fullAliasNames())));

        return new InputNames(splitterlessFirstNames, splitterlessLastNames, splitterlessAliasNames);

    }

    public static String nameWithSplittersReplacedBySpaces(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, " ");
    }

    static InputNames nameWithSplittersReplacedBySpaces(InputNames inputNames) {

        List<Name> splitFirstNames = analyse(inputNames.firstNames(), FIRST, splitIntoDistinctNames(nameWithSplittersReplacedBySpaces(inputNames.fullFirstName())));
        List<Name> splitLastNames = analyse(inputNames.lastNames(), LAST, splitIntoDistinctNames(nameWithSplittersReplacedBySpaces(inputNames.fullLastName())));
        List<Name> splitAliasNames = analyse(inputNames.aliasSurnames(), ALIAS, splitIntoDistinctNames(nameWithSplittersReplacedBySpaces(inputNames.fullAliasNames())));

        return new InputNames(splitFirstNames, splitLastNames, splitAliasNames);
    }

    static boolean namesContainSplitters(InputNames inputNames) {
        return StringUtils.containsAny(inputNames.fullName(), NAME_SPLITTERS) || StringUtils.containsAny(inputNames.fullAliasNames(), NAME_SPLITTERS);
    }
}
