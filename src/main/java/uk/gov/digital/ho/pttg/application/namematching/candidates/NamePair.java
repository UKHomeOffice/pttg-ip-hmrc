package uk.gov.digital.ho.pttg.application.namematching.candidates;

import lombok.AllArgsConstructor;
import uk.gov.digital.ho.pttg.application.namematching.CandidateDerivation;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;
import uk.gov.digital.ho.pttg.application.namematching.Name;

import java.util.List;

import static java.util.Collections.singletonList;
import static lombok.AccessLevel.PRIVATE;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator.NAME_COMBINATIONS;

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
        Name lastName = names.get(lastNameIndex);

        CandidateDerivation derivation =
                new CandidateDerivation(
                        originalNames,
                        singletonList(NAME_COMBINATIONS),
                        firstName.derivation(),
                        lastName.derivation()
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