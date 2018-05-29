package uk.gov.digital.ho.pttg.application.retry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class NameMatchingCandidatesGenerator {

    // This maps the number of names to a list containing pairs of candidate names in the order defined by the requirements
    private static final Map<Integer, List<List<Integer>>> CANDIDATE_NAME_RULES = ImmutableMap.of(
            1, ImmutableList.of(
                    ImmutableList.of(0, 0)),
            2, ImmutableList.of(
                    ImmutableList.of(0, 1), ImmutableList.of(1, 0)),
            3, ImmutableList.of(
                    ImmutableList.of(0, 2), ImmutableList.of(1, 2),
                    ImmutableList.of(2, 0), ImmutableList.of(2, 1),
                    ImmutableList.of(0, 1), ImmutableList.of(1, 0)),
            4, ImmutableList.of(
                    ImmutableList.of(0, 3), ImmutableList.of(1, 3), ImmutableList.of(2 ,3),
                    ImmutableList.of(0, 1), ImmutableList.of(0, 2), ImmutableList.of(1, 0),
                    ImmutableList.of(2, 0), ImmutableList.of(3, 0), ImmutableList.of(1, 2),
                    ImmutableList.of(2, 1), ImmutableList.of(3, 1), ImmutableList.of(3, 2)),
            5, ImmutableList.of(
                    ImmutableList.of(0, 4), ImmutableList.of(1, 4), ImmutableList.of(2, 4), ImmutableList.of(3, 4),
                    ImmutableList.of(0, 1), ImmutableList.of(0, 2), ImmutableList.of(0, 3), ImmutableList.of(1, 0),
                    ImmutableList.of(1, 2), ImmutableList.of(1, 3), ImmutableList.of(2, 0), ImmutableList.of(2, 1),
                    ImmutableList.of(2, 3), ImmutableList.of(3, 0), ImmutableList.of(3, 1), ImmutableList.of(3, 2),
                    ImmutableList.of(4, 0), ImmutableList.of(4, 1), ImmutableList.of(4, 2), ImmutableList.of(4, 3))
    );


    public static List<String> generateCandidates(String firstName, String lastName) {
        validateName(firstName, lastName);

        List<String> candidates = new ArrayList<>();
        List<String> allNames = findAllNames(firstName, lastName);

        candidates.addAll(
                CANDIDATE_NAME_RULES
                        .get(allNames.size())
                        .stream()
                        .map(nameSelectorRule -> allNames.get(nameSelectorRule.get(0)) + " " + allNames.get(nameSelectorRule.get(1)))
                        .collect(toList())
        );

        return candidates;
    }

    private static void validateName(String firstName, String lastName) {
        if(isEmptyName(firstName) && isEmptyName(lastName)) {
            throw new IllegalArgumentException("At least one name is required");
        }
    }

    private static boolean isEmptyName(String name) {
        if(name == null) {
            return true;
        }
        if(name.trim().equals("")) {
            return true;
        }
        return false;
    }

    private static List<String> findAllNames(String firstName, String lastName) {
        List<String> allNames = new ArrayList<>();
        if(!isEmptyName(firstName)) {
            allNames.addAll(Arrays.asList(firstName.trim().split("\\s+")));
        }
        if(!isEmptyName(lastName)) {
            allNames.addAll(Arrays.asList(lastName.trim().split("\\s+")));
        }
        return allNames;
    }
}
