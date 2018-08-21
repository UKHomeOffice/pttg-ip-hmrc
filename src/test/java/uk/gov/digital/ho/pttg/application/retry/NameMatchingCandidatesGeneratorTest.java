package uk.gov.digital.ho.pttg.application.retry;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.application.namematching.NameMatchingCandidatesGenerator;
import uk.gov.digital.ho.pttg.application.namematching.PersonName;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class NameMatchingCandidatesGeneratorTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldErrorIfNoName() {
        NameMatchingCandidatesGenerator.generateCandidateNames("", "");
    }

    @Test
    public void shouldDuplicateSingleFirstName() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Mono", "");

        assertThat("There should be a single name", names.size(), is(1));
        assertThat("The first name should be duplicated", names.get(0), is(new PersonName("Mono", "Mono")));
    }

    @Test
    public void shouldDuplicateSingleLastName() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("", "Mono");

        assertThat("There should be a single name", names.size(), is(1));
        assertThat("The last name should be duplicated", names.get(0), is(new PersonName("Mono", "Mono")));
    }

    @Test
    public void shouldSwitchSingleFirstAndLastName() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Arthur", "Bobbins");

        assertThat("The number of generated names should be as expected", names.size(), is(2));
        assertThat("The names should be correctly generated in the defined order", names.get(0), is(new PersonName("Arthur", "Bobbins")));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is(new PersonName("Bobbins", "Arthur")));
    }

    @Test
    public void shouldTryAllCombinationsOfTwoFirstNames() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Arthur Brian", "Coates");

        assertThat("The number of generated names should be as expected", names.size(), is(6));
        assertThat("The names should be correctly generated in the defined order", names.get(0), is(new PersonName("Arthur", "Coates")));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is(new PersonName("Brian", "Coates")));
        assertThat("The names should be correctly generated in the defined order", names.get(2), is(new PersonName("Coates", "Arthur")));
        assertThat("The names should be correctly generated in the defined order", names.get(3), is(new PersonName("Coates", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(4), is(new PersonName("Arthur", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(5), is(new PersonName("Brian", "Arthur")));
    }

    @Test
    public void shouldTryAllCombinationsOfThreeFirstNames() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Arthur Brian Chris", "Doom");

        assertThat("The number of generated names should be as expected", names.size(), is(12));
        assertThat("The names should be correctly generated in the defined order", names.get(0), is(new PersonName("Arthur", "Doom")));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is(new PersonName("Brian", "Doom")));
        assertThat("The names should be correctly generated in the defined order", names.get(2), is(new PersonName("Chris", "Doom")));
        assertThat("The names should be correctly generated in the defined order", names.get(3), is(new PersonName("Arthur", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(4), is(new PersonName("Arthur", "Chris")));
        assertThat("The names should be correctly generated in the defined order", names.get(5), is(new PersonName("Brian", "Arthur")));
        assertThat("The names should be correctly generated in the defined order", names.get(6), is(new PersonName("Chris", "Arthur")));
        assertThat("The names should be correctly generated in the defined order", names.get(7), is(new PersonName("Doom", "Arthur")));
        assertThat("The names should be correctly generated in the defined order", names.get(8), is(new PersonName("Brian", "Chris")));
        assertThat("The names should be correctly generated in the defined order", names.get(9), is(new PersonName("Chris", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(10), is(new PersonName("Doom", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(11), is(new PersonName("Doom", "Chris")));
    }

    @Test
    public void shouldTryAllCombinationsOfFourFirstNames() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Arthur Brian Chris Daniel", "Eccles");

        assertThat("The number of generated names should be as expected", names.size(), is(20));
        assertThat("The names should be correctly generated in the defined order", names.get(0), is(new PersonName("Arthur", "Eccles")));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is(new PersonName("Brian", "Eccles")));
        assertThat("The names should be correctly generated in the defined order", names.get(2), is(new PersonName("Chris", "Eccles")));
        assertThat("The names should be correctly generated in the defined order", names.get(3), is(new PersonName("Daniel", "Eccles")));
        assertThat("The names should be correctly generated in the defined order", names.get(4), is(new PersonName("Arthur", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(5), is(new PersonName("Arthur", "Chris")));
        assertThat("The names should be correctly generated in the defined order", names.get(6), is(new PersonName("Arthur", "Daniel")));
        assertThat("The names should be correctly generated in the defined order", names.get(7), is(new PersonName("Brian", "Arthur")));
        assertThat("The names should be correctly generated in the defined order", names.get(8), is(new PersonName("Brian", "Chris")));
        assertThat("The names should be correctly generated in the defined order", names.get(9), is(new PersonName("Brian", "Daniel")));
        assertThat("The names should be correctly generated in the defined order", names.get(10), is(new PersonName("Chris", "Arthur")));
        assertThat("The names should be correctly generated in the defined order", names.get(11), is(new PersonName("Chris", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(12), is(new PersonName("Chris", "Daniel")));
        assertThat("The names should be correctly generated in the defined order", names.get(13), is(new PersonName("Daniel", "Arthur")));
        assertThat("The names should be correctly generated in the defined order", names.get(14), is(new PersonName("Daniel", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(15), is(new PersonName("Daniel", "Chris")));
        assertThat("The names should be correctly generated in the defined order", names.get(16), is(new PersonName("Eccles", "Arthur")));
        assertThat("The names should be correctly generated in the defined order", names.get(17), is(new PersonName("Eccles", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(18), is(new PersonName("Eccles", "Chris")));
        assertThat("The names should be correctly generated in the defined order", names.get(19), is(new PersonName("Eccles", "Daniel")));
    }

    @Test
    public void shouldHanndleExtraWhitespace() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames(" Arthur   Brian   ", " Coates ");

        assertThat("The number of generated names should be as expected", names.size(), is(6));
        assertThat("The names should be correctly generated in the defined order", names.get(0), is(new PersonName("Arthur", "Coates")));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is(new PersonName("Brian", "Coates")));
        assertThat("The names should be correctly generated in the defined order", names.get(2), is(new PersonName("Coates", "Arthur")));
        assertThat("The names should be correctly generated in the defined order", names.get(3), is(new PersonName("Coates", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(4), is(new PersonName("Arthur", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(5), is(new PersonName("Brian", "Arthur")));
    }

    @Test
    public void shouldHandleMultipleLastNames() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Arthur", "Brian Coates");

        assertThat("The number of generated names should be as expected", names.size(), is(7));
        assertThat("The surname is used unsplit for the first permutation", names.get(0), is(new PersonName("Arthur", "Brian Coates")));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is(new PersonName("Arthur", "Coates")));
        assertThat("The names should be correctly generated in the defined order", names.get(2), is(new PersonName("Brian", "Coates")));
        assertThat("The names should be correctly generated in the defined order", names.get(3), is(new PersonName("Coates", "Arthur")));
        assertThat("The names should be correctly generated in the defined order", names.get(4), is(new PersonName("Coates", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(5), is(new PersonName("Arthur", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(6), is(new PersonName("Brian", "Arthur")));

    }

    @Test
    public void shouldHandleHyphenatedNames() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Arthur-Brian", "Coates");

        assertThat("The number of generated names should be as expected", names.size(), is(8));
        assertThat("The names should be correctly generated in the defined order", names.get(0), is(new PersonName("ArthurBrian", "Coates")));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is(new PersonName("Coates", "ArthurBrian")));
        assertThat("The names should be correctly generated in the defined order", names.get(2), is(new PersonName("Arthur", "Coates")));
        assertThat("The names should be correctly generated in the defined order", names.get(3), is(new PersonName("Brian", "Coates")));
        assertThat("The names should be correctly generated in the defined order", names.get(4), is(new PersonName("Coates", "Arthur")));
        assertThat("The names should be correctly generated in the defined order", names.get(5), is(new PersonName("Coates", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(6), is(new PersonName("Arthur", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(7), is(new PersonName("Brian", "Arthur")));
    }

    @Test
    public void shouldHandleApostrophedNames() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Arthur", "O'Bobbins");

        assertThat("The number of generated names should be as expected", names.size(), is(9));
        assertThat("The names should be correctly generated in the defined order", names.get(0), is(new PersonName("Arthur", "O Bobbins")));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is(new PersonName("Arthur", "OBobbins")));
        assertThat("The names should be correctly generated in the defined order", names.get(2), is(new PersonName("OBobbins", "Arthur")));
        assertThat("The names should be correctly generated in the defined order", names.get(3), is(new PersonName("Arthur", "Bobbins")));
        assertThat("The names should be correctly generated in the defined order", names.get(4), is(new PersonName("O", "Bobbins")));
        assertThat("The names should be correctly generated in the defined order", names.get(5), is(new PersonName("Bobbins", "Arthur")));
        assertThat("The names should be correctly generated in the defined order", names.get(6), is(new PersonName("Bobbins", "O")));
        assertThat("The names should be correctly generated in the defined order", names.get(7), is(new PersonName("Arthur", "O")));
        assertThat("The names should be correctly generated in the defined order", names.get(8), is(new PersonName("O", "Arthur")));
    }

    @Test
    public void shouldHandleHyphensAndApostrophes() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Arthur-Brian", "O'Coates");

        assertThat("The number of generated names should be as expected", names.size(), is(16));
        assertThat("The names should be correctly generated in the defined order", names.get(0), is(new PersonName("Arthur", "O Coates")));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is(new PersonName("Brian", "O Coates")));
        assertThat("The names should be correctly generated in the defined order", names.get(2), is(new PersonName("ArthurBrian", "OCoates")));
        assertThat("The names should be correctly generated in the defined order", names.get(3), is(new PersonName("OCoates", "ArthurBrian")));

        assertThat("The names should be correctly generated in the defined order", names.get(4), is(new PersonName("Arthur", "Coates")));
        assertThat("The names should be correctly generated in the defined order", names.get(5), is(new PersonName("Brian", "Coates")));
        assertThat("The names should be correctly generated in the defined order", names.get(6), is(new PersonName("O", "Coates")));
        assertThat("The names should be correctly generated in the defined order", names.get(7), is(new PersonName("Arthur", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(8), is(new PersonName("Arthur", "O")));
        assertThat("The names should be correctly generated in the defined order", names.get(9), is(new PersonName("Brian", "Arthur")));
        assertThat("The names should be correctly generated in the defined order", names.get(10), is(new PersonName("O", "Arthur")));
        assertThat("The names should be correctly generated in the defined order", names.get(11), is(new PersonName("Coates", "Arthur")));
        assertThat("The names should be correctly generated in the defined order", names.get(12), is(new PersonName("Brian", "O")));
        assertThat("The names should be correctly generated in the defined order", names.get(13), is(new PersonName("O", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(14), is(new PersonName("Coates", "Brian")));
        assertThat("The names should be correctly generated in the defined order", names.get(15), is(new PersonName("Coates", "O")));
    }

    @Test
    public void shouldUseFirstFourAndLastThreeNamesIfOverSevenProvided() {
        List<PersonName> candidateNames = NameMatchingCandidatesGenerator.generateCandidateNames("A B C D E", "F G H");

        assertThat(candidateNames.size(), is(47));

        candidateNames = candidateNames.stream().
                filter(name -> !name.getSurname().equals("F G H")).
                collect(Collectors.toList());

        assertThat(candidateNames.size(), is(42)); // Expect there to be 5 candidates of retained first names paired with the entire surname "F G H"

        for (PersonName name : candidateNames) {
            assertThat(name.getFirstName().contains("E"), is(false));
            assertThat(name.getSurname().contains("E"), is(false));
        }
    }

    @Test
    public void shouldOnlyUseFirstSixFirstNamesWithMultipleWordSurname() {
        List<PersonName> candidateNames = NameMatchingCandidatesGenerator.generateCandidateNames("A B C D E F G", "Van Halen");

        List<PersonName> expectedCandidateNames = Arrays.asList(
                new PersonName("A", "Van Halen"),
                new PersonName("B", "Van Halen"),
                new PersonName("C", "Van Halen"),
                new PersonName("D", "Van Halen"),
                new PersonName("E", "Van Halen"),
                new PersonName("F", "Van Halen")
        );

        for (PersonName expectedCandidateName : expectedCandidateNames) {
            assertThat(candidateNames.contains(expectedCandidateName), is(true));
        }

        assertThat(candidateNames.contains(new PersonName("G", "Van Halen")), is(false));
    }

    @Test
    public void shouldUseWholeMultiWordSurnameWithHyphensEtc() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Bob-Brian", "Hill O'Coates-Smith");

        assertThat("The names should be correctly generated in the defined order", names.get(0), is(new PersonName("BobBrian", "Hill OCoatesSmith")));
        assertThat("The names should be correctly generated in the defined order", names.get(1), is(new PersonName("Bob", "Hill O Coates Smith")));
        assertThat("The names should be correctly generated in the defined order", names.get(2), is(new PersonName("Brian", "Hill O Coates Smith")));

        Assertions.assertThat(names).doesNotHaveDuplicates();
    }
}
