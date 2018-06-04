package uk.gov.digital.ho.pttg.application.retry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class NameMatchingCandidatesGenerator {

    private static final String NAME_SPLITTERS = "-'";

    // This maps the number of names to a list containing pairs of candidate names in the order defined by the requirements
    private static final Map<Integer, List<NamePairRule>> CANDIDATE_NAME_RULES = ImmutableMap.of(
            1, ImmutableList.of(
                    NamePairRule.create(0, 0)),
            2, ImmutableList.of(
                    NamePairRule.create(0, 1), NamePairRule.create(1, 0)),
            3, ImmutableList.of(
                    NamePairRule.create(0, 2), NamePairRule.create(1, 2),
                    NamePairRule.create(2, 0), NamePairRule.create(2, 1),
                    NamePairRule.create(0, 1), NamePairRule.create(1, 0)),
            4, ImmutableList.of(
                    NamePairRule.create(0, 3), NamePairRule.create(1, 3), NamePairRule.create(2, 3),
                    NamePairRule.create(0, 1), NamePairRule.create(0, 2), NamePairRule.create(1, 0),
                    NamePairRule.create(2, 0), NamePairRule.create(3, 0), NamePairRule.create(1, 2),
                    NamePairRule.create(2, 1), NamePairRule.create(3, 1), NamePairRule.create(3, 2)),
            5, ImmutableList.of(
                    NamePairRule.create(0, 4), NamePairRule.create(1, 4), NamePairRule.create(2, 4), NamePairRule.create(3, 4),
                    NamePairRule.create(0, 1), NamePairRule.create(0, 2), NamePairRule.create(0, 3), NamePairRule.create(1, 0),
                    NamePairRule.create(1, 2), NamePairRule.create(1, 3), NamePairRule.create(2, 0), NamePairRule.create(2, 1),
                    NamePairRule.create(2, 3), NamePairRule.create(3, 0), NamePairRule.create(3, 1), NamePairRule.create(3, 2),
                    NamePairRule.create(4, 0), NamePairRule.create(4, 1), NamePairRule.create(4, 2), NamePairRule.create(4, 3))
    );


    public static List<String> generateCandidateNames(String firstName, String lastName) {
        validateName(firstName, lastName);

        List<String> candidates = new ArrayList<>();

        if(namesContainSplitters(firstName, lastName)) {
            generateCandidatesWithSplitters(candidates, firstName, lastName);
        }
        else {
            generateCandidates(candidates, firstName, lastName);
        }

        return candidates;
    }

    private static boolean namesContainSplitters(String firstName, String lastName) {
        long firstNameSplitters = Arrays.stream(NAME_SPLITTERS.split("")).filter(c -> firstName.contains(c)).count();
        long lastNameSplitters = Arrays.stream(NAME_SPLITTERS.split("")).filter(c -> lastName.contains(c)).count();
        return firstNameSplitters > 0 || lastNameSplitters > 0;
    }

    private static void generateCandidatesWithSplitters(List<String> candidates, String firstName, String lastName) {
        final String NAME_SPLITTER_REGEX = "[" + NAME_SPLITTERS + "]";
        generateCandidates(candidates, firstName.replaceAll(NAME_SPLITTER_REGEX, ""), lastName.replaceAll(NAME_SPLITTER_REGEX, ""));
        generateCandidates(candidates, firstName.replaceAll(NAME_SPLITTER_REGEX, " "), lastName.replaceAll(NAME_SPLITTER_REGEX, " "));
    }

    private static void generateCandidates(List<String> candidates, String firstName, String lastName) {
        List<String> allNames = findAllNames(firstName, lastName);
        candidates.addAll(
                CANDIDATE_NAME_RULES
                        .get(allNames.size())
                        .stream()
                        .map(namePairRule -> namePairRule.calculateName(allNames))
                        .collect(toList())
        );
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

    private static final class NamePairRule {
        private final int firstNameIndex;
        private final int lastNameIndex;

        private NamePairRule(int firstNameIndex, int lastNameIndex) {
            this.firstNameIndex = firstNameIndex;
            this.lastNameIndex = lastNameIndex;
        }

        protected static NamePairRule create(int firstNameIndex, int lastNameIndex) {
            return new NamePairRule(firstNameIndex, lastNameIndex);
        }

        protected String calculateName(List<String> names) {
            if(firstNameIndex > names.size()-1) {
                throw new IllegalArgumentException(String.format("Cannot retrieve name in position %d from names %s", firstNameIndex, names.toString()));
            }
            if(lastNameIndex > names.size()-1) {
                throw new IllegalArgumentException(String.format("Cannot retrieve name in position %d from names %s", lastNameIndex, names.toString()));
            }
            return names.get(firstNameIndex) + " " + names.get(lastNameIndex);
        }
    }
}
