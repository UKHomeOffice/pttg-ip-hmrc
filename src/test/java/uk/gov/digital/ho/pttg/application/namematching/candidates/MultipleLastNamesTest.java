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
public class MultipleLastNamesTest {

    private static final String INCORRECT_ORDER = "The names should be correctly generated in the defined order";
    private static final String INCORRECT_NUMBER_OF_GENERATED_NAMES = "The number of generated names should be as expected";

    private MultipleLastNames multipleLastNames = new MultipleLastNames();

    @Test
    public void shouldGenerateCorrectSurnameCombinationsForMultipleSurnames() {

        List<CandidateName> names = multipleLastNames.generateCandidates(new InputNames("A B C", "D E F"), new InputNames("A B C", "D E F"));
        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(18));

        assertThat(INCORRECT_ORDER, names.get(0), is(new CandidateName("A", "D E")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new CandidateName("A", "D F")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new CandidateName("A", "E D")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new CandidateName("A", "E F")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new CandidateName("A", "F D")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new CandidateName("A", "F E")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new CandidateName("B", "D E")));
        assertThat(INCORRECT_ORDER, names.get(7), is(new CandidateName("B", "D F")));
        assertThat(INCORRECT_ORDER, names.get(8), is(new CandidateName("B", "E D")));
        assertThat(INCORRECT_ORDER, names.get(9), is(new CandidateName("B", "E F")));
        assertThat(INCORRECT_ORDER, names.get(10), is(new CandidateName("B", "F D")));
        assertThat(INCORRECT_ORDER, names.get(11), is(new CandidateName("B", "F E")));
        assertThat(INCORRECT_ORDER, names.get(12), is(new CandidateName("C", "D E")));
        assertThat(INCORRECT_ORDER, names.get(13), is(new CandidateName("C", "D F")));
        assertThat(INCORRECT_ORDER, names.get(14), is(new CandidateName("C", "E D")));
        assertThat(INCORRECT_ORDER, names.get(15), is(new CandidateName("C", "E F")));
        assertThat(INCORRECT_ORDER, names.get(16), is(new CandidateName("C", "F D")));
        assertThat(INCORRECT_ORDER, names.get(17), is(new CandidateName("C", "F E")));
    }
}
