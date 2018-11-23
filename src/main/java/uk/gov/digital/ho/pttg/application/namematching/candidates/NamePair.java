package uk.gov.digital.ho.pttg.application.namematching.candidates;

import lombok.AllArgsConstructor;
import uk.gov.digital.ho.pttg.application.namematching.*;

import java.util.List;

import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator.NAME_MATCHING;

@AllArgsConstructor(access = PRIVATE)
class NamePair {

    private final int firstNameIndex;
    private final int lastNameIndex;

    static NamePair of(int firstNameIndex, int lastNameIndex) {
        return new NamePair(firstNameIndex, lastNameIndex);
    }

    CandidateName calculateName(InputNames originalNames, List<Name> names) {

        validateNamePair(names.size());

        Name firstName = names.get(firstNameIndex);
        NameOrigin firstNameOrigin = originalNames.locateName(firstName.name());

        Name lastName = names.get(lastNameIndex);
        NameOrigin lastNameOrigin = originalNames.locateName(lastName.name());

        CandidateDerivation derivation =
                new CandidateDerivation(
                        originalNames,
                        singletonList(NAME_MATCHING),
                        new NameDerivation(
                                firstNameOrigin.nameType(),
                                singletonList(firstNameOrigin.position()),
                                firstName.name().length(),
                                singletonList(firstNameOrigin.derivationAction())),
                        new NameDerivation(
                                lastNameOrigin.nameType(),
                                singletonList(lastNameOrigin.position()),
                                lastName.name().length(),
                                singletonList(lastNameOrigin.derivationAction()))
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