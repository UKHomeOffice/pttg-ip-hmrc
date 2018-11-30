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
public class EntireLastNameAndEachFirstNameTest {

    private static final String INCORRECT_ORDER = "The names should be correctly generated in the defined order";
    private static final String INCORRECT_NUMBER_OF_GENERATED_NAMES = "The number of generated names should be as expected";

    private EntireLastNameAndEachFirstName entireLastNameAndEachFirstName = new EntireLastNameAndEachFirstName();

    @Test
    public void shouldGenerateCorrectSurnameCombinationsForMultipleSurnames() {

        List<CandidateName> names = entireLastNameAndEachFirstName.generateCandidates(new InputNames("A B C", "D E F"), new InputNames("A B C", "D E F"));
        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(3));

        assertThat(INCORRECT_ORDER, names.get(0), is(new CandidateName("A", "D E F")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new CandidateName("B", "D E F")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new CandidateName("C", "D E F")));
    }
}