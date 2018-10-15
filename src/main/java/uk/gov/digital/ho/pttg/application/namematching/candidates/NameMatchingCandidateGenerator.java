package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.PersonName;

import java.util.List;

public interface NameMatchingCandidateGenerator {

    List<PersonName> generateCandidates(String firstName, String lastName);

}
