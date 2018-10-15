package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.application.namematching.PersonName;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class MultipleLastNamesTest {

    private static final String INCORRECT_ORDER = "The names should be correctly generated in the defined order";
    private static final String INCORRECT_NUMBER_OF_GENERATED_NAMES = "The number of generated names should be as expected";

    private MultipleLastNames multipleLastNames = new MultipleLastNames();

    @Test
    public void shouldGenerateCorrectSurnameCombinationsForMultipleSurnames() {

        List<PersonName> names = multipleLastNames.generateCandidates("A B C", "D E F");
        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(22));

        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("A B C", "D E F")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("A", "D E")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("A", "D F")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("A", "E D")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("A", "E F")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new PersonName("A", "F D")));
        assertThat(INCORRECT_ORDER, names.get(7), is(new PersonName("A", "F E")));
        assertThat(INCORRECT_ORDER, names.get(8), is(new PersonName("B", "D E F")));
        assertThat(INCORRECT_ORDER, names.get(9), is(new PersonName("B", "D E")));
        assertThat(INCORRECT_ORDER, names.get(10), is(new PersonName("B", "D F")));
        assertThat(INCORRECT_ORDER, names.get(11), is(new PersonName("B", "E D")));
        assertThat(INCORRECT_ORDER, names.get(12), is(new PersonName("B", "E F")));
        assertThat(INCORRECT_ORDER, names.get(13), is(new PersonName("B", "F D")));
        assertThat(INCORRECT_ORDER, names.get(14), is(new PersonName("B", "F E")));
        assertThat(INCORRECT_ORDER, names.get(15), is(new PersonName("C", "D E F")));
        assertThat(INCORRECT_ORDER, names.get(16), is(new PersonName("C", "D E")));
        assertThat(INCORRECT_ORDER, names.get(17), is(new PersonName("C", "D F")));
        assertThat(INCORRECT_ORDER, names.get(18), is(new PersonName("C", "E D")));
        assertThat(INCORRECT_ORDER, names.get(19), is(new PersonName("C", "E F")));
        assertThat(INCORRECT_ORDER, names.get(20), is(new PersonName("C", "F D")));
        assertThat(INCORRECT_ORDER, names.get(21), is(new PersonName("C", "F E")));
    }
}
