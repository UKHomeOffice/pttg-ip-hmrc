package uk.gov.digital.ho.pttg.application.namematching;

import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingCandidatesGeneratorFunctions.deduplicate;

public class NameMatchingCandidatesGeneratorFunctionsTest {

    @Test
    public void deduplicateEmptyListShouldReturnEmptyList() {
        assertThat(deduplicate(emptyList())).isEqualTo(emptyList());
    }

    @Test
    public void deduplicateSingletonListShouldReturnSingletonList() {
        List<PersonName> singleName = singletonList(new PersonName("any first name", "any last name"));
        assertThat(deduplicate(singleName)).isEqualTo(singleName);
    }

    @Test
    public void deduplicateTwoDistinctItemListShouldReturnTwoDistinctItemList() {
        List<PersonName> twoDistinctNames = asList(
                new PersonName("any first name", "any last name"),
                new PersonName("other first name", "some other last name")
        );
        assertThat(deduplicate(twoDistinctNames)).isEqualTo(twoDistinctNames);
    }

    @Test
    public void deduplicateSameFirstInitialOnlyShouldKeepAll() {
        List<PersonName> sameFirstInitialNames = asList(
                new PersonName("any first name", "any last name"),
                new PersonName("any other first name", "some other last name")
        );
        assertThat(deduplicate(sameFirstInitialNames)).isEqualTo(sameFirstInitialNames);
    }

    @Test
    public void deduplicateSameFirstThreeLettersOfSurnameOnlyShouldKeepAll() {
        List<PersonName> sameFirstInitialNames = asList(
                new PersonName("any first name", "any last name"),
                new PersonName("other first name", "any other last name")
        );
        assertThat(deduplicate(sameFirstInitialNames)).isEqualTo(sameFirstInitialNames);
    }

    @Test
    public void deduplicateShouldRemoveIfSameFirstInitialAndFirstThreeSurnameLetters() {
        List<PersonName> duplicateNames = asList(
                new PersonName("any first name", "any last name"),
                new PersonName("a first name", "any other last name")
        );
        List<PersonName> expected = singletonList(new PersonName("any first name", "any last name"));
        assertThat(deduplicate(duplicateNames)).isEqualTo(expected);
    }

    @Test
    public void deduplicateShouldKeepInputOrder() {
        List<PersonName> duplicateNames = asList(
                new PersonName("some other name", "some other name"),
                new PersonName("a first name", "any other last name"),
                new PersonName("any first name", "any last name")
        );
        List<PersonName> expected = asList(
                new PersonName("some other name", "some other name"),
                new PersonName("a first name", "any other last name")
        );
        assertThat(deduplicate(duplicateNames)).isEqualTo(expected);
    }
}