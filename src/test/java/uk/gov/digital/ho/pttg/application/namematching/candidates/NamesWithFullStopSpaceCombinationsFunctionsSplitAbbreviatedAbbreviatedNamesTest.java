package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AbbreviatedNamesFunctions.splitAroundAbbreviatedNames;

public class NamesWithFullStopSpaceCombinationsFunctionsSplitAbbreviatedAbbreviatedNamesTest {

    @Test
    public void splitNamesIgnoringFullStopSpace_emptyName_returnEmptyList() {
        assertThat(splitAroundAbbreviatedNames("")).isEmpty();
    }

    @Test
    public void splitNamesIgnoringFullStopSpace_twoNames_returnAsList() {
        String twoNames = "John Smith";
        List<String> expected = asList("John", "Smith");

        assertThat(splitAroundAbbreviatedNames(twoNames))
                .isEqualTo(expected);
    }

    @Test
    public void splitNamesIgnoringFullStopSpace_fullStopNoSpaceInName_doNotSplit() {
        String nameWithFullStop = "St.John";
        List<String> expected = singletonList("St.John");

        assertThat(splitAroundAbbreviatedNames(nameWithFullStop))
                .isEqualTo(expected);
    }

    @Test
    public void splitNamesIgnoringFullStopSpace_fullStopSpaceInName_doNotSplit() {
        String nameWithFullStop = "St. John";
        List<String> expected = singletonList("St. John");

        assertThat(splitAroundAbbreviatedNames(nameWithFullStop))
                .isEqualTo(expected);
    }

    @Test
    public void splitNamesIgnoringFullStopSpace_multipleSpacesAfterFullStop_reduceToSingleSpace() {
        String nameWithTwoSpaces = "St.  John";
        List<String> expected = singletonList("St. John");

        assertThat(splitAroundAbbreviatedNames(nameWithTwoSpaces))
                .isEqualTo(expected);
    }

    @Test
    public void splitNamesIgnoringFullStopSpace_tabAfterFullStop_changeToSingleSpace() {
        String nameWithTwoSpaces = "St.\tJohn";
        List<String> expected = singletonList("St. John");

        assertThat(splitAroundAbbreviatedNames(nameWithTwoSpaces))
                .isEqualTo(expected);
    }
}