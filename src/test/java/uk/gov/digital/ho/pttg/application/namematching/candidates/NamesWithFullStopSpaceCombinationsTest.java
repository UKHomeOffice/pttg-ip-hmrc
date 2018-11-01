package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class NamesWithFullStopSpaceCombinationsTest {

    private NamesWithFullStopSpaceCombinations namesWithFullStopSpaceCombinations = new NamesWithFullStopSpaceCombinations(new NameCombinations());

    @Test
    public void shouldReturnEmptyListWhenNoFullStopsInInputNames() {
        InputNames inputNames = new InputNames("John", "Smith");
        assertThat(namesWithFullStopSpaceCombinations.generateCandidates(inputNames)).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListWhenNamesAreOnlyFullStops() {
        InputNames inputNames = new InputNames(".", ".");
        assertThat(namesWithFullStopSpaceCombinations.generateCandidates(inputNames)).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListWhenFullStopAtVeryEndOfName() {
        InputNames fullStopEndOfFirstName = new InputNames("John.", "Smith");
        InputNames fullStopEndOfLastName = new InputNames("John", "Smith.");
        assertThat(namesWithFullStopSpaceCombinations.generateCandidates(fullStopEndOfFirstName)).isEmpty();
        assertThat(namesWithFullStopSpaceCombinations.generateCandidates(fullStopEndOfLastName)).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListWhenFullStopNotFollowedBySpace() {
        InputNames fullStopInFirstName = new InputNames("St.John", "Smith");
        InputNames fullStopInLastName = new InputNames("David", "St.John");
        assertThat(namesWithFullStopSpaceCombinations.generateCandidates(fullStopInFirstName)).isEmpty();
        assertThat(namesWithFullStopSpaceCombinations.generateCandidates(fullStopInLastName)).isEmpty();
    }

    @Test
    public void shouldReturnCorrectCombinationWhenFullStopFollowedBySpaceFirstName() {
        InputNames fullStopInFirstName = new InputNames("St. John", "Smith");
        List<CandidateName> expected = asList(
                new CandidateName("St. John", "Smith"),
                new CandidateName("Smith", "St. John")
        );

        assertThat(namesWithFullStopSpaceCombinations.generateCandidates(fullStopInFirstName)).isEqualTo(expected);
    }

    @Test
    public void shouldOnlyUseFirst4ForenamesAndLast3SurnamesIfOver7NamesAfterSplitting() {
        List<String> firstNames = asList("St. John", "Dr. Pepper", "Mr. Mister", "An. Other", "Dr. No");
        List<String> lastNames = asList("Ms. Smith", "Mr. Jones", "Sgt. Price", "Dme. Bassey");
        InputNames inputNames = new InputNames(firstNames, lastNames);

        List<CandidateName> candidateNames = namesWithFullStopSpaceCombinations.generateCandidates(inputNames);
        assertNamePresentInCandidateNames(candidateNames, "St. John", "Dr. Pepper", "Mr. Mister", "An. Other", "Mr. Jones", "Sgt. Price", "Dme. Bassey");
        assertNameNotPresentInCandidateNames(candidateNames, "Dr. No", "Ms. Smith");
    }

    private void assertNameNotPresentInCandidateNames(List<CandidateName> candidateNames, String... names) {
        for (String name : names) {
            assertThat(candidateNames)
                    .noneMatch(
                            candidateName -> candidateName.firstName().equals(name) || candidateName.lastName().equals(name)
                    );
        }
    }

    private void assertNamePresentInCandidateNames(List<CandidateName> candidateNames, String... names) {
        for (String name : names) {
            assertThat(candidateNames)
                    .anyMatch(
                            candidateName -> candidateName.firstName().equals(name) || candidateName.lastName().equals(name)
                    );
        }
    }
}