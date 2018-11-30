package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.*;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.DerivationAction.ENTIRE;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.LAST;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.GeneratorFunctions.nameIndexes;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator.ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME;

@Component
public class EntireLastNameAndEachFirstName implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(InputNames originalNames, InputNames namesToProcess) {

        String entireLastName = namesToProcess.fullLastName();

        return namesToProcess.firstNames().stream()
                .map(firstName -> combine(originalNames, firstName, entireLastName))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    private CandidateName combine(InputNames originalNames, Name firstName, String entireLastName) {

        CandidateDerivation candidateDerivation =
                new CandidateDerivation(
                        originalNames,
                        singletonList(ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME),
                        new NameDerivation(firstName),
                        new NameDerivation(LAST, nameIndexes(originalNames.lastNames().size()), entireLastName.length(), singletonList(ENTIRE))
                        );

        return new CandidateName(firstName.name(), entireLastName, candidateDerivation);
    }

}
