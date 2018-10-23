package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AliasSurnameCombinationsFunctions.*;

public class AliasSurnameCombinationsFunctionsTest {

    @Test
    public void removeNameFromEmptyListShouldReturnEmptyList() {
        assertThat(removeName("", emptyList())).isEqualTo(emptyList());
        assertThat(removeName("any non-empty string", emptyList())).isEqualTo(emptyList());
    }

    @Test
    public void removeNameThatIsNotInListShouldReturnOriginalList() {
        List<String> names = asList("John", "Smith");
        assertThat(removeName("David", names)).isEqualTo(names);
    }

    @Test
    public void removeNameThatIsInListShouldRemoveName() {
        List<String> names = asList("John", "Smith");
        assertThat(removeName("John", names)).isEqualTo(singletonList("Smith"));
        assertThat(removeName("Smith", names)).isEqualTo(singletonList("John"));
    }

    @Test
    public void nonAliasFirstAliasLastCombinationsShouldEmptyListInput() {
        assertThat(nonAliasFirstAliasLastCombinations(emptyList(), emptyList())).isEqualTo(emptyList());
        assertThat(nonAliasFirstAliasLastCombinations(emptyList(), singletonList("somename"))).isEqualTo(emptyList());
        assertThat(nonAliasFirstAliasLastCombinations(singletonList("somename"), emptyList())).isEqualTo(emptyList());
    }

    @Test
    public void nonAliasFirstAliasLastCombinationsOneNonAliasOneAlias() {
        List<String> nonAliasNames = singletonList("nonalias");
        List<String> aliasSurnames = singletonList("aliasname");

        List<CandidateName> expectedCandidateName = singletonList(new CandidateName("nonalias", "aliasname"));
        List<CandidateName> actualCandidateName = nonAliasFirstAliasLastCombinations(nonAliasNames, aliasSurnames);

        assertThat(actualCandidateName).isEqualTo(expectedCandidateName);
    }

    @Test
    public void nonAliasFirstAliasLastCombinationsTwoNonAliasesOneAlias() {
        List<String> nonAliasNames = asList("nonalias1", "nonalias2");
        List<String> aliasSurnames = singletonList("aliasname");

        List<CandidateName> expectedCandidateName = asList(
                new CandidateName("nonalias1", "aliasname"),
                new CandidateName("nonalias2", "aliasname")
        );

        List<CandidateName> actualCandidateName = nonAliasFirstAliasLastCombinations(nonAliasNames, aliasSurnames);
        assertThat(actualCandidateName).isEqualTo(expectedCandidateName);
    }

    @Test
    public void nonAliasFirstAliasLastCombinationsOneNonAliasTwoAliases() {
        List<String> nonAliasNames = singletonList("nonalias");
        List<String> aliasSurnames = asList("aliasname1", "aliasname2");

        List<CandidateName> expectedCandidateName = asList(
                new CandidateName("nonalias", "aliasname2"),
                new CandidateName("nonalias", "aliasname1")
        );

        List<CandidateName> actualCandidateName = nonAliasFirstAliasLastCombinations(nonAliasNames, aliasSurnames);
        assertThat(actualCandidateName).isEqualTo(expectedCandidateName);
    }

    @Test
    public void nonAliasFirstAliasLastCombinationsTwoNonAliasesTwoAliases() {
        List<String> nonAliasNames = asList("nonalias1", "nonalias2");
        List<String> aliasSurnames = asList("aliasname1", "aliasname2");

        List<CandidateName> expectedCandidateName = asList(
                new CandidateName("nonalias1", "aliasname2"),
                new CandidateName("nonalias2", "aliasname2"),
                new CandidateName("nonalias1", "aliasname1"),
                new CandidateName("nonalias2", "aliasname1")
        );

        List<CandidateName> actualCandidateName = nonAliasFirstAliasLastCombinations(nonAliasNames, aliasSurnames);
        assertThat(actualCandidateName).isEqualTo(expectedCandidateName);
    }

    @Test
    public void firstNameCombinationsForEmptyListShouldReturnEmptyList() {
        assertThat(firstNameCombinations(emptyList())).isEqualTo(emptyList());
    }

    @Test
    public void firstNameCombinationsForOneNameShouldReturnEmptyList() {
        assertThat(firstNameCombinations(singletonList("John"))).isEqualTo(emptyList());
    }

    @Test
    public void firstNameCombinationsForTwoNamesShouldReturnCombinations() {
        List<String> firstNames = asList("John", "David");
        List<CandidateName> expected = asList(
                new CandidateName("John", "David"),
                new CandidateName("David", "John")
        );

        assertThat(firstNameCombinations(firstNames)).isEqualTo(expected);
    }

