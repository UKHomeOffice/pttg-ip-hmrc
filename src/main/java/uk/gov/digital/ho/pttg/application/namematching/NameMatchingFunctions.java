package uk.gov.digital.ho.pttg.application.namematching;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class NameMatchingFunctions {

    static List<String> splitIntoDistinctNames(String name) {
        if (isBlank(name)) {
            return Collections.emptyList();
        }

        String[] splitNames = name.trim().split("\\s+");

        return Arrays.asList(splitNames);
    }

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

    static List<CandidateName> deduplicate(List<CandidateName> candidateNames) {
        Set<CandidateName> seenHmrcEquivalentNames = new HashSet<>();
        return candidateNames.stream()
                .filter(name -> seenHmrcEquivalentNames.add(name.hmrcNameMatchingEquivalent()))
                .collect(Collectors.toList());
    }


}
