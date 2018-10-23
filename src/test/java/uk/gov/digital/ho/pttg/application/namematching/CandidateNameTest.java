package uk.gov.digital.ho.pttg.application.namematching;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CandidateNameTest {

    @Test
    public void emptyNameHmrcEquivalentShouldBeEmptyName() {
        CandidateName emptyName = new CandidateName("", "");
        assertThat(emptyName.hmrcNameMatchingEquivalent()).isEqualTo(emptyName);
    }

    @Test
    public void oneCharacterFirstAndSurnameHmrcEquivalentShouldBeSame() {
        CandidateName oneCharNames = new CandidateName("a", "b");
        assertThat(oneCharNames.hmrcNameMatchingEquivalent()).isEqualTo(oneCharNames);
    }

    @Test
    public void oneCharacterFirstAndThreeCharacterSurnameHmrcEquivalentShouldBeSame() {
        CandidateName inputName = new CandidateName("a", "bcd");
        assertThat(inputName.hmrcNameMatchingEquivalent()).isEqualTo(inputName);
    }

    @Test
    public void hmrcEquivalentSurnameShouldOnlyUseFirstInitial() {
        CandidateName inputName = new CandidateName("ab", "cde");
        CandidateName expected = new CandidateName("a", "cde");
        assertThat(inputName.hmrcNameMatchingEquivalent()).isEqualTo(expected);
    }

    @Test
    public void hmrcEquivalentSurnameShouldOnlyUseFirstThreeSurnameLetters() {
        CandidateName inputName = new CandidateName("a", "cdefg");
        CandidateName expected = new CandidateName("a", "cde");
        assertThat(inputName.hmrcNameMatchingEquivalent()).isEqualTo(expected);
    }

    @Test
    public void hmrcEquivalentSurnameShouldUseFirstThreeSignificantSurnameCharacters() {
        CandidateName inputName = new CandidateName("a", "cd efg");
        CandidateName expected = new CandidateName("a", "cd e");
        assertThat(inputName.hmrcNameMatchingEquivalent()).isEqualTo(expected);
    }
}