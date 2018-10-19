package uk.gov.digital.ho.pttg.application.namematching;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.InputNamesFunctions.splitIntoDistinctNames;

@RunWith(MockitoJUnitRunner.class)
public class InputNamesFunctionsTest {

    @Test
    public void splitIntoDistinctNames_singleName() {
        List<String> splitNames = splitIntoDistinctNames("single");

        assertThat(splitNames.size()).isEqualTo(1);
        assertThat(splitNames.get(0)).isEqualTo("single");
    }

    @Test
    public void splitIntoDistinctNames_twoName() {
        List<String> splitNames = splitIntoDistinctNames("first second");

        assertThat(splitNames.size()).isEqualTo(2);
        assertThat(splitNames.get(0)).isEqualTo("first");
        assertThat(splitNames.get(1)).isEqualTo("second");
    }

    @Test
    public void splitIntoDistinctNames_severalNames() {
        List<String> splitNames = splitIntoDistinctNames("first second thrird fourth fifth sixth");

        assertThat(splitNames.size()).isEqualTo(6);
        assertThat(splitNames.get(0)).isEqualTo("first");
        assertThat(splitNames.get(1)).isEqualTo("second");
        assertThat(splitNames.get(4)).isEqualTo("fifth");
        assertThat(splitNames.get(5)).isEqualTo("sixth");
    }

}
