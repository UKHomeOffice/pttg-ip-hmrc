package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.DerivationAction.COMBINATION;
import static uk.gov.digital.ho.pttg.application.namematching.DerivationAction.ORIGINAL;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.FIRST;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.LAST;

class MultipleLastNamesFunctions {

    static List<CandidateName> generateAllLastNameCombinations(List<String> firstNames, List<String> lastNameCombinations) {
        List<CandidateName> combinations =
                firstNames.stream()
                        .flatMap(firstName -> lastNameCombinations.stream()
                                .map(lastNameCombination -> new CandidateName(firstName, lastNameCombination)))
                        .collect(toList());

        return Collections.unmodifiableList(combinations);
    }

    static List<String> generateLastNameCombinations(List<String> lastNames) {
        final int HMRC_SURNAME_LENGTH = 3;

        return lastNames.stream()
                       .filter(lastName -> lastName.length() < HMRC_SURNAME_LENGTH)
                       .distinct()
                       .flatMap(lastName1 -> lastNames.stream()
                                                     .filter(lastName2 -> !lastName1.equals(lastName2))
                                                     .map(lastName2 -> lastName1 + " " + lastName2))
                       .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    static List<CandidateName> generateNameCombinations(List<Name> firstNames, List<CandidateName> lastNameCandidates) {
        List<CandidateName> combinations =
                firstNames.stream()
                        .flatMap(firstName -> lastNameCandidates.stream()
                                                      .map(lastNameCandidate -> generateCandidate(firstName, lastNameCandidate)))
                        .collect(toList());

        return Collections.unmodifiableList(combinations);
    }

    private static CandidateName generateCandidate(Name firstName, CandidateName lastNameCandidate) {
        return new CandidateName(
                firstName.name(),
                lastNameCandidate.lastName(),
                new CandidateDerivation(
                        lastNameCandidate.derivation().generators(),
                        new Derivation(
                                FIRST,
                                Arrays.asList(firstName.index()),
                                firstName.name().length(),
                                firstName.containsDiacritics(),
                                firstName.containsUmlauts(),
                                firstName.containsFullStopSpace(),
                                firstName.containsNameSplitter(),
                                singletonList(ORIGINAL)),
                        lastNameCandidate.derivation().lastName()));
    }

    static List<CandidateName> generateNobiliaryLastNameCombinations(List<Integer> generators, List<Name> lastNames) {
        final int HMRC_SURNAME_LENGTH = 3;

        return lastNames.stream()
                       .filter(lastName -> lastName.name().length() < HMRC_SURNAME_LENGTH)
                       .distinct()
                       .flatMap(lastName1 -> lastNames.stream()
                                                     .filter(lastName2 -> !lastName1.equals(lastName2))
                                                     .map(lastname2 -> generateCandidateLastName(generators, lastName1, lastname2)))
                       .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    static CandidateName generateCandidateLastName(List<Integer> generators, Name lastName1, Name lastname2) {
        return new CandidateName(
                null,
                String.format("%s %s", lastName1.name(), lastname2.name()),
                new CandidateDerivation(
                        generators,
                        null,
                        new Derivation(
                                LAST,
                                Arrays.asList(lastName1.index(), lastname2.index()),
                                lastName1.name().length() + lastname2.name().length(),
                                lastName1.containsDiacritics() || lastname2.containsDiacritics(),
                                lastName1.containsUmlauts() || lastname2.containsUmlauts(),
                                lastName1.containsFullStopSpace() || lastname2.containsFullStopSpace(),
                                lastName1.containsNameSplitter() || lastname2.containsNameSplitter(),
                                singletonList(COMBINATION))));
    }

    // TODO: Remove - only used in tests
    static List<String> addMultiPartLastNameToCombination(List<String> lastNameCombinations, List<String> listOfLastNames) {
        final int MIN_NAMES_FOR_MULTIPART = 3;

        List<String> newLastNameCombinations = new ArrayList<>();

        if (listOfLastNames.size() >= MIN_NAMES_FOR_MULTIPART) {
            newLastNameCombinations.add(String.join(" ", listOfLastNames));
        }

        newLastNameCombinations.addAll(lastNameCombinations);
        return Collections.unmodifiableList(newLastNameCombinations);
    }

    // TODO: Remove - only used in tests
    static List<CandidateName> addFullNameIfNotAlreadyPresent(List<CandidateName> candidates, InputNames inputNames) {
        CandidateName fullName = new CandidateName(inputNames.fullFirstName(), inputNames.fullLastName());

        List<CandidateName> newCandidates = new ArrayList<>();

        if (!candidates.contains(fullName)) {
            newCandidates.add(fullName);
        }

        newCandidates.addAll(candidates);
        return Collections.unmodifiableList(newCandidates);
    }
}
