package uk.gov.digital.ho.pttg.application.namematching;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingCandidateServiceFunctions.deduplicate;

@RunWith(MockitoJUnitRunner.class)
public class NameMatchingCandidateServiceFunctionsTest {

    @Test
    public void deduplicateEmptyListShouldReturnEmptyList() {
        assertThat(deduplicate(emptyList())).isEqualTo(emptyList());
    }

    @Test
    public void deduplicateSingletonListShouldReturnSingletonList() {
        List<CandidateName> singleName = singletonList(new CandidateName("any first name", "any last name"));
        assertThat(deduplicate(singleName)).isEqualTo(singleName);
    }

    @Test
    public void deduplicateTwoDistinctItemListShouldReturnTwoDistinctItemList() {
        List<CandidateName> twoDistinctNames = asList(
                new CandidateName("any first name", "any last name"),
                new CandidateName("other first name", "some other last name")
        );
        assertThat(deduplicate(twoDistinctNames)).isEqualTo(twoDistinctNames);
    }

    @Test
    public void deduplicateSameFirstInitialOnlyShouldKeepAll() {
        List<CandidateName> sameFirstInitialNames = asList(
                new CandidateName("any first name", "any last name"),
                new CandidateName("any other first name", "some other last name")
        );
        assertThat(deduplicate(sameFirstInitialNames)).isEqualTo(sameFirstInitialNames);
    }

    @Test
    public void deduplicateSameFirstThreeLettersOfSurnameOnlyShouldKeepAll() {
        List<CandidateName> sameFirstInitialNames = asList(
                new CandidateName("any first name", "any last name"),
                new CandidateName("other first name", "any other last name")
        );
        assertThat(deduplicate(sameFirstInitialNames)).isEqualTo(sameFirstInitialNames);
    }

    @Test
    public void deduplicateShouldRemoveIfSameFirstInitialAndFirstThreeSurnameLetters() {
        List<CandidateName> duplicateNames = asList(
                new CandidateName("any first name", "any last name"),
                new CandidateName("a first name", "any other last name")
        );
        List<CandidateName> expected = singletonList(new CandidateName("any first name", "any last name"));
        assertThat(deduplicate(duplicateNames)).isEqualTo(expected);
    }

    @Test
    public void deduplicateShouldKeepInputOrder() {
        List<CandidateName> duplicateNames = asList(
                new CandidateName("some other name", "some other name"),
                new CandidateName("a first name", "any other last name"),
                new CandidateName("any first name", "any last name")
        );
        List<CandidateName> expected = asList(
                new CandidateName("some other name", "some other name"),
                new CandidateName("a first name", "any other last name")
        );
        assertThat(deduplicate(duplicateNames)).isEqualTo(expected);
    }

}
