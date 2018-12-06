package uk.gov.digital.ho.pttg.application.namematching;

import org.junit.Test;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.NameDerivation.ALL_FIRST_NAMES;
import static uk.gov.digital.ho.pttg.application.namematching.NameDerivation.ALL_LAST_NAMES;

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

    @Test
    public void shouldProduceDefaultCandidateDerivationWithDeprecatedConstructor() {
        CandidateName candidateName = new CandidateName("some first name", "some last name");
        assertThat(candidateName.derivation()).isEqualTo(
                new CandidateDerivation(null, singletonList(null), ALL_FIRST_NAMES, ALL_LAST_NAMES));
    }
}