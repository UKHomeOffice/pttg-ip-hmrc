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

        if (!SpecialCharactersFunctions.namesContainSplitters(originalNames)) {
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

        InputNames inputNameSplittersRemoved = SpecialCharactersFunctions.nameWithSplittersRemoved(inputNames);

        if (namesAreEmpty(inputNameSplittersRemoved)) {
            return emptyList();
        }

        return generators.stream()
                       .map(generator -> candidatesWithSplittersRemoved(generator, inputNames, inputNameSplittersRemoved))
                       .flatMap(Collection::stream)
                       .collect(toList());
    }

    private List<CandidateName> candidateNamesAfterSplitting(InputNames inputNames) {

        InputNames inputNameSpacesReplacingSplitters = SpecialCharactersFunctions.nameWithSplittersReplacedBySpaces(inputNames);

        if (namesAreEmpty(inputNameSpacesReplacingSplitters)) {
            return emptyList();
        }

        return generators.stream()
                       .map(generator -> candidatesWithSpacesReplacingSplitters(generator, inputNames, inputNameSpacesReplacingSplitters))
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
