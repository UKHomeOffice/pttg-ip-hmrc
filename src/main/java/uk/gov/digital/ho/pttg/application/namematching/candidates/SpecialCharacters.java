package uk.gov.digital.ho.pttg.application.namematching.candidates;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.SPECIAL_CHARACTERS_STRATEGY_PRIORITY;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.SpecialCharactersFunctions.namesAreNotEmpty;

@Component
@Order(value = SPECIAL_CHARACTERS_STRATEGY_PRIORITY)
public class SpecialCharacters implements NameMatchingCandidateGenerator {
    private static final String NAME_SPLITTERS = "-'.";
    private static final String NAME_SPLITTER_REGEX = "[" + NAME_SPLITTERS + "]";

    private EntireNonAliasName entireNonAliasName;
    private EntireLastNameAndEachFirstName entireLastNameAndEachFirstName;
    private NameCombinations nameCombinations;
    private AliasCombinations aliasCombinations;
    private MultipleLastNames multipleLastNames;

    public SpecialCharacters(EntireNonAliasName entireNonAliasName, EntireLastNameAndEachFirstName entireLastNameAndEachFirstName, NameCombinations nameCombinations, AliasCombinations aliasCombinations, MultipleLastNames multipleLastNames) {
        this.entireNonAliasName = entireNonAliasName;
        this.entireLastNameAndEachFirstName = entireLastNameAndEachFirstName;
        this.nameCombinations = nameCombinations;
        this.aliasCombinations = aliasCombinations;
        this.multipleLastNames = multipleLastNames;
    }

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {
        Set<CandidateName> candidateNames = new LinkedHashSet<>();

        if (!namesContainSplitters(inputNames)) {
            return Lists.newArrayList(candidateNames);
        }

        candidateNames.addAll(getNameCandidates(inputNames, entireNonAliasName));
        candidateNames.addAll(getNameCandidates(inputNames, entireLastNameAndEachFirstName));
        candidateNames.addAll(getNameCandidates(inputNames, multipleLastNames));

        if (inputNames.hasAliasSurnames()) {
            candidateNames.addAll(getNameCandidates(inputNames, aliasCombinations));
        } else {
            candidateNames.addAll(getNameCandidates(inputNames, nameCombinations));
        }

        return Lists.newArrayList(candidateNames);
    }

    private List<CandidateName> getNameCandidates(InputNames inputNames, NameMatchingCandidateGenerator candidateGenerator) {
        List<CandidateName> candidateNames = new ArrayList<>();

        InputNames inputNameSplittersRemoved = nameWithSplittersRemoved(inputNames);
        InputNames inputNameSpacesNotSplitters = nameWithSplittersReplacedBySpaces(inputNames);

        if (namesAreNotEmpty(inputNameSplittersRemoved)) {
            candidateNames.addAll(candidateGenerator.generateCandidates(inputNameSplittersRemoved));
        }
        if (namesAreNotEmpty(inputNameSpacesNotSplitters)) {
            candidateNames.addAll(candidateGenerator.generateCandidates(inputNameSpacesNotSplitters));
        }
        return candidateNames;
    }

    private static boolean namesContainSplitters(InputNames inputNames) {
        return StringUtils.containsAny(inputNames.fullName(), NAME_SPLITTERS) || StringUtils.containsAny(inputNames.allAliasSurnamesAsString(), NAME_SPLITTERS);
    }

    private static String nameWithSplittersRemoved(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, "");
    }

    private static InputNames nameWithSplittersRemoved(InputNames inputNames) {
        return new InputNames(nameWithSplittersRemoved(inputNames.fullFirstName()), nameWithSplittersRemoved(inputNames.fullLastName()), nameWithSplittersRemoved(inputNames.allAliasSurnamesAsString()));
    }

    private static String nameWithSplittersReplacedBySpaces(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, " ");
    }

    private static InputNames nameWithSplittersReplacedBySpaces(InputNames inputNames) {
        String aliasSurnames = inputNames.allAliasSurnamesAsString();
        return new InputNames(nameWithSplittersReplacedBySpaces(inputNames.fullFirstName()), nameWithSplittersReplacedBySpaces(inputNames.fullLastName()), nameWithSplittersReplacedBySpaces(aliasSurnames));
    }
}