    @Test
    public void firstNameCombinationsForThreeNamesShouldReturnCombinations() {
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

    @Test
    public void nonAliasSurnameAsFirstNameCombinationsShouldReturnEmptyListForEmptyInput() {
        InputNames emptyInputNames = new InputNames(emptyList(), emptyList());
        assertThat(nonAliasSurnameAsFirstNameCombinations(emptyInputNames)).isEqualTo(emptyList());
    }

    @Test
    public void nonAliasSurnameAsFirstNameCombinationsShouldReturnEmptyListForSingleInputName() {
        InputNames oneFirstNameOnly = new InputNames(singletonList("somename"), emptyList());
        InputNames oneLastNameOnly = new InputNames(emptyList(), singletonList("somename"));

        assertThat(nonAliasSurnameAsFirstNameCombinations(oneFirstNameOnly)).isEqualTo(emptyList());
        assertThat(nonAliasSurnameAsFirstNameCombinations(oneLastNameOnly)).isEqualTo(emptyList());
    }

    @Test
    public void nonAliasSurnameAsFirstNameCombinationsForOneFirstNameOneLastName() {
        InputNames inputNames = new InputNames("John", "Smith");
        List<CandidateName> expected = singletonList(new CandidateName("Smith", "John"));

        assertThat(nonAliasSurnameAsFirstNameCombinations(inputNames)).isEqualTo(expected);
    }

    @Test
    public void nonAliasSurnameAsFirstNameCombinationsForTwoFirstNamesOneLastName() {
        InputNames inputNames = new InputNames("John David", "Smith");
        List<CandidateName> expected = asList(
                new CandidateName("Smith", "John"),
                new CandidateName("Smith", "David")
        );

        assertThat(nonAliasSurnameAsFirstNameCombinations(inputNames)).isEqualTo(expected);
    }

    @Test
    public void nonAliasSurnameAsFirstNameCombinationsForOneFirstNameTwoLastNames() {
        InputNames inputNames = new InputNames("John", "Smith Evans");
        List<CandidateName> expected = asList(
                new CandidateName("Smith", "John"),
                new CandidateName("Smith", "Evans"),
                new CandidateName("Evans", "John"),
                new CandidateName("Evans", "Smith")
        );

        assertThat(nonAliasSurnameAsFirstNameCombinations(inputNames)).isEqualTo(expected);
    }

    @Test
    public void nonAliasSurnameAsFirstNameCombinationsForTwoFirstNamesTwoLastNames() {
        InputNames inputNames = new InputNames("John David", "Smith Evans");
        List<CandidateName> expected = asList(
                new CandidateName("Smith", "John"),
                new CandidateName("Smith", "David"),
                new CandidateName("Smith", "Evans"),
                new CandidateName("Evans", "John"),
                new CandidateName("Evans", "David"),
                new CandidateName("Evans", "Smith")
        );

        assertThat(nonAliasSurnameAsFirstNameCombinations(inputNames)).isEqualTo(expected);
    }

    @Test
    public void aliasSurnameAsFirstNameCombinationsShouldReturnEmptyListForEmptyInputs() {
        InputNames emptyInputNames = new InputNames(emptyList(), emptyList(), emptyList());

        assertThat(aliasSurnameAsFirstNameCombinations(emptyInputNames)).isEqualTo(emptyList());
    }

    @Test
    public void aliasSurnameAsFirstNameCombinationsShouldReturnEmptyListForEmptyEmptyAliasSurnames() {
        InputNames emptyAliasInputNames = new InputNames(asList("John", "David"), asList("Smith", "Evans"), emptyList());

        assertThat(aliasSurnameAsFirstNameCombinations(emptyAliasInputNames)).isEqualTo(emptyList());
    }

    @Test
    public void aliasSurnameAsFirstNameCombinationsForOneFirstNameAndOneAliasSurname() {
        InputNames inputNames = new InputNames("John", "", "Smith");
        List<CandidateName> expected = singletonList(new CandidateName("Smith", "John"));

        assertThat(aliasSurnameAsFirstNameCombinations(inputNames)).isEqualTo(expected);
    }

    @Test
    public void aliasSurnameAsFirstNameCombinationsForTwoFirstNamesAndOneAliasSurname() {
        InputNames inputNames = new InputNames("John David", "", "Smith");
        List<CandidateName> expected = asList(
                new CandidateName("Smith", "John"),
                new CandidateName("Smith", "David")
        );

        assertThat(aliasSurnameAsFirstNameCombinations(inputNames)).isEqualTo(expected);
    }

    @Test
    public void aliasSurnameAsFirstNameCombinationsForOneFirstNameAndTwoAliasSurnames() {
        InputNames inputNames = new InputNames("John", "", "Smith Evans");
        List<CandidateName> expected = asList(
                new CandidateName("Smith", "John"),
                new CandidateName("Smith", "Evans"),
                new CandidateName("Evans", "John"),
                new CandidateName("Evans", "Smith")
        );

        assertThat(aliasSurnameAsFirstNameCombinations(inputNames)).isEqualTo(expected);
    }

    @Test
    public void aliasSurnameAsFirstNameCombinationsForTwoFirstNamesAndTwoAliasSurnames() {
        InputNames inputNames = new InputNames("John David", "", "Smith Evans");
        List<CandidateName> expected = asList(
                new CandidateName("Smith", "John"),
                new CandidateName("Smith", "David"),
                new CandidateName("Smith", "Evans"),
                new CandidateName("Evans", "John"),
                new CandidateName("Evans", "David"),
                new CandidateName("Evans", "Smith")
        );

        assertThat(aliasSurnameAsFirstNameCombinations(inputNames)).isEqualTo(expected);
    }

    @Test
    public void aliasSurnameAsFirstNameCombinationsForTwoFirstNamesTwoSurnamesTwoAliasSurnames() {
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

        assertThat(aliasSurnameAsFirstNameCombinations(inputNames)).isEqualTo(expected);
    }
}
