package uk.gov.digital.ho.pttg.application.namematching;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingPerformance.HasAliases.HAS_ALIASES;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingPerformance.HasAliases.NO_ALIASES;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingPerformance.HasSpecialCharacters.*;

public class NameMatchingPerformanceTest {

    private static final String NO_SPECIAL_CHARACTERS = "John";
    private static final String NAME_WITH_SPECIAL_CHARACTER = "Jóhn";

    private NameMatchingPerformance matchingPerformance = new NameMatchingPerformance();

    @Test
    public void hasAliases_inputNameHasNoAliases_noAliases() {
        InputNames noAliases = new InputNames("John", "Smith");
        assertThat(matchingPerformance.hasAliases(noAliases)).isEqualTo(NO_ALIASES);
    }

    @Test
    public void hasAliases_inputNameHasAlias_hasAliases() {
        InputNames hasAliases = new InputNames("John", "Smith", "Jones");
        assertThat(matchingPerformance.hasAliases(hasAliases)).isEqualTo(HAS_ALIASES);
    }

    @Test
    public void hasSpecialCharacters_neitherNameHasSpecialCharacters_none() {
        InputNames noSpecialCharacters = new InputNames(NO_SPECIAL_CHARACTERS, NO_SPECIAL_CHARACTERS);
        assertThat(matchingPerformance.hasSpecialCharacters(noSpecialCharacters)).isEqualTo(NONE);
    }

    @Test
    public void hasSpecialCharacters_firstNameSpecial_firstOnly() {
        InputNames firstNameSpecial = new InputNames(NAME_WITH_SPECIAL_CHARACTER, NO_SPECIAL_CHARACTERS);
        assertThat(matchingPerformance.hasSpecialCharacters(firstNameSpecial)).isEqualTo(FIRST_ONLY);
    }

    @Test
    public void hasSpecialCharacters_lastNameSpecial_lastOnly() {
        InputNames lastNameSpecial = new InputNames(NO_SPECIAL_CHARACTERS, NAME_WITH_SPECIAL_CHARACTER);
        assertThat(matchingPerformance.hasSpecialCharacters(lastNameSpecial)).isEqualTo(LAST_ONLY);
    }

    @Test
    public void hasSpecialCharacters_bothNamesSpecial_firstAndLast() {
        InputNames bothNamesSpecial = new InputNames(NAME_WITH_SPECIAL_CHARACTER, NAME_WITH_SPECIAL_CHARACTER);
        assertThat(matchingPerformance.hasSpecialCharacters(bothNamesSpecial)).isEqualTo(FIRST_AND_LAST);
    }
}