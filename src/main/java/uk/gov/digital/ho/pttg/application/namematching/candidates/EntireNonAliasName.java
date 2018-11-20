package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateDerivation;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;
import uk.gov.digital.ho.pttg.application.namematching.NameDerivation;

import java.util.List;

import static java.util.Collections.singletonList;
import static uk.gov.digital.ho.pttg.application.namematching.DerivationAction.ENTIRE;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.FIRST;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.LAST;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator.ENTIRE_NON_ALIAS_NAME;

@Component
public class EntireNonAliasName implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {

        String firstName = inputNames.fullFirstName();
        String lastName = inputNames.fullLastName();

        NameDerivation firstNameDerivation;
        NameDerivation lastNameDerivation;

        if (StringUtils.isBlank(firstName)) {
            firstName = lastName;
            firstNameDerivation = new NameDerivation(LAST, null, firstName.length(), inputNames.splittersRemoved(), inputNames.splittersReplaced(), singletonList(ENTIRE));
        } else {
            firstNameDerivation = new NameDerivation(FIRST, null, firstName.length(), inputNames.splittersRemoved(), inputNames.splittersReplaced(), singletonList(ENTIRE));
        }

        if (StringUtils.isBlank(lastName)) {
            lastName = firstName;
            lastNameDerivation = new NameDerivation(FIRST, null, lastName.length(), inputNames.splittersRemoved(), inputNames.splittersReplaced(), singletonList(ENTIRE));
        } else {
            lastNameDerivation = new NameDerivation(LAST, null, lastName.length(), inputNames.splittersRemoved(), inputNames.splittersReplaced(), singletonList(ENTIRE));
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
