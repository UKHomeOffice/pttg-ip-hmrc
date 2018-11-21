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
import static uk.gov.digital.ho.pttg.application.namematching.candidates.SpecialCharactersFunctions.namesAreEmpty;

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
    public List<CandidateName> generateCandidates(InputNames originalNames, InputNames namesToProcess) {

        //Set<CandidateName> candidateNames = new LinkedHashSet<>(); // TODO: why LinkedHashSet?

        if (!namesContainSplitters(originalNames)) {
            return emptyList();
        }

        return getNameCandidates(originalNames);
    }

    private List<CandidateName> getNameCandidates(InputNames inputNames) {

        List<CandidateName> candidateNames = new ArrayList<>();

        candidateNames.addAll(candidateNamesAfterSplittersIgnored(inputNames));
        candidateNames.addAll(candidateNamesAfterSplitting(inputNames));

        return candidateNames;
    }

    private List<CandidateName> candidateNamesAfterSplittersIgnored(InputNames inputNames) {

        InputNames inputNameSplittersRemoved = nameWithSplittersRemoved(inputNames);

        if (namesAreEmpty(inputNameSplittersRemoved)) {
            return emptyList();
        }

        return generators.stream()
                       .map(generator -> candidatesWithSplittersRemoved(generator, inputNames, inputNameSplittersRemoved))
                       .flatMap(Collection::stream)
                       .collect(toList());
    }

    private List<CandidateName> candidateNamesAfterSplitting(InputNames inputNames) {

        InputNames inputNameSpacesReplacingSplitters = nameWithSplittersReplacedBySpaces(inputNames);

        if (namesAreEmpty(inputNameSpacesReplacingSplitters)) {
            return emptyList();
        }

        List<CandidateName> candidateNames = generators.stream()
                                                     .map(generator -> candidatesWithSpacesReplacingSplitters(generator, inputNames, inputNameSpacesReplacingSplitters))
                                                     .flatMap(Collection::stream)
                                                     .collect(toList());

        return candidateNames;
    }

    private List<CandidateName> candidatesWithSpacesReplacingSplitters(NameMatchingCandidateGenerator candidateGenerator, InputNames inputNames, InputNames inputNameSpacesNotSplitters) {
        List<CandidateName> candidateNames = candidateGenerator.generateCandidates(inputNames, inputNameSpacesNotSplitters);

        candidateNames.forEach(candidateName -> candidateName.derivation().addGenerator(SPLITTERS_REPLACED));

        return candidateNames;
    }

    private List<CandidateName> candidatesWithSplittersRemoved(NameMatchingCandidateGenerator candidateGenerator, InputNames inputNames, InputNames inputNameSplittersRemoved) {
        List<CandidateName> candidateNames = candidateGenerator.generateCandidates(inputNames, inputNameSplittersRemoved);

        candidateNames.forEach(candidateName -> candidateName.derivation().addGenerator(SPLITTERS_REMOVED));

        return candidateNames;
    }

    private static boolean namesContainSplitters(InputNames inputNames) {
        return StringUtils.containsAny(inputNames.fullName(), NAME_SPLITTERS) || StringUtils.containsAny(inputNames.fullAliasNames(), NAME_SPLITTERS);
    }

    // TODO: Move to Functions class
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

    // TODO: Move to Functions class
    public static String nameWithSplittersReplacedBySpaces(String name) {
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
