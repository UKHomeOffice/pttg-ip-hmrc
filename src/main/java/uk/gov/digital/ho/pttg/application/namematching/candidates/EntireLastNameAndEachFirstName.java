package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.*;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.DerivationAction.ENTIRE;
import static uk.gov.digital.ho.pttg.application.namematching.DerivationAction.ORIGINAL;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.LAST;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME_GENERATOR_PRIORITY;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator.ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME;

@Component
@Order(value = ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME_GENERATOR_PRIORITY)
public class EntireLastNameAndEachFirstName implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {

        String entireLastName = inputNames.fullLastName();

        return inputNames.firstNames().stream()
                .map(firstName -> combine(inputNames, firstName, entireLastName))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    private CandidateName combine(InputNames inputNames, Name firstName, String entireLastName) {

        CandidateDerivation derivation =
                new CandidateDerivation(
                        inputNames,
                        singletonList(ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME),
                        new Derivation(firstName, ORIGINAL, inputNames.splittersRemoved(), inputNames.splittersReplaced()),
                        new Derivation(LAST, null, null, inputNames.splittersRemoved(), inputNames.splittersReplaced(), singletonList(ENTIRE))
                        );

        return new CandidateName(firstName.name(), entireLastName, derivation);
    }
}
