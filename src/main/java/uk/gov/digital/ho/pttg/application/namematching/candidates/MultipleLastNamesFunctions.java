package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.*;
import uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.DerivationAction.COMBINATION;
import static uk.gov.digital.ho.pttg.application.namematching.NameDerivation.ALL_FIRST_NAMES;
import static uk.gov.digital.ho.pttg.application.namematching.NameDerivation.ALL_LAST_NAMES;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.LAST;

class MultipleLastNamesFunctions {

    // TODO: Remove - only used in tests
    static List<CandidateName> generateAllLastNameCombinations(List<String> firstNames, List<String> lastNameCombinations) {
        List<CandidateName> combinations =
                firstNames.stream()
                        .flatMap(firstName -> lastNameCombinations.stream()
                                                      .map(lastNameCombination -> new CandidateName(
                                                              firstName,
                                                              lastNameCombination,
                                                              new CandidateDerivation(
                                                                      null,
                                                                      singletonList(null),
                                                                      ALL_FIRST_NAMES,
                                                                      ALL_LAST_NAMES))))
                        .collect(toList());

        return Collections.unmodifiableList(combinations);
    }

    // TODO: Remove - only used in tests
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

    static List<CandidateName> generateNameCombinations(InputNames originalNames, InputNames namesToProcess, List<CandidateName> lastNameCandidates) {

        List<CandidateName> combinations =
                namesToProcess.firstNames().stream()
                        .flatMap(firstName -> lastNameCandidates.stream()
                                                      .map(lastNameCandidate -> generateCandidate(originalNames, firstName, lastNameCandidate)))
                        .collect(toList());

        return Collections.unmodifiableList(combinations);
    }

    private static CandidateName generateCandidate(InputNames originalNames, Name firstName, CandidateName lastNameCandidate) {
        return new CandidateName(
                firstName.name(),
                lastNameCandidate.lastName(),
                new CandidateDerivation(
                        originalNames,
                        lastNameCandidate.derivation().generators(),
                        new NameDerivation(firstName),
                        lastNameCandidate.derivation().lastName()));
    }

    static List<CandidateName> generateNobiliaryLastNameCombinations(InputNames namesToProcess, List<Generator> generators, List<Name> lastNames) {
        final int HMRC_SURNAME_LENGTH = 3;

        return lastNames.stream()
                       .filter(lastName -> lastName.name().length() < HMRC_SURNAME_LENGTH)
                       .distinct()
                       .flatMap(lastName1 -> lastNames.stream()
                                                     .filter(lastName2 -> !lastName1.equals(lastName2))
                                                     .map(lastname2 -> generateCandidateLastName(namesToProcess, generators, lastName1, lastname2)))
                       .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    static CandidateName generateCandidateLastName(InputNames inputNames, List<Generator> generators, Name lastName1, Name lastName2) {
        return new CandidateName(
                null,
                String.format("%s %s", lastName1.name(), lastName2.name()),
                new CandidateDerivation(
                        inputNames,
                        generators,
                        null,
                        new NameDerivation(
                                LAST,
                                asList(lastName1.index(), lastName2.index()),
                                lastName1.name().length() + lastName2.name().length(),
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
