package uk.gov.digital.ho.pttg.application.namematching.candidates;

import com.google.common.collect.ImmutableList;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;
import java.util.stream.Collectors;

class CandidateFunctions {

    private static final int MAX_NAMES = 7;
    private static final int MAX_LAST_NAMES = 3;

    static InputNames removeAdditionalNamesIfOverMax(InputNames inputNames) {

        if (inputNames.size() <= MAX_NAMES) {
            return inputNames;
        }

        List<String> lastNames = getMaxPermittedSurnames(inputNames.lastNames());

        List<String> firstNames = getMaxPermittedFirstNames(inputNames.firstNames(), lastNames.size());

        return new InputNames(firstNames, lastNames);
    }

    private static List<String> getMaxPermittedSurnames(List<String> surnames) {
        List<String> reversedRetainedSurnames = ImmutableList.copyOf(surnames).reverse().stream()
                .limit(MAX_LAST_NAMES)
                .collect(Collectors.toList());

        return ImmutableList.copyOf(reversedRetainedSurnames).reverse();
    }

    private static List<String> getMaxPermittedFirstNames(List<String> firstNames, int numberOfRetainedLastNames) {
        return firstNames.stream()
                .limit(MAX_NAMES - numberOfRetainedLastNames)
                .collect(Collectors.toList());
    }
}
