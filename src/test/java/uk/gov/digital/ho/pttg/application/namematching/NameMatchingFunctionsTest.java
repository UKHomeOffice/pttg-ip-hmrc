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
    public void splitTwoIntoDistinctNames_singleNames() {
        List<String> splitNames = NameMatchingFunctions.splitTwoIntoDistinctNames("first", "last");

        assertThat(splitNames.size()).isEqualTo(2);
        assertThat(splitNames.get(0)).isEqualTo("first");
        assertThat(splitNames.get(1)).isEqualTo("last");
    }

    @Test
    public void splitTwoIntoDistinctNames_multipleNames() {
        List<String> splitNames = NameMatchingFunctions.splitTwoIntoDistinctNames("one two three", "four five six seven");

        assertThat(splitNames.size()).isEqualTo(7);
        assertThat(splitNames.get(0)).isEqualTo("one");

        assertThat(splitNames.get(4)).isEqualTo("five");
        assertThat(splitNames.get(6)).isEqualTo("seven");
    }

    @Test
    public void removeAdditionalNamesOverMax_belowMax() {
        List<String> afterRemoved = NameMatchingFunctions.removeAdditionalNamesIfOverMax(Arrays.asList("one", "two", "three", "four", "five", "six", "seven"));

        assertThat(afterRemoved.size()).isEqualTo(7);
        assertThat(afterRemoved.get(0)).isEqualTo("one");
        assertThat(afterRemoved.get(3)).isEqualTo("four");
        assertThat(afterRemoved.get(4)).isEqualTo("five");
        assertThat(afterRemoved.get(6)).isEqualTo("seven");
    }

    @Test
    public void removeAdditionalNamesOverMax_overMax() {
        List<String> afterRemoved = NameMatchingFunctions.removeAdditionalNamesIfOverMax(Arrays.asList("one", "two", "three", "four", "extra-one", "extra-two", "five", "six", "seven"));

        assertThat(afterRemoved.size()).isEqualTo(7);
        assertThat(afterRemoved.get(0)).isEqualTo("one");
        assertThat(afterRemoved.get(3)).isEqualTo("four");
        assertThat(afterRemoved.get(4)).isEqualTo("five");
        assertThat(afterRemoved.get(6)).isEqualTo("seven");
    }

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
