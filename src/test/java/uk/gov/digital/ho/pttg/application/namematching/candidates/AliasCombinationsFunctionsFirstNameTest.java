package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AliasCombinationsFunctions.firstNameCombinations;

public class AliasCombinationsFunctionsFirstNameTest {

    @Test
    public void shouldReturnEmptyListForEmptyListInput() {
        assertThat(firstNameCombinations(emptyList())).isEqualTo(emptyList());
    }

    @Test
    public void shouldReturnEmptyListForOneName() {
        assertThat(firstNameCombinations(singletonList("John"))).isEqualTo(emptyList());
    }

    @Test
    public void shouldReturnCombinationsForTwoNames() {
        List<String> firstNames = asList("John", "David");
        List<CandidateName> expected = asList(
                new CandidateName("John", "David"),
                new CandidateName("David", "John")
        );

        assertThat(firstNameCombinations(firstNames)).isEqualTo(expected);
    }

    @Test
    public void shouldReturnCombinationsForThreeNames() {
        List<String> firstNames = asList("John", "David", "Greg");
        List<CandidateName> expected = asList(
                new CandidateName("John", "David"),
                new CandidateName("John", "Greg"),
                new CandidateName("David", "John"),
                new CandidateName("David", "Greg"),
                new CandidateName("Greg", "John"),
                new CandidateName("Greg", "David")
        );

        assertThat(firstNameCombinations(firstNames)).isEqualTo(expected);
    }
}
