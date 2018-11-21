package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AbbreviatedNamesTest {

    private final AliasCombinations aliasCombinations = new AliasCombinations();
    private final NameCombinations nameCombinations = new NameCombinations();

    private final AbbreviatedNames namesWithFullStopSpaceCombinations = new AbbreviatedNames(nameCombinations, aliasCombinations);

    @Test
    public void shouldReturnEmptyListWhenNoFullStopsInInputNames() {
        InputNames inputNames = new InputNames("John", "Smith");
        assertThat(namesWithFullStopSpaceCombinations.generateCandidates(inputNames, inputNames)).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListWhenNamesAreOnlyFullStops() {
        InputNames inputNames = new InputNames(".", ".");
        assertThat(namesWithFullStopSpaceCombinations.generateCandidates(inputNames, inputNames)).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListWhenFullStopAtVeryEndOfName() {
        InputNames fullStopEndOfFirstName = new InputNames("John.", "Smith");
        InputNames fullStopEndOfLastName = new InputNames("John", "Smith.");
        assertThat(namesWithFullStopSpaceCombinations.generateCandidates(fullStopEndOfFirstName, fullStopEndOfFirstName)).isEmpty();
        assertThat(namesWithFullStopSpaceCombinations.generateCandidates(fullStopEndOfLastName, fullStopEndOfLastName)).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListWhenFullStopNotFollowedBySpace() {
        InputNames fullStopInFirstName = new InputNames("St.John", "Smith");
        InputNames fullStopInLastName = new InputNames("David", "St.John");
        assertThat(namesWithFullStopSpaceCombinations.generateCandidates(fullStopInFirstName, fullStopInFirstName)).isEmpty();
        assertThat(namesWithFullStopSpaceCombinations.generateCandidates(fullStopInLastName, fullStopInLastName)).isEmpty();
    }

    @Test
    public void shouldReturnCorrectCombinationWhenFullStopFollowedBySpaceFirstName() {
        InputNames fullStopInFirstName = new InputNames("St. John", "Smith");
        List<CandidateName> expected = asList(
                new CandidateName("St. John", "Smith"),
                new CandidateName("Smith", "St. John")
        );

        assertThat(namesWithFullStopSpaceCombinations.generateCandidates(fullStopInFirstName, fullStopInFirstName)).isEqualTo(expected);
    }

    @Test
    public void shouldOnlyUseFirst4ForenamesAndLast3SurnamesIfOver7NamesAfterSplitting() {
        String firstNames = "St. John Dr. Pepper Mr. Mister An. Other Dr. No";
        String lastNames = "Ms. Smith Mr. Jones Sgt. Price Dme. Bassey";
        InputNames inputNames = new InputNames(firstNames, lastNames);

        List<CandidateName> candidateNames = namesWithFullStopSpaceCombinations.generateCandidates(inputNames, inputNames);
        assertNamePresentInCandidateNames(candidateNames, "St. John", "Dr. Pepper", "Mr. Mister", "An. Other", "Mr. Jones", "Sgt. Price", "Dme. Bassey");
        assertNameNotPresentInCandidateNames(candidateNames, "Dr. No", "Ms. Smith");
    }

    @Test
    public void generateCandidates_noAliasNames_callNameCombinationsOnly() {
        InputNames inputNames = new InputNames("some. name", "some. name");

        NameCombinations mockedNameCombinations = mock(NameCombinations.class);
        AliasCombinations mockedAliasCombinations = mock(AliasCombinations.class);

        AbbreviatedNames namesWithFullStopSpaceCombinations = new AbbreviatedNames(mockedNameCombinations, mockedAliasCombinations);

        namesWithFullStopSpaceCombinations.generateCandidates(inputNames, inputNames);

        verify(mockedNameCombinations).generateCandidates(any(InputNames.class), any(InputNames.class));
        verifyNoMoreInteractions(mockedAliasCombinations);

    }

    @Test
    public void generateCandidates_withAliasNames_callAliasCombinationsOnly() {
        InputNames inputNames = new InputNames("some name", "some. name", "some alias");

        NameCombinations mockedNameCombinations = mock(NameCombinations.class);
        AliasCombinations mockedAliasCombinations = mock(AliasCombinations.class);

        AbbreviatedNames namesWithFullStopSpaceCombinations = new AbbreviatedNames(mockedNameCombinations, mockedAliasCombinations);

        namesWithFullStopSpaceCombinations.generateCandidates(inputNames, inputNames);

        verify(mockedAliasCombinations).generateCandidates(any(InputNames.class), any(InputNames.class));
        verifyNoMoreInteractions(mockedNameCombinations);

    }

    @Test
    public void generateCandidates_aliasPresentButNoFullStopSpace_correctCombination() {
        InputNames inputNamesWithAlias = new InputNames("David", "St. John", "Jones");

        List<CandidateName> expectedCandidateNames = asList(
                new CandidateName("David", "St. John"),
                new CandidateName("David", "Jones"),
                new CandidateName("St. John", "Jones"),
                new CandidateName("St. John", "David"),
                new CandidateName("Jones", "David"),
                new CandidateName("Jones", "St. John")
        );

        List<CandidateName> actualCandidateNames = namesWithFullStopSpaceCombinations.generateCandidates(inputNamesWithAlias, inputNamesWithAlias);

        assertThat(actualCandidateNames).isEqualTo(expectedCandidateNames);
    }

    @Test
    public void generateCandidates_aliasWithFullStopSpace_correctCombination() {
        InputNames inputNamesWithAlias = new InputNames("David", "Smith", "St. John");

        List<CandidateName> expectedCandidateNames = asList(
                new CandidateName("David", "Smith"),
                new CandidateName("David", "St. John"),
                new CandidateName("Smith", "St. John"),
                new CandidateName("Smith", "David"),
                new CandidateName("St. John", "David"),
                new CandidateName("St. John", "Smith")
        );

        List<CandidateName> actualCandidateNames = namesWithFullStopSpaceCombinations.generateCandidates(inputNamesWithAlias, inputNamesWithAlias);

        assertThat(actualCandidateNames).isEqualTo(expectedCandidateNames);
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