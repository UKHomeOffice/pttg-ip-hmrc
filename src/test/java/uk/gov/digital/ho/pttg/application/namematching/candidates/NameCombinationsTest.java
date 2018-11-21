package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class NameCombinationsTest {

    private NameCombinations nameCombinations = new NameCombinations();

    @Test
    public void shouldReturnEmptyListWhenAliasSurnames() {
        InputNames inputNamesWithAlias = new InputNames("A B", "C", "Alias");

        assertThat(nameCombinations.generateCandidates(inputNamesWithAlias, inputNamesWithAlias)).isEqualTo(emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldErrorIfNoName() {
        nameCombinations.generateCandidates(new InputNames("", ""), new InputNames("", ""));
    }

    @Test
    public void shouldDuplicateSingleFirstName() {
        List<CandidateName> names = nameCombinations.generateCandidates(new InputNames("Mono", ""), new InputNames("Mono", ""));

        assertThat(names.size()).isEqualTo(1);
        assertThat(names.get(0)).isEqualTo(new CandidateName("Mono", "Mono"));
    }

    @Test
    public void shouldDuplicateSingleLastName() {
        List<CandidateName> names = nameCombinations.generateCandidates(new InputNames("", "Mono"), new InputNames("", "Mono"));

        assertThat(names.size()).isEqualTo(1);
        assertThat(names.get(0)).isEqualTo(new CandidateName("Mono", "Mono"));
    }

    @Test
    public void shouldSwitchSingleFirstAndLastName() {
        List<CandidateName> names = nameCombinations.generateCandidates(new InputNames("Arthur", "Bobbins"), new InputNames("Arthur", "Bobbins"));

        assertThat(names.size()).isEqualTo(2);
        assertThat(names.get(0)).isEqualTo(new CandidateName("Arthur", "Bobbins"));
        assertThat(names.get(1)).isEqualTo(new CandidateName("Bobbins", "Arthur"));
    }

    @Test
    public void shouldTryAllCombinationsOfTwoFirstNames() {
        List<CandidateName> names = nameCombinations.generateCandidates(new InputNames("Arthur Brian", "Coates"), new InputNames("Arthur Brian", "Coates"));

        assertThat(names.size()).isEqualTo(6);
        assertThat(names.get(0)).isEqualTo(new CandidateName("Arthur", "Coates"));
        assertThat(names.get(1)).isEqualTo(new CandidateName("Brian", "Coates"));
        assertThat(names.get(2)).isEqualTo(new CandidateName("Coates", "Arthur"));
        assertThat(names.get(3)).isEqualTo(new CandidateName("Coates", "Brian"));
        assertThat(names.get(4)).isEqualTo(new CandidateName("Arthur", "Brian"));
        assertThat(names.get(5)).isEqualTo(new CandidateName("Brian", "Arthur"));
    }

    @Test
    public void shouldTryAllCombinationsOfThreeFirstNames() {
        List<CandidateName> names = nameCombinations.generateCandidates(new InputNames("Arthur Brian Chris", "Doom"), new InputNames("Arthur Brian Chris", "Doom"));

        assertThat(names.size()).isEqualTo(12);
        assertThat(names.get(0)).isEqualTo(new CandidateName("Arthur", "Doom"));
        assertThat(names.get(1)).isEqualTo(new CandidateName("Brian", "Doom"));
        assertThat(names.get(2)).isEqualTo(new CandidateName("Chris", "Doom"));
        assertThat(names.get(3)).isEqualTo(new CandidateName("Arthur", "Brian"));
        assertThat(names.get(4)).isEqualTo(new CandidateName("Arthur", "Chris"));
        assertThat(names.get(5)).isEqualTo(new CandidateName("Brian", "Arthur"));
        assertThat(names.get(6)).isEqualTo(new CandidateName("Chris", "Arthur"));
        assertThat(names.get(7)).isEqualTo(new CandidateName("Doom", "Arthur"));
        assertThat(names.get(8)).isEqualTo(new CandidateName("Brian", "Chris"));
        assertThat(names.get(9)).isEqualTo(new CandidateName("Chris", "Brian"));
        assertThat(names.get(10)).isEqualTo(new CandidateName("Doom", "Brian"));
        assertThat(names.get(11)).isEqualTo(new CandidateName("Doom", "Chris"));
    }

    @Test
    public void shouldTryAllCombinationsOfFourFirstNames() {
        List<CandidateName> names = nameCombinations.generateCandidates(new InputNames("Arthur Brian Chris Daniel", "Eccles"), new InputNames("Arthur Brian Chris Daniel", "Eccles"));

        assertThat(names.size()).isEqualTo(20);
        assertThat(names.get(0)).isEqualTo(new CandidateName("Arthur", "Eccles"));
        assertThat(names.get(1)).isEqualTo(new CandidateName("Brian", "Eccles"));
        assertThat(names.get(2)).isEqualTo(new CandidateName("Chris", "Eccles"));
        assertThat(names.get(3)).isEqualTo(new CandidateName("Daniel", "Eccles"));
        assertThat(names.get(4)).isEqualTo(new CandidateName("Arthur", "Brian"));
        assertThat(names.get(5)).isEqualTo(new CandidateName("Arthur", "Chris"));
        assertThat(names.get(6)).isEqualTo(new CandidateName("Arthur", "Daniel"));
        assertThat(names.get(7)).isEqualTo(new CandidateName("Brian", "Arthur"));
        assertThat(names.get(8)).isEqualTo(new CandidateName("Brian", "Chris"));
        assertThat(names.get(9)).isEqualTo(new CandidateName("Brian", "Daniel"));
        assertThat(names.get(10)).isEqualTo(new CandidateName("Chris", "Arthur"));
        assertThat(names.get(11)).isEqualTo(new CandidateName("Chris", "Brian"));
        assertThat(names.get(12)).isEqualTo(new CandidateName("Chris", "Daniel"));
        assertThat(names.get(13)).isEqualTo(new CandidateName("Daniel", "Arthur"));
        assertThat(names.get(14)).isEqualTo(new CandidateName("Daniel", "Brian"));
        assertThat(names.get(15)).isEqualTo(new CandidateName("Daniel", "Chris"));
        assertThat(names.get(16)).isEqualTo(new CandidateName("Eccles", "Arthur"));
        assertThat(names.get(17)).isEqualTo(new CandidateName("Eccles", "Brian"));
        assertThat(names.get(18)).isEqualTo(new CandidateName("Eccles", "Chris"));
        assertThat(names.get(19)).isEqualTo(new CandidateName("Eccles", "Daniel"));
    }

    @Test
    public void shouldHandleExtraWhitespace() {
        List<CandidateName> names = nameCombinations.generateCandidates(new InputNames(" Arthur   Brian   ", " Coates "), new InputNames(" Arthur   Brian   ", " Coates "));

        assertThat(names.size()).isEqualTo(6);
        assertThat(names.get(0)).isEqualTo(new CandidateName("Arthur", "Coates"));
        assertThat(names.get(1)).isEqualTo(new CandidateName("Brian", "Coates"));
        assertThat(names.get(2)).isEqualTo(new CandidateName("Coates", "Arthur"));
        assertThat(names.get(3)).isEqualTo(new CandidateName("Coates", "Brian"));
        assertThat(names.get(4)).isEqualTo(new CandidateName("Arthur", "Brian"));
        assertThat(names.get(5)).isEqualTo(new CandidateName("Brian", "Arthur"));
    }

    @Test
    public void shouldHandleMultipleLastNames() {
        List<CandidateName> names = nameCombinations.generateCandidates(new InputNames("Arthur", "Brian Coates"), new InputNames("Arthur", "Brian Coates"));

        assertThat(names.size()).isEqualTo(6);
        assertThat(names.get(0)).isEqualTo(new CandidateName("Arthur", "Coates"));
        assertThat(names.get(1)).isEqualTo(new CandidateName("Brian", "Coates"));
        assertThat(names.get(2)).isEqualTo(new CandidateName("Coates", "Arthur"));
        assertThat(names.get(3)).isEqualTo(new CandidateName("Coates", "Brian"));
        assertThat(names.get(4)).isEqualTo(new CandidateName("Arthur", "Brian"));
        assertThat(names.get(5)).isEqualTo(new CandidateName("Brian", "Arthur"));

    }
}
