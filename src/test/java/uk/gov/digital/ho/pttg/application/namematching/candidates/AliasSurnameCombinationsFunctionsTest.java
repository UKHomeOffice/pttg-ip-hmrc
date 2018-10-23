package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AliasSurnameCombinationsFunctions.nonAliasFirstAliasLastCombinations;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AliasSurnameCombinationsFunctions.removeName;

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

}