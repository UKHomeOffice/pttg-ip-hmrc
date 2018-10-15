package uk.gov.digital.ho.pttg.application.namematching.candidates;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.PersonName;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class SpecialCharacters implements NameMatchingCandidateGenerator {
    private static final String NAME_SPLITTERS = "-'";
    private static final String NAME_SPLITTER_REGEX = "[" + NAME_SPLITTERS + "]";

    private NameCombinations nameCombinations;
    private MultipleLastNames multipleLastNames;

    public SpecialCharacters(NameCombinations nameCombinations, MultipleLastNames multipleLastNames) {
        this.nameCombinations = nameCombinations;
        this.multipleLastNames = multipleLastNames;
    }

    @Override
    public List<PersonName> generateCandidates(String firstName, String lastName) {
        Set<PersonName> candidateNames = new LinkedHashSet<>();

        if (!namesContainSplitters(firstName, lastName)) {
            return Lists.newArrayList(candidateNames);
        }

        candidateNames.addAll(multipleLastNames.generateCandidates(nameWithSplittersRemoved(firstName), nameWithSplittersRemoved(lastName)));
        candidateNames.addAll(multipleLastNames.generateCandidates(nameWithSplittersReplacedBySpaces(firstName), nameWithSplittersReplacedBySpaces(lastName)));
        candidateNames.addAll(nameCombinations.generateCandidates(nameWithSplittersRemoved(firstName), nameWithSplittersRemoved(lastName)));
        candidateNames.addAll(nameCombinations.generateCandidates(nameWithSplittersReplacedBySpaces(firstName), nameWithSplittersReplacedBySpaces(lastName)));

        return Lists.newArrayList(candidateNames);
    }

    private static boolean namesContainSplitters(String firstName, String lastName) {
        return StringUtils.containsAny(firstName, NAME_SPLITTERS) || StringUtils.containsAny(lastName, NAME_SPLITTERS);
    }

    private static String nameWithSplittersRemoved(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, "");
    }

    private static String nameWithSplittersReplacedBySpaces(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, " ");
    }
}
