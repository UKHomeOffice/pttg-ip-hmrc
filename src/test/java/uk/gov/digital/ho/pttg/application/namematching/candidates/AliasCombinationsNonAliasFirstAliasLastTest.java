package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AliasCombinationsFunctions.nonAliasFirstAliasLastCombinations;

public class AliasCombinationsNonAliasFirstAliasLastTest {

    @Test
    public void shouldReturnEmptyListForEmptyInputs() {
        InputNames emptyNonAliasInputNames = new InputNames("", "", "somename");
        InputNames emptyAliasInputNames = new InputNames("somename", "somename", "");

        assertThat(nonAliasFirstAliasLastCombinations(emptyNonAliasInputNames, emptyNonAliasInputNames)).isEqualTo(emptyList());
        assertThat(nonAliasFirstAliasLastCombinations(emptyAliasInputNames, emptyAliasInputNames)).isEqualTo(emptyList());
    }

    @Test
    public void oneNonAliasOneAlias() {
        InputNames inputNames = new InputNames("", "nonalias", "aliasname");

        List<CandidateName> expectedCandidateName = singletonList(new CandidateName("nonalias", "aliasname"));
        List<CandidateName> actualCandidateName = nonAliasFirstAliasLastCombinations(inputNames, inputNames);

        assertThat(actualCandidateName).isEqualTo(expectedCandidateName);
    }

    @Test
    public void twoNonAliasesOneAlias() {
        InputNames inputNames = new InputNames("nonalias1", "nonalias2", "aliasname");

        List<CandidateName> expectedCandidateName = asList(
                new CandidateName("nonalias1", "aliasname"),
                new CandidateName("nonalias2", "aliasname")
        );

        List<CandidateName> actualCandidateName = nonAliasFirstAliasLastCombinations(inputNames, inputNames);
        assertThat(actualCandidateName).isEqualTo(expectedCandidateName);
    }

    @Test
    public void oneNonAliasTwoAliases() {
        InputNames inputNames = new InputNames("nonalias", "", "aliasname1 aliasname2");


        List<CandidateName> expectedCandidateName = asList(
                new CandidateName("nonalias", "aliasname2"),
                new CandidateName("nonalias", "aliasname1")
        );

        List<CandidateName> actualCandidateName = nonAliasFirstAliasLastCombinations(inputNames, inputNames);
        assertThat(actualCandidateName).isEqualTo(expectedCandidateName);
    }

    @Test
    public void twoNonAliasesTwoAliases() {
        InputNames inputNames = new InputNames("nonalias1", "nonalias2", "aliasname1 aliasname2");


        List<CandidateName> expectedCandidateName = asList(
                new CandidateName("nonalias1", "aliasname2"),
                new CandidateName("nonalias2", "aliasname2"),
                new CandidateName("nonalias1", "aliasname1"),
                new CandidateName("nonalias2", "aliasname1")
        );

        List<CandidateName> actualCandidateName = nonAliasFirstAliasLastCombinations(inputNames, inputNames);
        assertThat(actualCandidateName).isEqualTo(expectedCandidateName);
    }
}
