package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateDerivation;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.Derivation;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Collections.singletonList;
import static uk.gov.digital.ho.pttg.application.namematching.DerivationAction.ENTIRE;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.FIRST;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.LAST;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.ENTIRE_NON_ALIAS_NAME_GENERATOR_PRIORITY;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator.ENTIRE_NON_ALIAS_NAME;

@Component
@Order(value = ENTIRE_NON_ALIAS_NAME_GENERATOR_PRIORITY)
public class EntireNonAliasName implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {

        String firstName = inputNames.fullFirstName();
        String lastName = inputNames.fullLastName();

        Derivation firstNameDerivation;
        Derivation lastNameDerivation;

        if (StringUtils.isBlank(firstName)) {
            firstName = lastName;
            firstNameDerivation = new Derivation(LAST, null, null, inputNames.splittersRemoved(), inputNames.splittersReplaced(), singletonList(ENTIRE));
        } else {
            firstNameDerivation = new Derivation(FIRST, null, null, inputNames.splittersRemoved(), inputNames.splittersReplaced(), singletonList(ENTIRE));
        }

        if (StringUtils.isBlank(lastName)) {
            lastName = firstName;
            lastNameDerivation = new Derivation(FIRST, null, null, inputNames.splittersRemoved(), inputNames.splittersReplaced(), singletonList(ENTIRE));
        } else {
            lastNameDerivation = new Derivation(LAST, null, null, inputNames.splittersRemoved(), inputNames.splittersReplaced(), singletonList(ENTIRE));
        }

        return singletonList(
                new CandidateName(
                        firstName,
                        lastName,
                        new CandidateDerivation(
                                inputNames,
                                singletonList(ENTIRE_NON_ALIAS_NAME),
                                firstNameDerivation,
                                lastNameDerivation)));
    }

}
