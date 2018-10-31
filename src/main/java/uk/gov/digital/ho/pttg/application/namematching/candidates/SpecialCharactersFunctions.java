package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.InputNames;

final class SpecialCharactersFunctions {
    static boolean namesAreNotEmpty(InputNames inputNames) {
        return !(inputNames.firstNames().isEmpty() && inputNames.lastNames().isEmpty());
    }

    static String joinAllAliasSurnames(InputNames inputNames) {
        return String.join(" ", inputNames.aliasSurnames());
    }
}
