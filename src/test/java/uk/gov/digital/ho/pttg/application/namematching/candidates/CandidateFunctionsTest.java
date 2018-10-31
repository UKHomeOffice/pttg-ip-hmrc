package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.CandidateFunctions.removeAdditionalNamesIfOverMax;

@RunWith(JUnit4.class)
public class CandidateFunctionsTest {

    @Test
    public void removeAdditionalNamesOverMax_belowMax() {
        InputNames afterRemoved = removeAdditionalNamesIfOverMax(new InputNames(asList("one", "two", "three", "four"), asList("five", "six", "seven")));

        assertThat(afterRemoved.size()).isEqualTo(7);
        assertThat(afterRemoved.allNames().get(0)).isEqualTo("one");
        assertThat(afterRemoved.allNames().get(3)).isEqualTo("four");
        assertThat(afterRemoved.allNames().get(4)).isEqualTo("five");
        assertThat(afterRemoved.allNames().get(6)).isEqualTo("seven");
    }

    @Test
    public void removeAdditionalNamesOverMax_overMax() {
        InputNames afterRemoved = removeAdditionalNamesIfOverMax(new InputNames(asList("one", "two", "three", "four", "extra-one"), asList("extra-two", "five", "six", "seven")));

        assertThat(afterRemoved.size()).isEqualTo(7);
        assertThat(afterRemoved.allNames().get(0)).isEqualTo("one");
        assertThat(afterRemoved.allNames().get(3)).isEqualTo("four");
        assertThat(afterRemoved.allNames().get(4)).isEqualTo("five");
        assertThat(afterRemoved.allNames().get(6)).isEqualTo("seven");
    }
}
