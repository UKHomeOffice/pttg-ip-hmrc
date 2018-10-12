package uk.gov.digital.ho.pttg.application.namematching;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonNameTest {

    @Test
    public void emptyNameHmrcEquivalentShouldBeEmptyName() {
        PersonName emptyName = new PersonName("", "");
        assertThat(emptyName.hmrcNameMatchingEquivalent()).isEqualTo(emptyName);
    }

    @Test
    public void oneCharacterFirstAndSurnameHmrcEquivalentShouldBeSame() {
        PersonName oneCharNames = new PersonName("a", "b");
        assertThat(oneCharNames.hmrcNameMatchingEquivalent()).isEqualTo(oneCharNames);
    }

    @Test
    public void oneCharacterFirstAndThreeCharacterSurnameHmrcEquivalentShouldBeSame() {
        PersonName inputName = new PersonName("a", "bcd");
        assertThat(inputName.hmrcNameMatchingEquivalent()).isEqualTo(inputName);
    }

    @Test
    public void hmrcEquivalentSurnameShouldOnlyUseFirstInitial() {
        PersonName inputName = new PersonName("ab", "cde");
        PersonName expected = new PersonName("a", "cde");
        assertThat(inputName.hmrcNameMatchingEquivalent()).isEqualTo(expected);
    }

    @Test
    public void hmrcEquivalentSurnameShouldOnlyUseFirstThreeSurnameLetters() {
        PersonName inputName = new PersonName("a", "cdefg");
        PersonName expected = new PersonName("a", "cde");
        assertThat(inputName.hmrcNameMatchingEquivalent()).isEqualTo(expected);
    }

    @Test
    public void hmrcEquivalentSurnameShouldUseFirstThreeSignificantSurnameCharacters() {
        PersonName inputName = new PersonName("a", "cd efg");
        PersonName expected = new PersonName("a", "cd e");
        assertThat(inputName.hmrcNameMatchingEquivalent()).isEqualTo(expected);
    }
}