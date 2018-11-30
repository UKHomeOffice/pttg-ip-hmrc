package uk.gov.digital.ho.pttg.application.namematching;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.Character.UnicodeBlock;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.digital.ho.pttg.application.namematching.Name.End.LEFT;

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

    static boolean isAbbreviation(String name) {
        return name.contains(".") || FULL_STOP_SPACE_REGEX.matcher(name).find();
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

    static Set<UnicodeBlock> calculateUnicodeBlocks(String name) {

        return name.chars()
                       .mapToObj(codePoint -> UnicodeBlock.of(codePoint))
                       .collect(toSet());
    }

    @SafeVarargs
    public static List<Name> combine(List<Name>... namesToCombine) {
        return Stream.of(namesToCombine)
                       .flatMap(Collection::stream)
                       .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    static List<Name> reduceNames(List<Name> names, Name.End end, int amount) {

        if (end == LEFT) {
            names = Lists.reverse(names);
        }

        List<Name> reducedNames = names.stream()
                                          .limit(amount)
                                          .collect(collectingAndThen(toList(), Collections::unmodifiableList));

        if (end == LEFT) {
            return Lists.reverse(reducedNames);
        }

        return reducedNames;
    }

    static List<String> nameStringsOf(List<Name> names) {
        return names.stream()
                       .map(Name::name)
                       .collect(toList());
    }
}
