package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.Name;
import uk.gov.digital.ho.pttg.application.namematching.NameDerivation;
import uk.gov.digital.ho.pttg.application.namematching.NameType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.Collections.*;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.DerivationAction.*;
import static uk.gov.digital.ho.pttg.application.namematching.InputNamesFunctions.splitIntoDistinctNames;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.SpecialCharactersFunctions.nameWithSplittersRemoved;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.SpecialCharactersFunctions.nameWithSplittersReplacedBySpaces;

public final class GeneratorFunctions {

    private GeneratorFunctions() {
        // Don't allow instantiation (even using reflection)
        throw new UnsupportedOperationException("Companion class for implementations of NameMatchingCandidateGenerator - do not instantiate");
    }

    public static List<Name> analyse(List<Name> originNames, NameType nameType, List<String> names) {

        if (names.isEmpty()) {
            return unmodifiableList(emptyList());
        }

        AtomicInteger index = new AtomicInteger(0);

        return names.stream()
                       .map(name -> new Name(locate(name, originNames), nameType, index.getAndIncrement(), name))
                       .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    public static Optional<NameDerivation> locate(String rawName, List<Name> names) {

        Optional<NameDerivation> optionalNameOrigin;

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

        return locateAsAbbreviatedPair(rawName, names);
    }

    static Optional<NameDerivation> locateAsWholeName(String rawName, List<Name> names) {
        return names.stream()
                       .filter(name -> name.name().equals(rawName))
                       .map(name -> new NameDerivation(name.nameType(), singletonList(name.index()), name.getLength(), singletonList(ORIGINAL)))
                       .findFirst();
    }

    static Optional<NameDerivation> locateAsSplitName(String rawName, List<Name> names) {
        return names.stream()
                       .filter(Name::containsNameSplitter)
                       .filter(name -> {
                           List<String> originalNameParts = splitIntoDistinctNames(nameWithSplittersReplacedBySpaces(name.name()));
                           return originalNameParts.indexOf(rawName) >= 0;
                       })
                       .map(name -> {
                           List<String> originalNameParts = splitIntoDistinctNames(nameWithSplittersReplacedBySpaces(name.name()));

                           String lhsOfSplit = originalNameParts.get(0);
                           if (lhsOfSplit.equals(rawName)) {
                               return new NameDerivation(
                                       name.nameType(),
                                       singletonList(name.index()),
                                       lhsOfSplit.length(),
                                       singletonList(LEFT_OF_SPLIT));
                           }

                           String rhsOfSplit = originalNameParts.get(originalNameParts.size() - 1);
                           if (rhsOfSplit.equals(rawName)) {
                               return new NameDerivation(
                                       name.nameType(),
                                       singletonList(name.index()),
                                       rhsOfSplit.length(),
                                       singletonList(RIGHT_OF_SPLIT));
                           }

                           return new NameDerivation(
                                   name.nameType(),
                                   singletonList(name.index()),
                                   name.getLength() - (2 + lhsOfSplit.length() + rhsOfSplit.length()),
                                   singletonList(MIDDLE_OF_SPLIT));
                       })
                       .findFirst();
    }

    private static Optional<NameDerivation> locateAsNameWithSplitterRemoved(String rawName, List<Name> names) {

        return names.stream()
                       .filter(Name::containsNameSplitter)
                       .filter(name -> {
                           String splitterlessName = nameWithSplittersRemoved(name.name());
                           return splitterlessName.equals(rawName);
                       })
                       .map(matchingName -> new NameDerivation(
                               matchingName.nameType(),
                               singletonList(matchingName.index()),
                               rawName.length(),
                               singletonList(SPLITTER_IGNORED)))
                       .findFirst();

    }

    private static Optional<NameDerivation> locateAsAbbreviatedPair(String rawName, List<Name> names) {

        for (int i = 0; i < names.size() - 1; i++) {

            String namePair = String.join(" ", names.get(i).name(), names.get(i + 1).name());

            if (namePair.equals(rawName)) {
                return Optional.of(new NameDerivation(
                        names.get(i).nameType(),
                        singletonList(i),
                        namePair.length(),
                        singletonList(ABBREVIATED_PAIR)));
            }
        }

        return Optional.empty();
    }

    static List<Integer> nameIndexes(int size) {
        return IntStream.range(0, size)
                       .boxed()
                       .collect(toList());
    }
}
