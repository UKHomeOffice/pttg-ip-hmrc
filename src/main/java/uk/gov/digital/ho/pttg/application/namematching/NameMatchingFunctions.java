package uk.gov.digital.ho.pttg.application.namematching;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class NameMatchingFunctions {

    public static List<String> splitIntoDistinctNames(String name) {
        if (isBlank(name)) {
            return Collections.emptyList();
        }

        String[] splitNames = name.trim().split("\\s+");

        return Arrays.asList(splitNames);
    }

    public static List<String> splitTwoIntoDistinctNames(String firstName, String lastName) {
        List<String> names = new ArrayList<>();

        names.addAll(splitIntoDistinctNames(firstName));
        names.addAll(splitIntoDistinctNames(lastName));

        return Collections.unmodifiableList(names);
    }

    public static List<String> removeAdditionalNamesIfOverMax(List<String> incomingNames) {
        final int MAX_NAMES = 7;

        int numberOfNames = incomingNames.size();

        if (numberOfNames <= MAX_NAMES) {
            return incomingNames;
        }

        List<String> firstFourNames = incomingNames.subList(0, 4);
        List<String> lastThreeNames = incomingNames.subList(numberOfNames - 3, numberOfNames);

        return newArrayList(concat(firstFourNames, lastThreeNames));
    }

    static List<PersonName> deduplicate(List<PersonName> candidateNames) {
        Set<PersonName> seenHmrcEquivalentNames = new HashSet<>();
        return candidateNames.stream()
                .filter(name -> seenHmrcEquivalentNames.add(name.hmrcNameMatchingEquivalent()))
                .collect(Collectors.toList());
    }


}
