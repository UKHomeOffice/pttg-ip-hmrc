package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AliasCombinationsFunctions.firstNameCombinations;

public class AliasCombinationsFunctionsFirstNameTest {

    @Test
    public void shouldReturnEmptyListForEmptyFirstName() {
        InputNames emptyFirstName = new InputNames("", "somename", "somename");
        assertThat(firstNameCombinations(emptyFirstName, emptyFirstName)).isEqualTo(emptyList());
    }

    @Test
    public void shouldReturnEmptyListForOneName() {
        InputNames oneFirstName = new InputNames("John", "somename", "somename");

        assertThat(firstNameCombinations(oneFirstName, oneFirstName)).isEqualTo(emptyList());
    }

    @Test
    public void shouldReturnCombinationsForTwoNames() {
        InputNames inputNames = new InputNames("John David", "somename", "somename");

        List<CandidateName> expected = asList(
                new CandidateName("John", "David"),
                new CandidateName("David", "John")
        );

        assertThat(firstNameCombinations(inputNames, inputNames)).isEqualTo(expected);
    }

    @Test
    public void shouldReturnCombinationsForThreeNames() {
        InputNames inputNames = new InputNames("John David Greg", "somename", "somename");

        List<CandidateName> expected = asList(
                new CandidateName("John", "David"),
                new CandidateName("John", "Greg"),
                new CandidateName("David", "John"),
                new CandidateName("David", "Greg"),
                new CandidateName("Greg", "John"),
                new CandidateName("Greg", "David")
        );

        assertThat(firstNameCombinations(inputNames, inputNames)).isEqualTo(expected);
    }
}
