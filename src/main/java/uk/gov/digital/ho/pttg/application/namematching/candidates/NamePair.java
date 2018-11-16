package uk.gov.digital.ho.pttg.application.namematching.candidates;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import uk.gov.digital.ho.pttg.application.namematching.*;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static uk.gov.digital.ho.pttg.application.namematching.DerivationAction.COMBINATION;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.FIRST;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.LAST;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator.NAME_MATCHING;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class NamePair {
    private final int firstNameIndex;
    private final int lastNameIndex;

    static NamePair of(int firstNameIndex, int lastNameIndex) {
        return new NamePair(firstNameIndex, lastNameIndex);
    }

    CandidateName calculateName(InputNames inputNames, List<Name> names) {

        validateNamePair(names.size());

        Name firstName = names.get(firstNameIndex);
        Name lastName = names.get(lastNameIndex);

        CandidateDerivation derivation =
                new CandidateDerivation(
                        inputNames,
                        singletonList(NAME_MATCHING),
                        new Derivation(
                                FIRST,
                                asList(inputNames.indexOfFirstName(firstName)),
                                firstName.name().length(),
                                firstName.containsDiacritics(),
                                firstName.containsUmlauts(),
                                firstName.containsFullStopSpace(),
                                firstName.containsNameSplitter(),
                                singletonList(COMBINATION)),
                        new Derivation(
                                LAST,
                                asList(inputNames.indexOfLastName(lastName)),
                                lastName.name().length(),
                                lastName.containsDiacritics(),
                                lastName.containsUmlauts(),
                                lastName.containsFullStopSpace(),
                                lastName.containsNameSplitter(),
                                singletonList(COMBINATION))
                );

        return new CandidateName(firstName.name(), lastName.name(), derivation);
    }

    void validateNamePair(int size) {

        if (firstNameIndex > size - 1) {
            throw new IllegalArgumentException(String.format("Cannot retrieve name in position %d", firstNameIndex));
        }

        if (lastNameIndex > size - 1) {
            throw new IllegalArgumentException(String.format("Cannot retrieve name in position %d", lastNameIndex));
        }
    }

}