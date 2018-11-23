package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AliasCombinationsFunctions.nonAliasFirstNamesAndLastNameCombinations;

public class AliasCombinationsFunctionsNonAliasFirstLastTest {

    @Test
    public void shouldReturnEmptyListForEmptyInputs() {
        InputNames allEmpty = new InputNames("", "", "");
        InputNames emptyFirstName = new InputNames("", "somename", "somename");
        InputNames emptyLastName = new InputNames("somename", "", "somename");
        assertThat(nonAliasFirstNamesAndLastNameCombinations(allEmpty, allEmpty)).isEqualTo(emptyList());
        assertThat(nonAliasFirstNamesAndLastNameCombinations(emptyFirstName, emptyFirstName)).isEqualTo(emptyList());
        assertThat(nonAliasFirstNamesAndLastNameCombinations(emptyLastName, emptyLastName)).isEqualTo(emptyList());
    }

    @Test
    public void combinationsForOneFirstNameOneLastName() {
        InputNames inputNames = new InputNames("John", "Smith");

        List<CandidateName> expected = singletonList(new CandidateName("John", "Smith"));

        assertThat(nonAliasFirstNamesAndLastNameCombinations(inputNames, inputNames)).isEqualTo(expected);
    }

    @Test
    public void combinationsForTwoFirstNameOneLastName() {
        InputNames inputNames = new InputNames("John David", "Smith");
        List<CandidateName> expected = asList(
                new CandidateName("John", "Smith"),
                new CandidateName("David", "Smith")
        );

        assertThat(nonAliasFirstNamesAndLastNameCombinations(inputNames, inputNames)).isEqualTo(expected);
    }

    @Test
    public void combinationsForOneFirstNameTwoLastNames() {
        InputNames inputNames = new InputNames("John", "Smith Evans");

        List<CandidateName> expected = asList(
                new CandidateName("John", "Evans"),
                new CandidateName("Smith", "Evans"),
                new CandidateName("John", "Smith"),
                new CandidateName("Evans", "Smith")
        );

        assertThat(nonAliasFirstNamesAndLastNameCombinations(inputNames, inputNames)).isEqualTo(expected);
    }

    @Test
    public void combinationsForTwoFirstNamesTwoLastName() {
        InputNames inputNames = new InputNames("John David", "Smith Evans");

        List<CandidateName> expected = asList(
                new CandidateName("John", "Evans"),
                new CandidateName("David", "Evans"),
                new CandidateName("Smith", "Evans"),
                new CandidateName("John", "Smith"),
                new CandidateName("David", "Smith"),
                new CandidateName("Evans", "Smith")
        );

        assertThat(nonAliasFirstNamesAndLastNameCombinations(inputNames, inputNames)).isEqualTo(expected);
    }
}
