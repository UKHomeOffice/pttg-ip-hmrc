package uk.gov.digital.ho.pttg.application.namematching;

import org.apache.commons.lang3.StringUtils;
import org.javatuples.Triplet;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.digital.ho.pttg.application.namematching.DerivationAction.*;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.SpecialCharactersFunctions.nameWithSplittersRemoved;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.SpecialCharactersFunctions.nameWithSplittersReplacedBySpaces;

public final class InputNamesFunctions {

    private static final String NAME_SPLITTERS = "-'.";
    private static final String DIACRITICS = "";
    private static final String UMLAUT = "";

    private static final String ANY_LETTER_INCLUDING_UNICODE_MATCHER = "\\p{L}\\p{M}*+";
    private static final String FULL_STOP_SPACE_MATCHER = "\\.\\s+";
    private static final String FULL_STOP_SPACE_BETWEEN_NAMES_PATTERN = ANY_LETTER_INCLUDING_UNICODE_MATCHER + FULL_STOP_SPACE_MATCHER + ANY_LETTER_INCLUDING_UNICODE_MATCHER;

    private InputNamesFunctions() {
        // Don't allow instantiation (even using reflection)
        throw new UnsupportedOperationException("Companion class for InputNames - do not instantiate");
    }

    public static List<String> splitIntoDistinctNames(String combinedNames) {

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

    @SafeVarargs
    public static List<Name> combine(List<Name>... namesToCombine) {
        return Stream.of(namesToCombine)
                       .flatMap(Collection::stream)
                       .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }


    static Optional<Triplet<NameType, Integer, DerivationAction>> locate(String rawName, List<Name> names) {

        Optional<Triplet<NameType, Integer, DerivationAction>> optionalTuple;

        optionalTuple = locateAsWholeName(rawName, names);

        if (optionalTuple.isPresent()) {
            return optionalTuple;
        }

        optionalTuple = locateAsSplitName(rawName, names);

        if (optionalTuple.isPresent()) {
            return optionalTuple;
        }

        optionalTuple = locateAsNameWithSplitterRemoved(rawName, names);

        if (optionalTuple.isPresent()) {
            return optionalTuple;
        }

        return Optional.empty();
    }

    static Optional<Triplet<NameType, Integer, DerivationAction>> locateAsWholeName(String rawName, List<Name> names) {
        return names.stream()
                       .filter(name -> name.name().equals(rawName))
                       .map(name -> Triplet.with(name.nameType(), name.index(), ORIGINAL))
                       .findFirst();
    }

    static Optional<Triplet<NameType, Integer, DerivationAction>> locateAsSplitName(String rawName, List<Name> names) {
        return names.stream()
                       .filter(name -> name.containsNameSplitter())
                       .filter(name -> {
                           List<String> originalNameParts = splitIntoDistinctNames(nameWithSplittersReplacedBySpaces(name.name()));
                           return originalNameParts.indexOf(rawName) >= 0;
                       })
                       .map(name -> {
                           List<String> originalNameParts = splitIntoDistinctNames(nameWithSplittersReplacedBySpaces(name.name()));

                           if (originalNameParts.get(0).equals(rawName)) {
                               return Triplet.with(name.nameType(), name.index(), LEFT_OF_SPLIT);
                           }

                           if (originalNameParts.get(originalNameParts.size() - 1).equals(rawName)) {
                               return Triplet.with(name.nameType(), name.index(), RIGHT_OF_SPLIT);
                           }

                           return Triplet.with(name.nameType(), name.index(), MIDDLE_OF_SPLIT);
                       })
                       .findFirst();
    }

    private static Optional<Triplet<NameType, Integer, DerivationAction>> locateAsNameWithSplitterRemoved(String rawName, List<Name> names) {

        return names.stream()
                       .filter(name -> name.containsNameSplitter())
                       .filter(name -> {
                           String splitterlessName = nameWithSplittersRemoved(name.name());
                           return splitterlessName.equals(rawName);
                       })
                       .map(matchingName -> Triplet.with(matchingName.nameType(), matchingName.index(), DerivationAction.SPLITTER_IGNORED))
                       .findFirst();

    }
}
