package uk.gov.digital.ho.pttg.application.namematching;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingFunctions.deduplicate;

@RunWith(JUnit4.class)
public class NameMatchingFunctionsTest {

    @Test
    public void splitIntoDistinctNames_singleName() {
        List<String> splitNames = NameMatchingFunctions.splitIntoDistinctNames("single");

        assertThat(splitNames.size()).isEqualTo(1);
        assertThat(splitNames.get(0)).isEqualTo("single");
    }

    @Test
    public void splitIntoDistinctNames_twoName() {
        List<String> splitNames = NameMatchingFunctions.splitIntoDistinctNames("first second");

        assertThat(splitNames.size()).isEqualTo(2);
        assertThat(splitNames.get(0)).isEqualTo("first");
        assertThat(splitNames.get(1)).isEqualTo("second");
    }

    @Test
    public void splitIntoDistinctNames_severalNames() {
        List<String> splitNames = NameMatchingFunctions.splitIntoDistinctNames("first second thrird fourth fifth sixth");

        assertThat(splitNames.size()).isEqualTo(6);
        assertThat(splitNames.get(0)).isEqualTo("first");
        assertThat(splitNames.get(1)).isEqualTo("second");
        assertThat(splitNames.get(4)).isEqualTo("fifth");
        assertThat(splitNames.get(5)).isEqualTo("sixth");
    }

    @Test
    public void removeAdditionalNamesOverMax_belowMax() {
        InputNames afterRemoved = NameMatchingFunctions.removeAdditionalNamesIfOverMax(new InputNames(Arrays.asList("one", "two", "three", "four"), Arrays.asList("five", "six", "seven")));

        assertThat(afterRemoved.size()).isEqualTo(7);
        assertThat(afterRemoved.allNames().get(0)).isEqualTo("one");
        assertThat(afterRemoved.allNames().get(3)).isEqualTo("four");
        assertThat(afterRemoved.allNames().get(4)).isEqualTo("five");
        assertThat(afterRemoved.allNames().get(6)).isEqualTo("seven");
    }

    @Test
    public void removeAdditionalNamesOverMax_overMax() {
        InputNames afterRemoved = NameMatchingFunctions.removeAdditionalNamesIfOverMax(new InputNames(Arrays.asList("one", "two", "three", "four", "extra-one"), Arrays.asList("five", "six", "seven")));

        assertThat(afterRemoved.size()).isEqualTo(7);
        assertThat(afterRemoved.allNames().get(0)).isEqualTo("one");
        assertThat(afterRemoved.allNames().get(3)).isEqualTo("four");
        assertThat(afterRemoved.allNames().get(4)).isEqualTo("five");
        assertThat(afterRemoved.allNames().get(6)).isEqualTo("seven");
    }

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
