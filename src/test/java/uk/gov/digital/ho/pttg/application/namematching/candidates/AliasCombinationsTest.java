package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class AliasCombinationsTest {

    private AliasCombinations aliasCombinations = new AliasCombinations();

    @Test
    public void shouldReturnEmptyListWhenNoAliasSurnames() {
        InputNames inputNamesWithoutAlias = new InputNames("A B", "C", "");

        assertThat(aliasCombinations.generateCandidates(inputNamesWithoutAlias, inputNamesWithoutAlias)).isEqualTo(emptyList());
    }

    @Test
    public void shouldHaveCorrectSequenceFor2FirstNames1SurnameAnd1AliasSurname() {
        InputNames inputNames = new InputNames("A B", "C", "D");
        List<CandidateName> candidateNames = aliasCombinations.generateCandidates(inputNames, inputNames);

        assertThat(candidateNames).containsExactly(
                new CandidateName("A", "C"),
                new CandidateName("B", "C"),
                new CandidateName("A", "D"),
                new CandidateName("B", "D"),
                new CandidateName("C", "D"),
                new CandidateName("A", "B"),
                new CandidateName("B", "A"),
                new CandidateName("C", "A"),
                new CandidateName("C", "B"),
                new CandidateName("D", "A"),
                new CandidateName("D", "B"),
                new CandidateName("D", "C")
        );
    }

    @Test
    public void shouldHaveCorrectSequenceFor3FirstNames1SurnameAnd1AliasSurname() {
        InputNames inputNames = new InputNames("A B C", "D", "E");
        List<CandidateName> candidateNames = aliasCombinations.generateCandidates(inputNames, inputNames);

        assertThat(candidateNames).containsExactly(
                new CandidateName("A", "D"),
                new CandidateName("B", "D"),
                new CandidateName("C", "D"),
                new CandidateName("A", "E"),
                new CandidateName("B", "E"),
                new CandidateName("C", "E"),
                new CandidateName("D", "E"),
                new CandidateName("A", "B"),
                new CandidateName("A", "C"),
                new CandidateName("B", "A"),
                new CandidateName("B", "C"),
                new CandidateName("C", "A"),
                new CandidateName("C", "B"),
                new CandidateName("D", "A"),
                new CandidateName("D", "B"),
                new CandidateName("D", "C"),
                new CandidateName("E", "A"),
                new CandidateName("E", "B"),
                new CandidateName("E", "C"),
                new CandidateName("E", "D")
        );
    }

    @Test
    public void shouldHaveCorrectSequenceFor3FirstNames1SurnameAnd2AliasSurnames() {
        InputNames inputNames = new InputNames("A B C", "D", "E F");
        List<CandidateName> candidateNames = aliasCombinations.generateCandidates(inputNames, inputNames);

        assertThat(candidateNames).containsExactly(
                new CandidateName("A", "D"),
                new CandidateName("B", "D"),
                new CandidateName("C", "D"),
                new CandidateName("A", "F"),
                new CandidateName("B", "F"),
                new CandidateName("C", "F"),
                new CandidateName("D", "F"),
                new CandidateName("A", "E"),
                new CandidateName("B", "E"),
                new CandidateName("C", "E"),
                new CandidateName("D", "E"),
                new CandidateName("A", "B"),
                new CandidateName("A", "C"),
                new CandidateName("B", "A"),
                new CandidateName("B", "C"),
                new CandidateName("C", "A"),
                new CandidateName("C", "B"),
                new CandidateName("D", "A"),
                new CandidateName("D", "B"),
                new CandidateName("D", "C"),
                new CandidateName("E", "A"),
                new CandidateName("E", "B"),
                new CandidateName("E", "C"),
                new CandidateName("E", "D"),
                new CandidateName("E", "F"),
                new CandidateName("F", "A"),
                new CandidateName("F", "B"),
                new CandidateName("F", "C"),
                new CandidateName("F", "D"),
                new CandidateName("F", "E")
        );
    }

    @Test
    public void shouldHaveCorrectSequenceFor3FirstNames1SurnameAnd3AliasSurnames() {
        InputNames inputNames = new InputNames("A B C", "D", "E F G");
        List<CandidateName> candidateNames = aliasCombinations.generateCandidates(inputNames, inputNames);

        assertThat(candidateNames).containsExactly(
                new CandidateName("A", "D"),
                new CandidateName("B", "D"),
                new CandidateName("C", "D"),
                new CandidateName("A", "G"),
                new CandidateName("B", "G"),
                new CandidateName("C", "G"),
                new CandidateName("D", "G"),
                new CandidateName("A", "F"),
                new CandidateName("B", "F"),
                new CandidateName("C", "F"),
                new CandidateName("D", "F"),
                new CandidateName("A", "E"),
                new CandidateName("B", "E"),
                new CandidateName("C", "E"),
                new CandidateName("D", "E"),
                new CandidateName("A", "B"),
                new CandidateName("A", "C"),
                new CandidateName("B", "A"),
                new CandidateName("B", "C"),
                new CandidateName("C", "A"),
                new CandidateName("C", "B"),
                new CandidateName("D", "A"),
                new CandidateName("D", "B"),
                new CandidateName("D", "C"),
                new CandidateName("E", "A"),
                new CandidateName("E", "B"),
                new CandidateName("E", "C"),
                new CandidateName("E", "D"),
                new CandidateName("E", "F"),
                new CandidateName("E", "G"),
                new CandidateName("F", "A"),
                new CandidateName("F", "B"),
                new CandidateName("F", "C"),
                new CandidateName("F", "D"),
                new CandidateName("F", "E"),
                new CandidateName("F", "G"),
                new CandidateName("G", "A"),
                new CandidateName("G", "B"),
                new CandidateName("G", "C"),
                new CandidateName("G", "D"),
                new CandidateName("G", "E"),
                new CandidateName("G", "F")
        );
    }

    @Test
    public void shouldHaveCorrectSequenceFor3FirstNames1SurnameAnd4AliasSurnames() {
        InputNames inputNames = new InputNames("A B C", "D", "E F G H");
        List<CandidateName> candidateNames = aliasCombinations.generateCandidates(inputNames, inputNames);

        assertThat(candidateNames).containsExactly(
                new CandidateName("A", "D"),
                new CandidateName("B", "D"),
                new CandidateName("C", "D"),
                new CandidateName("A", "H"),
                new CandidateName("B", "H"),
                new CandidateName("C", "H"),
                new CandidateName("D", "H"),
                new CandidateName("A", "G"),
                new CandidateName("B", "G"),
                new CandidateName("C", "G"),
                new CandidateName("D", "G"),
                new CandidateName("A", "F"),
                new CandidateName("B", "F"),
                new CandidateName("C", "F"),
                new CandidateName("D", "F"),
                new CandidateName("A", "E"),
                new CandidateName("B", "E"),
                new CandidateName("C", "E"),
                new CandidateName("D", "E"),
                new CandidateName("A", "B"),
                new CandidateName("A", "C"),
                new CandidateName("B", "A"),
                new CandidateName("B", "C"),
                new CandidateName("C", "A"),
                new CandidateName("C", "B"),
                new CandidateName("D", "A"),
                new CandidateName("D", "B"),
                new CandidateName("D", "C"),
                new CandidateName("E", "A"),
                new CandidateName("E", "B"),
                new CandidateName("E", "C"),
                new CandidateName("E", "D"),
                new CandidateName("E", "F"),
                new CandidateName("E", "G"),
                new CandidateName("E", "H"),
                new CandidateName("F", "A"),
                new CandidateName("F", "B"),
                new CandidateName("F", "C"),
                new CandidateName("F", "D"),
                new CandidateName("F", "E"),
                new CandidateName("F", "G"),
                new CandidateName("F", "H"),
                new CandidateName("G", "A"),
                new CandidateName("G", "B"),
                new CandidateName("G", "C"),
                new CandidateName("G", "D"),
                new CandidateName("G", "E"),
                new CandidateName("G", "F"),
                new CandidateName("G", "H"),
                new CandidateName("H", "A"),
                new CandidateName("H", "B"),
                new CandidateName("H", "C"),
                new CandidateName("H", "D"),
                new CandidateName("H", "E"),
                new CandidateName("H", "F"),
                new CandidateName("H", "G")
        );
    }

    @Test
    public void shouldHaveCorrectSequenceFor3FirstNames2SurnamesAnd3AliasSurnames() {
        InputNames inputNames = new InputNames("A B C", "D E", "F G H");
        List<CandidateName> candidateNames = aliasCombinations.generateCandidates(inputNames, inputNames);

        assertThat(candidateNames).isEqualTo(asList(
                new CandidateName("A", "E"),
                new CandidateName("B", "E"),
                new CandidateName("C", "E"),
                new CandidateName("D", "E"),
                new CandidateName("A", "D"),
                new CandidateName("B", "D"),
                new CandidateName("C", "D"),
                new CandidateName("E", "D"),
                new CandidateName("A", "H"),
                new CandidateName("B", "H"),
                new CandidateName("C", "H"),
                new CandidateName("D", "H"),
                new CandidateName("E", "H"),
                new CandidateName("A", "G"),
                new CandidateName("B", "G"),
                new CandidateName("C", "G"),
                new CandidateName("D", "G"),
                new CandidateName("E", "G"),
                new CandidateName("A", "F"),
                new CandidateName("B", "F"),
                new CandidateName("C", "F"),
                new CandidateName("D", "F"),
                new CandidateName("E", "F"),
                new CandidateName("A", "B"),
                new CandidateName("A", "C"),
                new CandidateName("B", "A"),
                new CandidateName("B", "C"),
                new CandidateName("C", "A"),
                new CandidateName("C", "B"),
                new CandidateName("D", "A"),
                new CandidateName("D", "B"),
                new CandidateName("D", "C"),
                new CandidateName("E", "A"),
                new CandidateName("E", "B"),
                new CandidateName("E", "C"),
                new CandidateName("F", "A"),
                new CandidateName("F", "B"),
                new CandidateName("F", "C"),
                new CandidateName("F", "D"),
                new CandidateName("F", "E"),
                new CandidateName("F", "G"),
                new CandidateName("F", "H"),
                new CandidateName("G", "A"),
                new CandidateName("G", "B"),
                new CandidateName("G", "C"),
                new CandidateName("G", "D"),
                new CandidateName("G", "E"),
                new CandidateName("G", "F"),
                new CandidateName("G", "H"),
                new CandidateName("H", "A"),
                new CandidateName("H", "B"),
                new CandidateName("H", "C"),
                new CandidateName("H", "D"),
                new CandidateName("H", "E"),
                new CandidateName("H", "F"),
                new CandidateName("H", "G")
        ));
    }
}