package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.InputNames;

final class SpecialCharactersFunctions {

    static boolean namesAreEmpty(InputNames inputNames) {
        return inputNames.rawFirstNames().isEmpty() && inputNames.rawLastNames().isEmpty();
    }

    static boolean namesAreNotEmpty(InputNames inputNames) {
        return !namesAreEmpty(inputNames);
    }
}
