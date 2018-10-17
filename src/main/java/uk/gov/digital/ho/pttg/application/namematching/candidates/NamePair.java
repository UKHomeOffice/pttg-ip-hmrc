package uk.gov.digital.ho.pttg.application.namematching.candidates;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class NamePair {
    private final int firstNameIndex;
    private final int lastNameIndex;

    static NamePair of(int firstNameIndex, int lastNameIndex) {
        return new NamePair(firstNameIndex, lastNameIndex);
    }

    CandidateName calculateName(List<String> names) {
        if (firstNameIndex > names.size() - 1) {
            throw new IllegalArgumentException(String.format("Cannot retrieve name in position %d from names %s", firstNameIndex, names.toString()));
        }
        if (lastNameIndex > names.size() - 1) {
            throw new IllegalArgumentException(String.format("Cannot retrieve name in position %d from names %s", lastNameIndex, names.toString()));
        }
        return new CandidateName(names.get(firstNameIndex), names.get(lastNameIndex));
    }

}