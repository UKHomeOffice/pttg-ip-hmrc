package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;
import java.util.stream.Collectors;

public class CandidateFunctions {

    public static InputNames removeAdditionalNamesIfOverMax(InputNames inputNames) {
        final int MAX_NAMES = 7;
        final int MAX_LAST_NAMES = 3;

        if (inputNames.size() <= MAX_NAMES) {
            return inputNames;
        }

        List<String> lastNames =
                inputNames.lastNames().stream()
                        .limit(MAX_LAST_NAMES)
                        .collect(Collectors.toList());

        List<String> firstNames =
                inputNames.firstNames().stream()
                        .limit(MAX_NAMES - lastNames.size())
                        .collect(Collectors.toList());

        return new InputNames(firstNames, lastNames);
    }
}
