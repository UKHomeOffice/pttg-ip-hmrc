package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator.SPLITTERS_REMOVED;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator.SPLITTERS_REPLACED;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.SpecialCharactersFunctions.namesAreNotEmpty;

@Component
public class SpecialCharacters implements NameMatchingCandidateGenerator {

    private static final String NAME_SPLITTERS = "-'.";
    private static final String NAME_SPLITTER_REGEX = "[" + NAME_SPLITTERS + "]";

    private List<NameMatchingCandidateGenerator> generators;

    public SpecialCharacters(EntireNonAliasName entireNonAliasName,
                             EntireLastNameAndEachFirstName entireLastNameAndEachFirstName,
                             NameCombinations nameCombinations,
                             AliasCombinations aliasCombinations,
                             MultipleLastNames multipleLastNames) {
        this.generators = asList(
                entireNonAliasName,
                entireLastNameAndEachFirstName,
                nameCombinations,
                aliasCombinations,
                multipleLastNames);
    }

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {

        //Set<CandidateName> candidateNames = new LinkedHashSet<>(); // TODO: why LinkedHashSet?

        if (!namesContainSplitters(inputNames)) {
            return emptyList();
        }

        return getNameCandidates(inputNames);
    }

    private List<CandidateName> getNameCandidates(InputNames inputNames) {

        List<CandidateName> candidateNames = new ArrayList<>();

        InputNames inputNameSplittersRemoved = nameWithSplittersRemoved(inputNames);
        InputNames inputNameSpacesReplacingSplitters = nameWithSplittersReplacedBySpaces(inputNames);

        if (namesAreNotEmpty(inputNameSplittersRemoved)) {

            candidateNames.addAll(
                    generators.stream()
                            .map(generator -> candidatesWithSplittersRemoved(generator, inputNameSplittersRemoved))
                            .flatMap(Collection::stream)
                            .collect(toList()));
        }

        if (namesAreNotEmpty(inputNameSpacesReplacingSplitters)) {

            candidateNames.addAll(
                    generators.stream()
                            .map(generator -> candidatesWithSpacesReplacingSplitters(generator, inputNameSpacesReplacingSplitters))
                            .flatMap(Collection::stream)
                            .collect(toList()));
        }

        return candidateNames;
    }

    private List<CandidateName> candidatesWithSpacesReplacingSplitters(NameMatchingCandidateGenerator candidateGenerator, InputNames inputNameSpacesNotSplitters) {
        List<CandidateName> candidateNames = candidateGenerator.generateCandidates(inputNameSpacesNotSplitters);

        candidateNames.forEach(candidateName -> candidateName.derivation().addGenerator(SPLITTERS_REPLACED));

        return candidateNames;
    }

    private List<CandidateName> candidatesWithSplittersRemoved(NameMatchingCandidateGenerator candidateGenerator, InputNames inputNameSplittersRemoved) {
        List<CandidateName> candidateNames = candidateGenerator.generateCandidates(inputNameSplittersRemoved);

        candidateNames.forEach(candidateName -> candidateName.derivation().addGenerator(SPLITTERS_REMOVED));

        return candidateNames;
    }

    private static boolean namesContainSplitters(InputNames inputNames) {
        return StringUtils.containsAny(inputNames.fullName(), NAME_SPLITTERS) || StringUtils.containsAny(inputNames.fullAliasNames(), NAME_SPLITTERS);
    }

    private static String nameWithSplittersRemoved(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, "");
    }

    private static InputNames nameWithSplittersRemoved(InputNames inputNames) {

        InputNames modifiedInputNames = new InputNames(
                nameWithSplittersRemoved(inputNames.fullFirstName()),
                nameWithSplittersRemoved(inputNames.fullLastName()),
                nameWithSplittersRemoved(inputNames.fullAliasNames()));

        modifiedInputNames.splittersRemoved(true);

        return modifiedInputNames;
    }

    private static String nameWithSplittersReplacedBySpaces(String name) {
        return name.replaceAll(NAME_SPLITTER_REGEX, " ");
    }

    private static InputNames nameWithSplittersReplacedBySpaces(InputNames inputNames) {

        InputNames modifiedInputNames = new InputNames(
                nameWithSplittersReplacedBySpaces(inputNames.fullFirstName()),
                nameWithSplittersReplacedBySpaces(inputNames.fullLastName()),
                nameWithSplittersReplacedBySpaces(inputNames.fullAliasNames()));

        modifiedInputNames.splittersReplaced(true);

        return modifiedInputNames;
    }
}
