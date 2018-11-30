package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.*;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.reverse;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.InputNamesFunctions.combine;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator.ALIAS_COMBINATIONS;

final class AliasCombinationsFunctions {

    private AliasCombinationsFunctions() {
        throw new UnsupportedOperationException("Helper class for AliasSurnameCombinationsFunctions containing only static methods - no need to instantiate.");
    }

    static List<String> removeName(String nameToRemove, List<String> names) {
        List<String> filteredNames = new ArrayList<>(names);
        filteredNames.remove(nameToRemove);
        return filteredNames;
    }

    static List<Name> removeName(Name nameToRemove, List<Name> names) {
        List<Name> filteredNames = new ArrayList<>(names);
        filteredNames.remove(nameToRemove);
        return filteredNames;
    }

    static List<CandidateName> nonAliasFirstAliasLastCombinations(InputNames originalNames, InputNames inputNames) {

        List<Name> reversedAliasSurnames = new ArrayList<>(inputNames.aliasSurnames());
        reverse(reversedAliasSurnames);

        List<Name> nonAliasNames = combine(inputNames.firstNames(), inputNames.lastNames());

        return reversedAliasSurnames.stream()
                       .flatMap(last -> nonAliasNames.stream()
                                                        .map(first -> createCandidateName(originalNames, first, last)))
                       .collect(toList());
    }

    static List<CandidateName> firstNameCombinations(InputNames originalNames, InputNames inputNames) {

        return inputNames.firstNames().stream()
                       .flatMap(first -> removeName(first, inputNames.firstNames()).stream()
                                                     .map(last -> createCandidateName(originalNames, first, last)))
                       .collect(toList());
    }

    static List<CandidateName> nonAliasSurnameAsFirstNameCombinations(InputNames originalNames, InputNames inputNames) {

        return inputNames.lastNames().stream()
                       .flatMap(first -> inputNames.firstNames().stream()
                                                     .map(last -> createCandidateName(originalNames, first, last)))
                       .collect(toList());

    }

    static List<CandidateName> aliasSurnameAsFirstNameCombinations(InputNames originalNames, InputNames inputNames) {

        List<Name> allNames = combine(inputNames.firstNames(), inputNames.lastNames(), inputNames.aliasSurnames());

        return inputNames.aliasSurnames().stream()
                       .flatMap(first -> removeName(first, allNames).stream()
                                                     .map(last -> createCandidateName(originalNames, first, last)))
                       .collect(toList());
    }

    static List<CandidateName> nonAliasFirstNamesAndLastNameCombinations(InputNames originalNames, InputNames inputNames) {

        List<Name> reversedLastNames = new ArrayList<>(inputNames.lastNames());
        reverse(reversedLastNames);

        List<Name> nonAliasNames = combine(inputNames.firstNames(), inputNames.lastNames());

        return reversedLastNames.stream()
                       .flatMap(last -> removeName(last, nonAliasNames).stream()
                                                        .map(first -> createCandidateName(originalNames, first, last)))
                       .collect(toList());
    }

    private static CandidateName createCandidateName(InputNames originalNames, Name first, Name last) {
        NameDerivation firstNameDerivation = new NameDerivation(first);
        NameDerivation lastNameDerivation = new NameDerivation(last);

        return new CandidateName(
                first.name(),
                last.name(),
                new CandidateDerivation(
                        originalNames,
                        singletonList(ALIAS_COMBINATIONS),
                        firstNameDerivation,
                        lastNameDerivation));
    }
}