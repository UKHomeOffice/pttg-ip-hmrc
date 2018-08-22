package uk.gov.digital.ho.pttg.application.namematching;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class NamePair {
    private final int firstNameIndex;
    private final int lastNameIndex;

    static NamePair of(int firstNameIndex, int lastNameIndex) {
        return new NamePair(firstNameIndex, lastNameIndex);
    }

    PersonName calculateName(List<String> names) {
        if (firstNameIndex > names.size() - 1) {
            throw new IllegalArgumentException(String.format("Cannot retrieve name in position %d from names %s", firstNameIndex, names.toString()));
        }
        if (lastNameIndex > names.size() - 1) {
            throw new IllegalArgumentException(String.format("Cannot retrieve name in position %d from names %s", lastNameIndex, names.toString()));
        }
        return new PersonName(names.get(firstNameIndex), names.get(lastNameIndex));
    }

}