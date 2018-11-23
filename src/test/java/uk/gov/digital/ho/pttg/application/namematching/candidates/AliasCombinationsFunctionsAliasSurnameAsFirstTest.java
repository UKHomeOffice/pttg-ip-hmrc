package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AliasCombinationsFunctions.aliasSurnameAsFirstNameCombinations;

public class AliasCombinationsFunctionsAliasSurnameAsFirstTest {

    @Test
    public void shouldReturnEmptyListForEmptyInputs() {
        InputNames emptyInputNames = new InputNames("", "", "");

        assertThat(aliasSurnameAsFirstNameCombinations(emptyInputNames, emptyInputNames)).isEqualTo(emptyList());
    }

    @Test
    public void shouldReturnEmptyListForEmptyEmptyAliasSurnames() {
        InputNames emptyAliasInputNames = new InputNames("John David", "Smith Evans", "");

        assertThat(aliasSurnameAsFirstNameCombinations(emptyAliasInputNames, emptyAliasInputNames)).isEqualTo(emptyList());
    }

    @Test
    public void combinationsForOneFirstNameAndOneAliasSurname() {
        InputNames inputNames = new InputNames("John", "", "Smith");
        List<CandidateName> expected = singletonList(new CandidateName("Smith", "John"));

        assertThat(aliasSurnameAsFirstNameCombinations(inputNames, inputNames)).isEqualTo(expected);
    }

    @Test
    public void combinationsForTwoFirstNamesAndOneAliasSurname() {
        InputNames inputNames = new InputNames("John David", "", "Smith");
        List<CandidateName> expected = asList(
                new CandidateName("Smith", "John"),
                new CandidateName("Smith", "David")
        );

        assertThat(aliasSurnameAsFirstNameCombinations(inputNames, inputNames)).isEqualTo(expected);
    }

    @Test
    public void combinationsForOneFirstNameAndTwoAliasSurnames() {
        InputNames inputNames = new InputNames("John", "", "Smith Evans");
        List<CandidateName> expected = asList(
                new CandidateName("Smith", "John"),
                new CandidateName("Smith", "Evans"),
                new CandidateName("Evans", "John"),
                new CandidateName("Evans", "Smith")
        );

        assertThat(aliasSurnameAsFirstNameCombinations(inputNames, inputNames)).isEqualTo(expected);
    }

    @Test
    public void combinationsForTwoFirstNamesAndTwoAliasSurnames() {
        InputNames inputNames = new InputNames("John David", "", "Smith Evans");
        List<CandidateName> expected = asList(
                new CandidateName("Smith", "John"),
                new CandidateName("Smith", "David"),
                new CandidateName("Smith", "Evans"),
                new CandidateName("Evans", "John"),
                new CandidateName("Evans", "David"),
                new CandidateName("Evans", "Smith")
        );

        assertThat(aliasSurnameAsFirstNameCombinations(inputNames, inputNames)).isEqualTo(expected);
    }

    @Test
    public void combinationsForTwoFirstNamesTwoSurnamesTwoAliasSurnames() {
        InputNames inputNames = new InputNames("John David", "Paul Roger", "Smith Evans");
        List<CandidateName> expected = asList(
                new CandidateName("Smith", "John"),
                new CandidateName("Smith", "David"),
                new CandidateName("Smith", "Paul"),
                new CandidateName("Smith", "Roger"),
                new CandidateName("Smith", "Evans"),
                new CandidateName("Evans", "John"),
                new CandidateName("Evans", "David"),
                new CandidateName("Evans", "Paul"),
                new CandidateName("Evans", "Roger"),
                new CandidateName("Evans", "Smith")
        );

        assertThat(aliasSurnameAsFirstNameCombinations(inputNames, inputNames)).isEqualTo(expected);
    }

}
