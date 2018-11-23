package uk.gov.digital.ho.pttg.application.namematching;

import org.apache.commons.lang3.StringUtils;
import uk.gov.digital.ho.pttg.application.namematching.candidates.NameOrigin;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.digital.ho.pttg.application.namematching.DerivationAction.*;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.SpecialCharactersFunctions.nameWithSplittersRemoved;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.SpecialCharactersFunctions.nameWithSplittersReplacedBySpaces;

public final class InputNamesFunctions {

    private static final String NAME_SPLITTERS = "-'.";

    private static final String ANY_LETTER_INCLUDING_UNICODE_MATCHER = "\\p{L}\\p{M}*+";
    private static final String FULL_STOP_SPACE_MATCHER = "\\.\\s+";
    private static final String FULL_STOP_SPACE_BETWEEN_NAMES_PATTERN = ANY_LETTER_INCLUDING_UNICODE_MATCHER + FULL_STOP_SPACE_MATCHER + ANY_LETTER_INCLUDING_UNICODE_MATCHER;
    private static final Pattern FULL_STOP_SPACE_REGEX = Pattern.compile(FULL_STOP_SPACE_BETWEEN_NAMES_PATTERN);
    private static final Pattern DIACRITIC_REGEX = Pattern.compile(".*[\u00C0-\u017E].*");
    private static final Pattern UMLAUT_REGEX = Pattern.compile(".*[\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF].*");


    private InputNamesFunctions() {
        // Don't allow instantiation (even using reflection)
        throw new UnsupportedOperationException("Companion class for InputNames - do not instantiate");
    }

    public static List<String> splitIntoDistinctNames(String combinedNames) {

        if (isBlank(combinedNames)) {
            return emptyList();
        }

        String[] splitNames = combinedNames.trim().split("\\s+");

        return asList(splitNames);
    }

    static boolean hasFullStopSpace(String name) {
        return FULL_STOP_SPACE_REGEX.matcher(name).find();
    }

    static boolean hasNameSplitter(String name) {
        return StringUtils.containsAny(name, NAME_SPLITTERS);
    }

    static boolean hasDiacritics(String name) {
        return DIACRITIC_REGEX.matcher(name).find();
    }

    static boolean hasUmlauts(String name) {
        return UMLAUT_REGEX.matcher(name).find();
    }

    static Set<String> calculateUnicodeBlocks(String name) {

        return name.chars()
                       .mapToObj(codePoint -> Character.UnicodeBlock.of(codePoint).toString())
                       .collect(toSet());
    }

    @SafeVarargs
    public static List<Name> combine(List<Name>... namesToCombine) {
        return Stream.of(namesToCombine)
                       .flatMap(Collection::stream)
                       .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }


    static Optional<NameOrigin> locate(String rawName, List<Name> names) {

        Optional<NameOrigin> optionalNameOrigin;

        optionalNameOrigin = locateAsWholeName(rawName, names);

        if (optionalNameOrigin.isPresent()) {
            return optionalNameOrigin;
        }

        optionalNameOrigin = locateAsSplitName(rawName, names);

        if (optionalNameOrigin.isPresent()) {
            return optionalNameOrigin;
        }

        optionalNameOrigin = locateAsNameWithSplitterRemoved(rawName, names);

        if (optionalNameOrigin.isPresent()) {
            return optionalNameOrigin;
        }

        optionalNameOrigin = locateAsAbbreviatedPair(rawName, names);

        return optionalNameOrigin;

    }

    static Optional<NameOrigin> locateAsWholeName(String rawName, List<Name> names) {
        return names.stream()
                       .filter(name -> name.name().equals(rawName))
                       .map(name -> new NameOrigin(name.nameType(), name.index(), ORIGINAL))
                       .findFirst();
    }

    static Optional<NameOrigin> locateAsSplitName(String rawName, List<Name> names) {
        return names.stream()
                       .filter(Name::containsNameSplitter)
                       .filter(name -> {
                           List<String> originalNameParts = splitIntoDistinctNames(nameWithSplittersReplacedBySpaces(name.name()));
                           return originalNameParts.indexOf(rawName) >= 0;
                       })
                       .map(name -> {
                           List<String> originalNameParts = splitIntoDistinctNames(nameWithSplittersReplacedBySpaces(name.name()));

                           if (originalNameParts.get(0).equals(rawName)) {
                               return new NameOrigin(name.nameType(), name.index(), LEFT_OF_SPLIT);
                           }

                           if (originalNameParts.get(originalNameParts.size() - 1).equals(rawName)) {
                               return new NameOrigin(name.nameType(), name.index(), RIGHT_OF_SPLIT);
                           }

                           return new NameOrigin(name.nameType(), name.index(), MIDDLE_OF_SPLIT);
                       })
                       .findFirst();
    }

    private static Optional<NameOrigin> locateAsNameWithSplitterRemoved(String rawName, List<Name> names) {

        return names.stream()
                       .filter(Name::containsNameSplitter)
                       .filter(name -> {
                           String splitterlessName = nameWithSplittersRemoved(name.name());
                           return splitterlessName.equals(rawName);
                       })
                       .map(matchingName -> new NameOrigin(matchingName.nameType(), matchingName.index(), SPLITTER_IGNORED))
                       .findFirst();

    }

    private static Optional<NameOrigin> locateAsAbbreviatedPair(String rawName, List<Name> names) {

        for (int i = 0; i < names.size() - 1; i++) {
            String namePair = String.join(" ", names.get(i).name(), names.get(i + 1).name());
            if (namePair.equals(rawName)) {
                return Optional.of(new NameOrigin(names.get(i).nameType(), i, ABBREVIATED_PAIR));
            }
        }

        return Optional.empty();
    }
}
