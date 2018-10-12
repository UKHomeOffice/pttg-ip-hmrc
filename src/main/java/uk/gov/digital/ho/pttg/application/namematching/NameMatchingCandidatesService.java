package uk.gov.digital.ho.pttg.application.namematching;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.pttg.application.namematching.candidates.MultipleLastNames;
import uk.gov.digital.ho.pttg.application.namematching.candidates.NameCombinations;

import java.util.*;

@Service
public class NameMatchingCandidatesService {
    private static final String NAME_SPLITTERS = "-'";
    private static final String NAME_SPLITTER_REGEX = "[" + NAME_SPLITTERS + "]";
    private static final Integer MAX_NAMES = 7;

    private NameCombinations nameCombinations;
    private MultipleLastNames multipleLastNames;

    public NameMatchingCandidatesService(NameCombinations nameCombinations, MultipleLastNames multipleLastNames) {
        this.nameCombinations = nameCombinations;
        this.multipleLastNames = multipleLastNames;
    }

    public List<PersonName> generateCandidateNames(String firstName, String lastName) {

        List<PersonName> candidates = new ArrayList<>();

        candidates.addAll(multipleLastNames.generateCandidates(firstName, lastName));
        candidates.addAll(nameCombinations.generateCandidates(firstName, lastName));

        if (namesContainSplitters(firstName, lastName)) {
            candidates.addAll(generateCandidatesWithSplitters(firstName, lastName));
        }

        return Collections.unmodifiableList(candidates);
    }

    private static boolean namesContainSplitters(String firstName, String lastName) {
        return StringUtils.containsAny(firstName, NAME_SPLITTERS) || StringUtils.containsAny(lastName, NAME_SPLITTERS);
    }

    private List<PersonName> generateCandidatesWithSplitters(String firstName, String lastName) {
        Set<PersonName> candidateNames = new LinkedHashSet<>();

        candidateNames.addAll(multipleLastNames.generateCandidates(nameWithSplittersRemoved(firstName), nameWithSplittersRemoved(lastName)));
        candidateNames.addAll(multipleLastNames.generateCandidates(nameWithSplittersReplacedBySpaces(firstName), nameWithSplittersReplacedBySpaces(lastName)));
        candidateNames.addAll(nameCombinations.generateCandidates(nameWithSplittersRemoved(firstName), nameWithSplittersRemoved(lastName)));
        candidateNames.addAll(nameCombinations.generateCandidates(nameWithSplittersReplacedBySpaces(firstName), nameWithSplittersReplacedBySpaces(lastName)));

        return Lists.newArrayList(candidateNames);
    }

    private static String nameWithSplittersRemoved(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, "");
    }

    private static String nameWithSplittersReplacedBySpaces(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, " ");
    }


}
