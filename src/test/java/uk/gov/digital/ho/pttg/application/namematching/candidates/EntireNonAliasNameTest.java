package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import static org.assertj.core.api.Assertions.assertThat;

public class EntireNonAliasNameTest {

    private EntireNonAliasName entireNonAliasName = new EntireNonAliasName();

    @Test
    public void shouldReturnEntireNonAliasNameAsOnlyCandidate() {
        InputNames inputNames = new InputNames("some first name", "some last name", "some alias");

        CandidateName expectedCandidateName = new CandidateName("some first name", "some last name");
        assertThat(entireNonAliasName.generateCandidates(inputNames)).hasSize(1);
        assertThat(entireNonAliasName.generateCandidates(inputNames).get(0)).isEqualTo(expectedCandidateName);
    }

}