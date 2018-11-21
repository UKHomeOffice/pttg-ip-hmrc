package uk.gov.digital.ho.pttg.application.namematching.candidates;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.javatuples.Triplet;
import uk.gov.digital.ho.pttg.application.namematching.*;

import java.util.List;

import static java.util.Collections.singletonList;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator.NAME_MATCHING;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class NamePair {

    private final int firstNameIndex;
    private final int lastNameIndex;

    static NamePair of(int firstNameIndex, int lastNameIndex) {
        return new NamePair(firstNameIndex, lastNameIndex);
    }

    CandidateName calculateName(InputNames originalNames, List<Name> names) {

        validateNamePair(names.size());

        Name firstName = names.get(firstNameIndex);
        Triplet<NameType, Integer, DerivationAction> firstNameOrigin = originalNames.locateName(firstName.name());

        Name lastName = names.get(lastNameIndex);
        Triplet<NameType, Integer, DerivationAction> lastNameOrigin = originalNames.locateName(lastName.name());

        CandidateDerivation derivation =
                new CandidateDerivation(
                        originalNames,
                        singletonList(NAME_MATCHING),
                        new NameDerivation(
                                firstNameOrigin.getValue0(),
                                singletonList(firstNameOrigin.getValue1()),
                                firstName.name().length(),
                                singletonList(firstNameOrigin.getValue2())),
                        new NameDerivation(
                                lastNameOrigin.getValue0(),
                                singletonList(lastNameOrigin.getValue1()),
                                lastName.name().length(),
                                singletonList(lastNameOrigin.getValue2()))
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