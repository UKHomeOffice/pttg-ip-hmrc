package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.PersonName;

import java.util.List;

public class BasicGenerator implements NameMatchingCandidateGenerator {

    @Override
    public List<PersonName> generateCandidates(String firstName, String lastName) {
        return null;
    }

    @Override
    public boolean appliesTo(String firstName, String lastName) {
        return true;
    }
}
