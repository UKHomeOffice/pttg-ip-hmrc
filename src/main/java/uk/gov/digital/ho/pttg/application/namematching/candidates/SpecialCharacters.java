package uk.gov.digital.ho.pttg.application.namematching.candidates;

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

        if (!SpecialCharactersFunctions.namesContainSplitters(namesToProcess)) {
            return emptyList();
        }

        return getNameCandidates(originalNames, namesToProcess);
    }

    private List<CandidateName> getNameCandidates(InputNames originalNames, InputNames namesToProcess) {

        List<CandidateName> candidateNames = new ArrayList<>();

        candidateNames.addAll(candidateNamesAfterSplittersIgnored(originalNames, namesToProcess));
        candidateNames.addAll(candidateNamesAfterSplitting(originalNames, namesToProcess));

        return candidateNames;
    }

    private List<CandidateName> candidateNamesAfterSplittersIgnored(InputNames originalNames, InputNames namesToProcess) {

        InputNames inputNameSplittersRemoved = SpecialCharactersFunctions.nameWithSplittersRemoved(namesToProcess);

        if (namesAreEmpty(inputNameSplittersRemoved)) {
            return emptyList();
        }

        return generators.stream()
                       .map(generator -> candidatesWithSplittersRemoved(generator, originalNames, inputNameSplittersRemoved))
                       .flatMap(Collection::stream)
                       .collect(toList());
    }

    private List<CandidateName> candidateNamesAfterSplitting(InputNames originalNames, InputNames namesToProcess) {

        InputNames inputNameSpacesReplacingSplitters = SpecialCharactersFunctions.nameWithSplittersReplacedBySpaces(namesToProcess);

        if (namesAreEmpty(inputNameSpacesReplacingSplitters)) {
            return emptyList();
        }

        return generators.stream()
                       .map(generator -> candidatesWithSpacesReplacingSplitters(generator, originalNames, inputNameSpacesReplacingSplitters))
                       .flatMap(Collection::stream)
                       .collect(toList());
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

}
