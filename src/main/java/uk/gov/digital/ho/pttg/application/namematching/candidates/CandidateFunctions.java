package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import static uk.gov.digital.ho.pttg.application.namematching.Name.End.LEFT;
import static uk.gov.digital.ho.pttg.application.namematching.Name.End.RIGHT;

class CandidateFunctions {

    private static final int MAX_NAMES = 7;
    private static final int MAX_LAST_NAMES = 3;

    static InputNames removeAdditionalNamesIfOverMax(InputNames inputNames) {

        if (inputNames.size() <= MAX_NAMES) {
            return inputNames;
        }

        InputNames reducedLastNames = inputNames.reduceLastNames(LEFT, MAX_LAST_NAMES);

        return reducedLastNames.reduceFirstNames(RIGHT, MAX_NAMES - reducedLastNames.rawLastNames().size());
    }
}
