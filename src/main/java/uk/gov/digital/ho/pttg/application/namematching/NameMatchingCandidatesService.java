package uk.gov.digital.ho.pttg.application.namematching;

import org.springframework.stereotype.Service;
import uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator;

import java.util.Collection;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingCandidateServiceFunctions.deduplicate;

@Service
public class NameMatchingCandidatesService {

    private List<NameMatchingCandidateGenerator> candidateStrategies;

    public NameMatchingCandidatesService(List<NameMatchingCandidateGenerator> candidateStrategies) {
        this.candidateStrategies = candidateStrategies;
    }

    public List<CandidateName> generateCandidateNames(String firstNames, String lastNames, String aliasSurnames) {

        InputNames inputNames = new InputNames(firstNames, lastNames, aliasSurnames);

        List<CandidateName> candidates = candidateStrategies
                                                 .stream()
                                                 .map(cs -> cs.generateCandidates(inputNames))
                                                 .flatMap(Collection::stream)
                                                 .collect(toList());

        return unmodifiableList(deduplicate(candidates));
    }

}
