package uk.gov.digital.ho.pttg.application.namematching;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class NameMatchingFunctionsTest {

    @Test
    public void splitIntoDistinctNames_singleName() {
        List<String> splitNames = NameMatchingFunctions.splitIntoDistinctNames("single");

        assertThat(splitNames.size()).isEqualTo(1);
        assertThat(splitNames.get(0)).isEqualTo("single");
    }

    @Test
    public void splitIntoDistinctNames_twoName() {
        List<String> splitNames = NameMatchingFunctions.splitIntoDistinctNames("first second");

        assertThat(splitNames.size()).isEqualTo(2);
        assertThat(splitNames.get(0)).isEqualTo("first");
        assertThat(splitNames.get(1)).isEqualTo("second");
    }

    @Test
    public void splitIntoDistinctNames_severalNames() {
        List<String> splitNames = NameMatchingFunctions.splitIntoDistinctNames("first second thrird fourth fifth sixth");

        assertThat(splitNames.size()).isEqualTo(6);
        assertThat(splitNames.get(0)).isEqualTo("first");
        assertThat(splitNames.get(1)).isEqualTo("second");
        assertThat(splitNames.get(4)).isEqualTo("fifth");
        assertThat(splitNames.get(5)).isEqualTo("sixth");
    }

    @Test
    public void splitTwoIntoDistinctNames_singleNames() {
        List<String> splitNames = NameMatchingFunctions.splitTwoIntoDistinctNames("first", "last");

        assertThat(splitNames.size()).isEqualTo(2);
        assertThat(splitNames.get(0)).isEqualTo("first");
        assertThat(splitNames.get(1)).isEqualTo("last");
    }

    @Test
    public void splitTwoIntoDistinctNames_multipleNames() {
        List<String> splitNames = NameMatchingFunctions.splitTwoIntoDistinctNames("one two three", "four five six seven");

        assertThat(splitNames.size()).isEqualTo(7);
        assertThat(splitNames.get(0)).isEqualTo("one");
        assertThat(splitNames.get(4)).isEqualTo("five");
        assertThat(splitNames.get(6)).isEqualTo("seven");
    }

}
