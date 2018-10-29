package uk.gov.digital.ho.pttg.application.namematching.candidates;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class SpecialCharacters implements NameMatchingCandidateGenerator {
    private static final String NAME_SPLITTERS = "-'.";
    private static final String NAME_SPLITTER_REGEX = "[" + NAME_SPLITTERS + "]";

    private NameCombinations nameCombinations;
    private MultipleLastNames multipleLastNames;

    public SpecialCharacters(NameCombinations nameCombinations, MultipleLastNames multipleLastNames) {
        this.nameCombinations = nameCombinations;
        this.multipleLastNames = multipleLastNames;
    }

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {
        Set<CandidateName> candidateNames = new LinkedHashSet<>();

        if (!namesContainSplitters(inputNames)) {
            return Lists.newArrayList(candidateNames);
        }

        InputNames inputNameWithoutSplitters = nameWithSplittersRemoved(inputNames);
        InputNames inputNameWithSpaces = nameWithSplittersReplacedBySpaces(inputNames);

        if (namesAreNotEmpty(inputNameWithoutSplitters)) {
            candidateNames.addAll(multipleLastNames.generateCandidates(inputNameWithoutSplitters));
        }
        if (namesAreNotEmpty(inputNameWithSpaces)) {
            candidateNames.addAll(multipleLastNames.generateCandidates(inputNameWithSpaces));
        }

        if (namesAreNotEmpty(inputNameWithoutSplitters)) {
            candidateNames.addAll(nameCombinations.generateCandidates(inputNameWithoutSplitters));
        }
        if (namesAreNotEmpty(inputNameWithSpaces)) {

            candidateNames.addAll(nameCombinations.generateCandidates(inputNameWithSpaces));
        }
        return Lists.newArrayList(candidateNames);
    }

    private static boolean namesContainSplitters(InputNames inputNames) {
        return StringUtils.containsAny(inputNames.fullName(), NAME_SPLITTERS);
    }

    private static String nameWithSplittersRemoved(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, "");
    }

    private static InputNames nameWithSplittersRemoved(InputNames inputNames) {
        return new InputNames(nameWithSplittersRemoved(inputNames.fullFirstName()), nameWithSplittersRemoved(inputNames.fullLastName()));
    }

    private static String nameWithSplittersReplacedBySpaces(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, " ");
    }

    private static InputNames nameWithSplittersReplacedBySpaces(InputNames inputNames) {
        return new InputNames(nameWithSplittersReplacedBySpaces(inputNames.fullFirstName()), nameWithSplittersReplacedBySpaces(inputNames.fullLastName()));
    }

    private boolean namesAreNotEmpty(InputNames inputNames) {
        return !(inputNames.firstNames().isEmpty() && inputNames.lastNames().isEmpty());
    }

}
