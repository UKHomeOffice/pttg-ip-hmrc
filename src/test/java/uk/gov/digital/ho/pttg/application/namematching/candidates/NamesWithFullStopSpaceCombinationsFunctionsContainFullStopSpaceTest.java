package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NamesWithFullStopSpaceCombinationsFunctions.doesNotContainFullStopSpaceBetweenNames;

public class NamesWithFullStopSpaceCombinationsFunctionsContainFullStopSpaceTest {

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_emptyString_returnTrue() {
        InputNames emptyStringNames = new InputNames("", "");
        assertThat(doesNotContainFullStopSpaceBetweenNames(emptyStringNames)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_fullStopOnly_returnTrue() {
        InputNames fullStopOnlyNames = new InputNames(".", ".");
        assertThat(doesNotContainFullStopSpaceBetweenNames(fullStopOnlyNames)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_spaceOnly_returnTrue() {
        InputNames spaceOnlyNames = new InputNames(" ", " ");
        assertThat(doesNotContainFullStopSpaceBetweenNames(spaceOnlyNames)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_fullStopSpaceNoLetters_returnTrue() {
        InputNames fullStopSpaceOnlyNames = new InputNames(". ", ". ");
        assertThat(doesNotContainFullStopSpaceBetweenNames(fullStopSpaceOnlyNames)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_noNameAfterSpace_returnTrue() {
        InputNames noNameAfterSpaceNames = new InputNames("St. ", "St. ");
        assertThat(doesNotContainFullStopSpaceBetweenNames(noNameAfterSpaceNames)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_noNameBeforeFullStop_returnTrue() {
        InputNames noNameBeforeFullStopNames = new InputNames(". John", ". John");
        assertThat(doesNotContainFullStopSpaceBetweenNames(noNameBeforeFullStopNames)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_fullStopSpaceFirstName_returnFalse() {
        InputNames namesWithFullStopSpaceFirstName = new InputNames("St. John", "Smith");
        assertThat(doesNotContainFullStopSpaceBetweenNames(namesWithFullStopSpaceFirstName)).isFalse();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_fullStopSpaceLastName_returnFalse() {
        InputNames namesWithFullStopSpaceLastName = new InputNames("David", "St. John");
        assertThat(doesNotContainFullStopSpaceBetweenNames(namesWithFullStopSpaceLastName)).isFalse();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_doubleFullStopNotName_returnTrue() {
        InputNames doubleFullStopNames = new InputNames(".. John", ".. John");
        assertThat(doesNotContainFullStopSpaceBetweenNames(doubleFullStopNames)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_fullStopAfterSpaceNotName_returnTrue() {
        InputNames fullStopAfterSpaceNames = new InputNames("St. .", "St. .");
        assertThat(doesNotContainFullStopSpaceBetweenNames(fullStopAfterSpaceNames)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_accentedCharactersAtBoundary_returnFalse() {
        InputNames accentBeforeFullStop = new InputNames("à. b", "Smith");
        InputNames accentAfterFullStop = new InputNames("David", "b. à");
        assertThat(doesNotContainFullStopSpaceBetweenNames(accentBeforeFullStop)).isFalse();
        assertThat(doesNotContainFullStopSpaceBetweenNames(accentAfterFullStop)).isFalse();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_aliasWithOutFullstopSpace_returnTrue(){
        InputNames noFullStopSpace = new InputNames("John", "Smith", "Jones");

        assertThat(doesNotContainFullStopSpaceBetweenNames(noFullStopSpace)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_aliasWithFullstopSpace_returnFalse(){
        InputNames withFullStopSpace = new InputNames("John", "Smith", "St. John");

        assertThat(doesNotContainFullStopSpaceBetweenNames(withFullStopSpace)).isFalse();
    }
}