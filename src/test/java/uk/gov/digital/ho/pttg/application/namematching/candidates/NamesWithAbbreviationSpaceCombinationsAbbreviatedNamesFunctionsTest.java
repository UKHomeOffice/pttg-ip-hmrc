package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AbbreviatedNamesFunctions.doesNotContainAbbreviatedNames;

public class NamesWithAbbreviationSpaceCombinationsAbbreviatedNamesFunctionsTest {

    @Test
    public void doesNotContainAbbreviationSpaceBetweenNames_emptyString_returnTrue() {
        InputNames emptyStringNames = new InputNames("", "");
        assertThat(doesNotContainAbbreviatedNames(emptyStringNames)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_fullStopOnly_returnTrue() {
        InputNames fullStopOnlyNames = new InputNames(".", ".");
        assertThat(doesNotContainAbbreviatedNames(fullStopOnlyNames)).isTrue();
    }

    @Test
    public void doesNotContainApostropheSpaceBetweenNames_apostropheOnly_returnTrue() {
        InputNames apostropheOnlyNames = new InputNames("'", "'");
        assertThat(doesNotContainAbbreviatedNames(apostropheOnlyNames)).isTrue();
    }

    @Test
    public void doesNotContainAbbreviationSpaceBetweenNames_spaceOnly_returnTrue() {
        InputNames spaceOnlyNames = new InputNames(" ", " ");
        assertThat(doesNotContainAbbreviatedNames(spaceOnlyNames)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_fullStopSpaceNoLetters_returnTrue() {
        InputNames fullStopSpaceOnlyNames = new InputNames(". ", ". ");
        assertThat(doesNotContainAbbreviatedNames(fullStopSpaceOnlyNames)).isTrue();
    }

    @Test
    public void doesNotContainApostropheSpaceBetweenNames_fullStopSpaceNoLetters_returnTrue() {
        InputNames apostropheSpaceOnlyNames = new InputNames("' ", "' ");
        assertThat(doesNotContainAbbreviatedNames(apostropheSpaceOnlyNames)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_noNameAfterSpace_returnTrue() {
        InputNames noNameAfterSpaceNames = new InputNames("St. ", "St. ");
        assertThat(doesNotContainAbbreviatedNames(noNameAfterSpaceNames)).isTrue();
    }

    @Test
    public void doesNotContainApostropheSpaceBetweenNames_noNameAfterSpace_returnTrue() {
        InputNames noNameAfterSpaceNames = new InputNames("O' ", "O' ");
        assertThat(doesNotContainAbbreviatedNames(noNameAfterSpaceNames)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_noNameBeforeFullStop_returnTrue() {
        InputNames noNameBeforeFullStopNames = new InputNames(". John", ". John");
        assertThat(doesNotContainAbbreviatedNames(noNameBeforeFullStopNames)).isTrue();
    }

    @Test
    public void doesNotContainApostropheSpaceBetweenNames_noNameBeforeApostrophe_returnTrue() {
        InputNames noNameBeforeApostropheNames = new InputNames("' Connor", "' Connor");
        assertThat(doesNotContainAbbreviatedNames(noNameBeforeApostropheNames)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_fullStopSpaceFirstName_returnFalse() {
        InputNames namesWithFullStopSpaceFirstName = new InputNames("St. John", "Smith");
        assertThat(doesNotContainAbbreviatedNames(namesWithFullStopSpaceFirstName)).isFalse();
    }

    @Test
    public void doesNotContainApostropheSpaceBetweenNames_apostropheSpaceFirstName_returnFalse() {
        InputNames namesWithApostropheSpaceFirstName = new InputNames("O' Connor", "John");
        assertThat(doesNotContainAbbreviatedNames(namesWithApostropheSpaceFirstName)).isFalse();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_fullStopSpaceLastName_returnFalse() {
        InputNames namesWithFullStopSpaceLastName = new InputNames("David", "St. John");
        assertThat(doesNotContainAbbreviatedNames(namesWithFullStopSpaceLastName)).isFalse();
    }

    @Test
    public void doesNotContainApostropheSpaceBetweenNames_apostropheSpaceLastName_returnFalse() {
        InputNames namesWithApostropheSpaceLastName = new InputNames("David", "O' Connor");
        assertThat(doesNotContainAbbreviatedNames(namesWithApostropheSpaceLastName)).isFalse();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_doubleFullStopNotName_returnTrue() {
        InputNames doubleFullStopNames = new InputNames(".. John", ".. John");
        assertThat(doesNotContainAbbreviatedNames(doubleFullStopNames)).isTrue();
    }

    @Test
    public void doesNotContainApostropheSpaceBetweenNames_doubleApostropheNotName_returnTrue() {
        InputNames doubleApostropheNames = new InputNames("'' Connor", "'' Connor");
        assertThat(doesNotContainAbbreviatedNames(doubleApostropheNames)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_fullStopAfterSpaceNotName_returnTrue() {
        InputNames fullStopAfterSpaceNames = new InputNames("St. .","St. .");
        assertThat(doesNotContainAbbreviatedNames(fullStopAfterSpaceNames)).isTrue();
    }

    @Test
    public void doesNotContainApostropheSpaceBetweenNames_apostropheAfterSpaceNotName_returnTrue() {
        InputNames apostropheAfterSpaceNames = new InputNames("Da' '","Da' '");
        assertThat(doesNotContainAbbreviatedNames(apostropheAfterSpaceNames)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_accentedCharactersAtBoundary_returnFalse() {
        InputNames accentBeforeFullStop = new InputNames("à. b", "Smith");
        InputNames accentAfterFullStop = new InputNames("David", "b. à");
        assertThat(doesNotContainAbbreviatedNames(accentBeforeFullStop)).isFalse();
        assertThat(doesNotContainAbbreviatedNames(accentAfterFullStop)).isFalse();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_aliasWithOutFullstopSpace_returnTrue(){
        InputNames noFullStopSpace = new InputNames("John", "Smith", "Jones");

        assertThat(doesNotContainAbbreviatedNames(noFullStopSpace)).isTrue();
    }

    @Test
    public void doesNotContainFullStopSpaceBetweenNames_aliasWithFullstopSpace_returnFalse(){
        InputNames withFullStopSpace = new InputNames("John", "Smith", "St. John");

        assertThat(doesNotContainAbbreviatedNames(withFullStopSpace)).isFalse();
    }

    @Test
    public void doesNotContainApostropheSpaceBetweenNames_aliasWithApostropheSpace_returnFalse(){
        InputNames withFullStopSpace = new InputNames("John", "Smith", "O' Connor");

        assertThat(doesNotContainAbbreviatedNames(withFullStopSpace)).isFalse();
    }
}