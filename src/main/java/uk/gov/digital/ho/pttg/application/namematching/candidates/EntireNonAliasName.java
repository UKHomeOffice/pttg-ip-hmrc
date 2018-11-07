package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.ArrayList;
import java.util.List;

@Component
public class EntireNonAliasName implements NameMatchingCandidateGenerator {

    @Override
    public List<CandidateName> generateCandidates(InputNames inputNames) {
        List<CandidateName> candidateNames = new ArrayList<>();

        String firstName = substituteIfBlank(inputNames.fullFirstName(), inputNames.fullLastName());
        String lastName = substituteIfBlank(inputNames.fullLastName(), inputNames.fullFirstName());

        candidateNames.add(new CandidateName(firstName, lastName));
        return candidateNames;
    }

    private String substituteIfBlank(String initial, String substitution) {
        return StringUtils.isBlank(initial) ? substitution : initial;
    }
}
