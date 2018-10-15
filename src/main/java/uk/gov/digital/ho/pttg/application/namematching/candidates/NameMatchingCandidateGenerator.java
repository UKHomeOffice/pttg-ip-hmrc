package uk.gov.digital.ho.pttg.application.namematching.candidates;

import uk.gov.digital.ho.pttg.application.namematching.CandidateName;

import java.util.List;

public interface NameMatchingCandidateGenerator {

    List<CandidateName> generateCandidates(String firstName, String lastName);

}
