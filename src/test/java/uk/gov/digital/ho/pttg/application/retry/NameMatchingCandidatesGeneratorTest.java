package uk.gov.digital.ho.pttg.application.retry;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class NameMatchingCandidatesGeneratorTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldErrorIfNoName() {
        NameMatchingCandidatesGenerator.generateCandidates("", "");
    }

    @Test
    public void shouldDuplicateSingleFirstName() {
        List<String> names = NameMatchingCandidatesGenerator.generateCandidates("Mono", "");

        assertThat("There should be a single name", names.size(), is(1));
        assertThat("The first name should be duplicated", names.get(0), is("Mono Mono"));
    }

    @Test
    public void shouldDuplicateSingleLastName() {
        List<String> names = NameMatchingCandidatesGenerator.generateCandidates("", "Mono");

        assertThat("There should be a single name", names.size(), is(1));
        assertThat("The last name should be duplicated", names.get(0), is("Mono Mono"));
    }

    @Test
    public void shouldSwitchSingleFirstAndLastName() {
        List<String> names = NameMatchingCandidatesGenerator.generateCandidates("Arthur", "Bobbins");

        assertThat("The number of generated names should be as expected", names.size(), is(2));
        assertThat("The names should be correctly generated in the defined order", names.get(0), is("Arthur Bobbins"));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is("Bobbins Arthur"));
    }

    @Test
    public void shouldTryAllCombinationsOfTwoFirstNames() {
        List<String> names = NameMatchingCandidatesGenerator.generateCandidates("Arthur Brian", "Coates");

        assertThat("The number of generated names should be as expected", names.size(), is(6));
        assertThat("The names should be correctly generated in the defined order",names.get(0), is("Arthur Coates"));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is("Brian Coates"));
        assertThat("The names should be correctly generated in the defined order", names.get(2), is("Coates Arthur"));
        assertThat("The names should be correctly generated in the defined order", names.get(3), is("Coates Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(4), is("Arthur Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(5), is("Brian Arthur"));
    }

    @Test
    public void shouldTryAllCombinationsOfThreeFirstNames() {
        List<String> names = NameMatchingCandidatesGenerator.generateCandidates("Arthur Brian Chris", "Doom");

        assertThat("The number of generated names should be as expected", names.size(), is(12));
        assertThat("The names should be correctly generated in the defined order",names.get(0), is("Arthur Doom"));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is("Brian Doom"));
        assertThat("The names should be correctly generated in the defined order", names.get(2), is("Chris Doom"));
        assertThat("The names should be correctly generated in the defined order", names.get(3), is("Arthur Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(4), is("Arthur Chris"));
        assertThat("The names should be correctly generated in the defined order", names.get(5), is("Brian Arthur"));
        assertThat("The names should be correctly generated in the defined order", names.get(6), is("Chris Arthur"));
        assertThat("The names should be correctly generated in the defined order", names.get(7), is("Doom Arthur"));
        assertThat("The names should be correctly generated in the defined order", names.get(8), is("Brian Chris"));
        assertThat("The names should be correctly generated in the defined order", names.get(9), is("Chris Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(10), is("Doom Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(11), is("Doom Chris"));
    }

    @Test
    public void shouldTryAllCombinationsOfFourFirstNames() {
        List<String> names = NameMatchingCandidatesGenerator.generateCandidates("Arthur Brian Chris Daniel", "Eccles");

        assertThat("The number of generated names should be as expected", names.size(), is(20));
        assertThat("The names should be correctly generated in the defined order",names.get(0), is("Arthur Eccles"));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is("Brian Eccles"));
        assertThat("The names should be correctly generated in the defined order", names.get(2), is("Chris Eccles"));
        assertThat("The names should be correctly generated in the defined order", names.get(3), is("Daniel Eccles"));
        assertThat("The names should be correctly generated in the defined order", names.get(4), is("Arthur Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(5), is("Arthur Chris"));
        assertThat("The names should be correctly generated in the defined order", names.get(6), is("Arthur Daniel"));
        assertThat("The names should be correctly generated in the defined order", names.get(7), is("Brian Arthur"));
        assertThat("The names should be correctly generated in the defined order", names.get(8), is("Brian Chris"));
        assertThat("The names should be correctly generated in the defined order", names.get(9), is("Brian Daniel"));
        assertThat("The names should be correctly generated in the defined order", names.get(10), is("Chris Arthur"));
        assertThat("The names should be correctly generated in the defined order", names.get(11), is("Chris Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(12), is("Chris Daniel"));
        assertThat("The names should be correctly generated in the defined order", names.get(13), is("Daniel Arthur"));
        assertThat("The names should be correctly generated in the defined order", names.get(14), is("Daniel Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(15), is("Daniel Chris"));
        assertThat("The names should be correctly generated in the defined order", names.get(16), is("Eccles Arthur"));
        assertThat("The names should be correctly generated in the defined order", names.get(17), is("Eccles Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(18), is("Eccles Chris"));
        assertThat("The names should be correctly generated in the defined order", names.get(19), is("Eccles Daniel"));
    }

    @Test
    public void shouldHanndleExtraWhitespace() {
        List<String> names = NameMatchingCandidatesGenerator.generateCandidates(" Arthur   Brian   ", " Coates ");

        assertThat("The number of generated names should be as expected", names.size(), is(6));
        assertThat("The names should be correctly generated in the defined order",names.get(0), is("Arthur Coates"));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is("Brian Coates"));
        assertThat("The names should be correctly generated in the defined order", names.get(2), is("Coates Arthur"));
        assertThat("The names should be correctly generated in the defined order", names.get(3), is("Coates Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(4), is("Arthur Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(5), is("Brian Arthur"));
    }

    @Test
    public void shouldHandleMultipleLastNames() {
        List<String> names = NameMatchingCandidatesGenerator.generateCandidates("Arthur", "Brian Coates");

        assertThat("The number of generated names should be as expected", names.size(), is(6));
        assertThat("The names should be correctly generated in the defined order",names.get(0), is("Arthur Coates"));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is("Brian Coates"));
        assertThat("The names should be correctly generated in the defined order", names.get(2), is("Coates Arthur"));
        assertThat("The names should be correctly generated in the defined order", names.get(3), is("Coates Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(4), is("Arthur Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(5), is("Brian Arthur"));

    }

    @Test
    public void shouldHandleHyphenatedNames() {
        List<String> names = NameMatchingCandidatesGenerator.generateCandidates("Arthur-Brian", "Coates");

        assertThat("The number of generated names should be as expected", names.size(), is(8));
        assertThat("The names should be correctly generated in the defined order", names.get(0), is("ArthurBrian Coates"));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is("Coates ArthurBrian"));
        assertThat("The names should be correctly generated in the defined order", names.get(2), is("Arthur Coates"));
        assertThat("The names should be correctly generated in the defined order", names.get(3), is("Brian Coates"));
        assertThat("The names should be correctly generated in the defined order", names.get(4), is("Coates Arthur"));
        assertThat("The names should be correctly generated in the defined order", names.get(5), is("Coates Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(6), is("Arthur Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(7), is("Brian Arthur"));
    }

    @Test
    public void shouldHandleApostrophedNames() {
        List<String> names = NameMatchingCandidatesGenerator.generateCandidates("Arthur", "O'Bobbins");

        assertThat("The number of generated names should be as expected", names.size(), is(8));
        assertThat("The names should be correctly generated in the defined order", names.get(0), is("Arthur OBobbins"));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is("OBobbins Arthur"));
        assertThat("The names should be correctly generated in the defined order", names.get(2), is("Arthur Bobbins"));
        assertThat("The names should be correctly generated in the defined order", names.get(3), is("O Bobbins"));
        assertThat("The names should be correctly generated in the defined order", names.get(4), is("Bobbins Arthur"));
        assertThat("The names should be correctly generated in the defined order", names.get(5), is("Bobbins O"));
        assertThat("The names should be correctly generated in the defined order", names.get(6), is("Arthur O"));
        assertThat("The names should be correctly generated in the defined order", names.get(7), is("O Arthur"));
    }

    @Test
    public void shouldHandleHyphensAndApostrophes() {
        List<String> names = NameMatchingCandidatesGenerator.generateCandidates("Arthur-Brian", "O'Coates");

        assertThat("The number of generated names should be as expected", names.size(), is(14));
        assertThat("The names should be correctly generated in the defined order", names.get(0), is("ArthurBrian OCoates"));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is("OCoates ArthurBrian"));

        assertThat("The names should be correctly generated in the defined order", names.get(2), is("Arthur Coates"));
        assertThat("The names should be correctly generated in the defined order", names.get(3), is("Brian Coates"));
        assertThat("The names should be correctly generated in the defined order", names.get(4), is("O Coates"));
        assertThat("The names should be correctly generated in the defined order", names.get(5), is("Arthur Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(6), is("Arthur O"));
        assertThat("The names should be correctly generated in the defined order", names.get(7), is("Brian Arthur"));
        assertThat("The names should be correctly generated in the defined order", names.get(8), is("O Arthur"));
        assertThat("The names should be correctly generated in the defined order", names.get(9), is("Coates Arthur"));
        assertThat("The names should be correctly generated in the defined order", names.get(10), is("Brian O"));
        assertThat("The names should be correctly generated in the defined order", names.get(11), is("O Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(12), is("Coates Brian"));
        assertThat("The names should be correctly generated in the defined order", names.get(13), is("Coates O"));
    }

    @Test
    @Ignore
    public void thatDiacriticsAreReplaced() {
        List<String> names = NameMatchingCandidatesGenerator.generateCandidates("Ibanez", "Higuaín");

        assertThat("The number of generated names should be as expected", names.size(), is(2));
        assertThat("The names should have the diacritics removed", names.get(0), is("Ibanez Higuain"));
        assertThat("The names should have the diacritics removed", names.get(1), is("Higuain Ibanez"));
    }
}
