package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.CandidateFunctions.removeAdditionalNamesIfOverMax;

@RunWith(JUnit4.class)
public class CandidateFunctionsTest {

    @Test
    public void removeAdditionalNamesOverMax_belowMax() {
        InputNames afterRemoved = removeAdditionalNamesIfOverMax(new InputNames("one two three four", "five six seven"), 7, 3);

        assertThat(afterRemoved.size()).isEqualTo(7);
        assertThat(afterRemoved.rawAllNames().get(0)).isEqualTo("one");
        assertThat(afterRemoved.rawAllNames().get(3)).isEqualTo("four");
        assertThat(afterRemoved.rawAllNames().get(4)).isEqualTo("five");
        assertThat(afterRemoved.rawAllNames().get(6)).isEqualTo("seven");
    }

    @Test
    public void removeAdditionalNamesOverMax_overMax() {
        InputNames afterRemoved = removeAdditionalNamesIfOverMax(new InputNames("one two three four extra-one", "extra-two five six seven"), 7, 3);

        assertThat(afterRemoved.size()).isEqualTo(7);
        assertThat(afterRemoved.rawAllNames().get(0)).isEqualTo("one");
        assertThat(afterRemoved.rawAllNames().get(3)).isEqualTo("four");
        assertThat(afterRemoved.rawAllNames().get(4)).isEqualTo("five");
        assertThat(afterRemoved.rawAllNames().get(6)).isEqualTo("seven");
    }
}
