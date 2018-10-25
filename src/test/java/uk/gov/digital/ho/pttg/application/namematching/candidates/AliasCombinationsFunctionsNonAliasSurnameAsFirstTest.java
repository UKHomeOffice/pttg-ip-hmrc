package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AliasCombinationsFunctions.nonAliasSurnameAsFirstNameCombinations;

public class AliasCombinationsFunctionsNonAliasSurnameAsFirstTest {

    @Test
    public void shouldReturnEmptyListForEmptyInput() {
        InputNames emptyInputNames = new InputNames(emptyList(), emptyList());
        assertThat(nonAliasSurnameAsFirstNameCombinations(emptyInputNames)).isEqualTo(emptyList());
    }

    @Test
    public void shouldReturnEmptyListForSingleInputName() {
        InputNames oneFirstNameOnly = new InputNames(singletonList("somename"), emptyList());
        InputNames oneLastNameOnly = new InputNames(emptyList(), singletonList("somename"));

        assertThat(nonAliasSurnameAsFirstNameCombinations(oneFirstNameOnly)).isEqualTo(emptyList());
        assertThat(nonAliasSurnameAsFirstNameCombinations(oneLastNameOnly)).isEqualTo(emptyList());
    }

    @Test
    public void combinationsForOneFirstNameOneLastName() {
        InputNames inputNames = new InputNames("John", "Smith");
        List<CandidateName> expected = singletonList(new CandidateName("Smith", "John"));

        assertThat(nonAliasSurnameAsFirstNameCombinations(inputNames)).isEqualTo(expected);
    }

    @Test
    public void combinationsForTwoFirstNamesOneLastName() {
        InputNames inputNames = new InputNames("John David", "Smith");
        List<CandidateName> expected = asList(
                new CandidateName("Smith", "John"),
                new CandidateName("Smith", "David")
        );

        assertThat(nonAliasSurnameAsFirstNameCombinations(inputNames)).isEqualTo(expected);
    }

    @Test
    public void combinationsForOneFirstNameTwoLastNames() {
        InputNames inputNames = new InputNames("John", "Smith Evans");
        List<CandidateName> expected = asList(
                new CandidateName("Smith", "John"),
                new CandidateName("Evans", "John")
        );

        assertThat(nonAliasSurnameAsFirstNameCombinations(inputNames)).isEqualTo(expected);
    }

    @Test
    public void nonAliasSurnameAsFirstNameCombinationsForTwoFirstNamesTwoLastNames() {
        InputNames inputNames = new InputNames("John David", "Smith Evans");
        List<CandidateName> expected = asList(
                new CandidateName("Smith", "John"),
                new CandidateName("Smith", "David"),
                new CandidateName("Evans", "John"),
                new CandidateName("Evans", "David")
        );

        assertThat(nonAliasSurnameAsFirstNameCombinations(inputNames)).isEqualTo(expected);
    }

}
