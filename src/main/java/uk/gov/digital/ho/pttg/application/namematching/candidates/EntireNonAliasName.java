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
import static uk.gov.digital.ho.pttg.application.namematching.candidates.GeneratorFunctions.nameIndexes;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator.ENTIRE_NON_ALIAS_NAME;

@Component
public class EntireNonAliasName implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(InputNames originalNames, InputNames namesToProcess) {

        String firstName = namesToProcess.fullFirstName();
        String lastName = namesToProcess.fullLastName();

        NameDerivation firstNameDerivation;
        NameDerivation lastNameDerivation;

        if (StringUtils.isBlank(firstName)) {
            firstName = lastName;
            firstNameDerivation = new NameDerivation(LAST, nameIndexes(originalNames.lastNames().size()), firstName.length(), singletonList(ENTIRE));
        } else {
            firstNameDerivation = new NameDerivation(FIRST, nameIndexes(originalNames.firstNames().size()), firstName.length(), singletonList(ENTIRE));
        }

        if (StringUtils.isBlank(lastName)) {
            lastName = firstName;
            lastNameDerivation = new NameDerivation(FIRST, nameIndexes(originalNames.firstNames().size()), lastName.length(), singletonList(ENTIRE));
        } else {
            lastNameDerivation = new NameDerivation(LAST, nameIndexes(originalNames.lastNames().size()), lastName.length(), singletonList(ENTIRE));
        }

        return singletonList(
                new CandidateName(
                        firstName,
                        lastName,
                        new CandidateDerivation(
                                originalNames,
                                singletonList(ENTIRE_NON_ALIAS_NAME),
                                firstNameDerivation,
                                lastNameDerivation)));
    }

}
