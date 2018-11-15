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
import static uk.gov.digital.ho.pttg.application.namematching.Derivation.ALL_FIRST_NAMES;
import static uk.gov.digital.ho.pttg.application.namematching.Derivation.ALL_LAST_NAMES;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.ENTIRE_NON_ALIAS_NAME_STRATEGY_PRIORITY;

@Component
@Order(value = ENTIRE_NON_ALIAS_NAME_STRATEGY_PRIORITY)
public class EntireNonAliasName implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {

        String firstName = inputNames.fullFirstName();
        String lastName = inputNames.fullLastName();

        Derivation firstNameDerivation;
        Derivation lastNameDerivation;

        if (StringUtils.isBlank(firstName)) {
            firstName = lastName;
            firstNameDerivation = ALL_LAST_NAMES;
        } else {
            firstNameDerivation = ALL_FIRST_NAMES;
        }

        if (StringUtils.isBlank(lastName)) {
            lastName = firstName;
            lastNameDerivation = ALL_FIRST_NAMES;
        } else {
            lastNameDerivation = ALL_LAST_NAMES;
        }

        return singletonList(
                new CandidateName(
                        firstName,
                        lastName,
                        new CandidateDerivation(
                                inputNames,
                                singletonList(ENTIRE_NON_ALIAS_NAME_STRATEGY_PRIORITY),
                                firstNameDerivation,
                                lastNameDerivation)));
    }

}
