package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

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
        nameCombinations.generateCandidates(new InputNames("", ""));
    }

    @Test
    public void shouldDuplicateSingleFirstName() {
        List<CandidateName> names = nameCombinations.generateCandidates(new InputNames("Mono", ""));

        assertThat("There should be a single name", names.size(), is(1));
        assertThat("The first name should be duplicated", names.get(0), is(new CandidateName("Mono", "Mono")));
    }

    @Test
    public void shouldDuplicateSingleLastName() {
        List<CandidateName> names = nameCombinations.generateCandidates(new InputNames("", "Mono"));

        assertThat("There should be a single name", names.size(), is(1));
        assertThat("The last name should be duplicated", names.get(0), is(new CandidateName("Mono", "Mono")));
    }

    @Test
    public void shouldSwitchSingleFirstAndLastName() {
        List<CandidateName> names = nameCombinations.generateCandidates(new InputNames("Arthur", "Bobbins"));

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(2));
        assertThat(INCORRECT_ORDER, names.get(0), is(new CandidateName("Arthur", "Bobbins")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new CandidateName("Bobbins", "Arthur")));
    }

    @Test
    public void shouldTryAllCombinationsOfTwoFirstNames() {
        List<CandidateName> names = nameCombinations.generateCandidates(new InputNames("Arthur Brian", "Coates"));

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(6));
        assertThat(INCORRECT_ORDER, names.get(0), is(new CandidateName("Arthur", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new CandidateName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new CandidateName("Coates", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new CandidateName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new CandidateName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new CandidateName("Brian", "Arthur")));
    }

    @Test
    public void shouldTryAllCombinationsOfThreeFirstNames() {
        List<CandidateName> names = nameCombinations.generateCandidates(new InputNames("Arthur Brian Chris", "Doom"));

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(12));
        assertThat(INCORRECT_ORDER, names.get(0), is(new CandidateName("Arthur", "Doom")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new CandidateName("Brian", "Doom")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new CandidateName("Chris", "Doom")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new CandidateName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new CandidateName("Arthur", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new CandidateName("Brian", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new CandidateName("Chris", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(7), is(new CandidateName("Doom", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(8), is(new CandidateName("Brian", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(9), is(new CandidateName("Chris", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(10), is(new CandidateName("Doom", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(11), is(new CandidateName("Doom", "Chris")));
    }

    @Test
    public void shouldTryAllCombinationsOfFourFirstNames() {
        List<CandidateName> names = nameCombinations.generateCandidates(new InputNames("Arthur Brian Chris Daniel", "Eccles"));

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(20));
        assertThat(INCORRECT_ORDER, names.get(0), is(new CandidateName("Arthur", "Eccles")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new CandidateName("Brian", "Eccles")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new CandidateName("Chris", "Eccles")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new CandidateName("Daniel", "Eccles")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new CandidateName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new CandidateName("Arthur", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new CandidateName("Arthur", "Daniel")));
        assertThat(INCORRECT_ORDER, names.get(7), is(new CandidateName("Brian", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(8), is(new CandidateName("Brian", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(9), is(new CandidateName("Brian", "Daniel")));
        assertThat(INCORRECT_ORDER, names.get(10), is(new CandidateName("Chris", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(11), is(new CandidateName("Chris", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(12), is(new CandidateName("Chris", "Daniel")));
        assertThat(INCORRECT_ORDER, names.get(13), is(new CandidateName("Daniel", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(14), is(new CandidateName("Daniel", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(15), is(new CandidateName("Daniel", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(16), is(new CandidateName("Eccles", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(17), is(new CandidateName("Eccles", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(18), is(new CandidateName("Eccles", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(19), is(new CandidateName("Eccles", "Daniel")));
    }

    @Test
    public void shouldHandleExtraWhitespace() {
        List<CandidateName> names = nameCombinations.generateCandidates(new InputNames(" Arthur   Brian   ", " Coates "));

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(6));
        assertThat(INCORRECT_ORDER, names.get(0), is(new CandidateName("Arthur", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new CandidateName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new CandidateName("Coates", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new CandidateName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new CandidateName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new CandidateName("Brian", "Arthur")));
    }

    @Test
    public void shouldHandleMultipleLastNames() {
        List<CandidateName> names = nameCombinations.generateCandidates(new InputNames("Arthur", "Brian Coates"));

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(6));
        assertThat(INCORRECT_ORDER, names.get(0), is(new CandidateName("Arthur", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new CandidateName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new CandidateName("Coates", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new CandidateName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new CandidateName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new CandidateName("Brian", "Arthur")));

    }
}
