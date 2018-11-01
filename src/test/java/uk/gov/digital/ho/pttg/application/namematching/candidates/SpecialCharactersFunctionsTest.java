package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.SpecialCharactersFunctions.namesAreNotEmpty;

public class SpecialCharactersFunctionsTest {

    private static final String SOME_NAME = "someName";

    @Test
    public void namesAreNotEmpty_bothNamesEmpty_isFalse() {
        InputNames emptyInputNames = new InputNames("", "");
        assertThat(namesAreNotEmpty(emptyInputNames)).isFalse();
    }

    @Test
    public void namesAreNotEmpty_firstNameEmpty_isTrue() {
        InputNames emptyFirstName = new InputNames("", SOME_NAME);
        assertThat(namesAreNotEmpty(emptyFirstName)).isTrue();
    }

    @Test
    public void namesAreNotEmpty_lastNameEmpty_isTrue() {
        InputNames emptyLastName = new InputNames(SOME_NAME, "");
        assertThat(namesAreNotEmpty(emptyLastName)).isTrue();
    }

    @Test
    public void namesAreNotEmpty_neitherNameEmpty_isTrue() {
        InputNames emptyLastName = new InputNames(SOME_NAME, SOME_NAME);
        assertThat(namesAreNotEmpty(emptyLastName)).isTrue();
    }
}