package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.application.namematching.PersonName;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class NameCombinationsTest {

    private static final String INCORRECT_ORDER = "The names should be correctly generated in the defined order";
    private static final String INCORRECT_NUMBER_OF_GENERATED_NAMES = "The number of generated names should be as expected";

    private NameCombinations nameCombinations = new NameCombinations();

    @Test(expected = IllegalArgumentException.class)
    public void shouldErrorIfNoName() {
        nameCombinations.generateCandidates("", "");
    }

    @Test
    public void shouldDuplicateSingleFirstName() {
        List<PersonName> names = nameCombinations.generateCandidates("Mono", "");

        assertThat("There should be a single name", names.size(), is(1));
        assertThat("The first name should be duplicated", names.get(0), is(new PersonName("Mono", "Mono")));
    }

    @Test
    public void shouldDuplicateSingleLastName() {
        List<PersonName> names = nameCombinations.generateCandidates("", "Mono");

        assertThat("There should be a single name", names.size(), is(1));
        assertThat("The last name should be duplicated", names.get(0), is(new PersonName("Mono", "Mono")));
    }

    @Test
    public void shouldSwitchSingleFirstAndLastName() {
        List<PersonName> names = nameCombinations.generateCandidates("Arthur", "Bobbins");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(2));
        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur", "Bobbins")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("Bobbins", "Arthur")));
    }

    @Test
    public void shouldTryAllCombinationsOfTwoFirstNames() {
        List<PersonName> names = nameCombinations.generateCandidates("Arthur Brian", "Coates");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(6));
        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("Coates", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("Brian", "Arthur")));
    }

    @Test
    public void shouldTryAllCombinationsOfThreeFirstNames() {
        List<PersonName> names = nameCombinations.generateCandidates("Arthur Brian Chris", "Doom");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(12));
        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur", "Doom")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("Brian", "Doom")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("Chris", "Doom")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("Arthur", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("Brian", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new PersonName("Chris", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(7), is(new PersonName("Doom", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(8), is(new PersonName("Brian", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(9), is(new PersonName("Chris", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(10), is(new PersonName("Doom", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(11), is(new PersonName("Doom", "Chris")));
    }

    @Test
    public void shouldTryAllCombinationsOfFourFirstNames() {
        List<PersonName> names = nameCombinations.generateCandidates("Arthur Brian Chris Daniel", "Eccles");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(20));
        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur", "Eccles")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("Brian", "Eccles")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("Chris", "Eccles")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Daniel", "Eccles")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("Arthur", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new PersonName("Arthur", "Daniel")));
        assertThat(INCORRECT_ORDER, names.get(7), is(new PersonName("Brian", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(8), is(new PersonName("Brian", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(9), is(new PersonName("Brian", "Daniel")));
        assertThat(INCORRECT_ORDER, names.get(10), is(new PersonName("Chris", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(11), is(new PersonName("Chris", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(12), is(new PersonName("Chris", "Daniel")));
        assertThat(INCORRECT_ORDER, names.get(13), is(new PersonName("Daniel", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(14), is(new PersonName("Daniel", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(15), is(new PersonName("Daniel", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(16), is(new PersonName("Eccles", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(17), is(new PersonName("Eccles", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(18), is(new PersonName("Eccles", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(19), is(new PersonName("Eccles", "Daniel")));
    }

    @Test
    public void shouldHandleExtraWhitespace() {
        List<PersonName> names = nameCombinations.generateCandidates(" Arthur   Brian   ", " Coates ");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(6));
        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("Coates", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("Brian", "Arthur")));
    }

    @Test
    public void shouldHandleMultipleLastNames() {
        List<PersonName> names = nameCombinations.generateCandidates("Arthur", "Brian Coates");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(6));
        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("Coates", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("Brian", "Arthur")));

    }
}
