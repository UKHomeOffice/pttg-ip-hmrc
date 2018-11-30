package uk.gov.digital.ho.pttg.application.namematching;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.application.namematching.candidates.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NameMatchingCandidatesServiceTest {

    private NameMatchingCandidatesService nameMatchingCandidatesService;

    @Mock private EntireNonAliasName mockEntireNonAliasName;
    @Mock private EntireLastNameAndEachFirstName mockEntireLastNameAndEachFirstName;
    @Mock private MultipleLastNames mockMultipleLastNames;
    @Mock private AbbreviatedNames mockNamesWithFullStopSpaceCombinations;
    @Mock private AliasCombinations mockAliasCombinations;
    @Mock private NameCombinations mockNameCombinations;
    @Mock private SpecialCharacters mockSpecialCharacters;
    @Mock private GeneratorFactory mockGeneratorFactory;

    @Before
    public void setUp() {

        List<NameMatchingCandidateGenerator> nameMatchingCandidateGenerators = Arrays.asList(
                mockEntireNonAliasName,
                mockEntireLastNameAndEachFirstName,
                mockMultipleLastNames,
                mockNamesWithFullStopSpaceCombinations,
                mockAliasCombinations,
                mockNameCombinations,
                mockSpecialCharacters
        );

        nameMatchingCandidatesService = new NameMatchingCandidatesService(mockGeneratorFactory);

        when(mockGeneratorFactory.createGenerators(any(InputNames.class))).thenReturn(nameMatchingCandidateGenerators);
        when(mockEntireNonAliasName.generateCandidates(any(InputNames.class), any(InputNames.class))).thenReturn(Collections.emptyList());
        when(mockEntireLastNameAndEachFirstName.generateCandidates(any(InputNames.class), any(InputNames.class))).thenReturn(Collections.emptyList());
        when(mockMultipleLastNames.generateCandidates(any(InputNames.class), any(InputNames.class))).thenReturn(Collections.emptyList());
        when(mockNamesWithFullStopSpaceCombinations.generateCandidates(any(InputNames.class), any(InputNames.class))).thenReturn(Collections.emptyList());
        when(mockAliasCombinations.generateCandidates(any(InputNames.class), any(InputNames.class))).thenReturn(Collections.emptyList());
        when(mockNameCombinations.generateCandidates(any(InputNames.class), any(InputNames.class))).thenReturn(Collections.emptyList());
        when(mockSpecialCharacters.generateCandidates(any(InputNames.class), any(InputNames.class))).thenReturn(Collections.emptyList());
    }

    @Test
    @SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void shouldUseCollaborators() {
        nameMatchingCandidatesService.generateCandidateNames("some firstname", "some lastname", "some alias");

        verify(mockGeneratorFactory).createGenerators(any(InputNames.class));
    }

    @Test
    @SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void attemptsCorrectCandidateGeneratorsForNameWithoutAliases() {
        InputNames expectedInputNames = new InputNames("firstname1 firstname2", "lastname1 lastname2");

        nameMatchingCandidatesService.generateCandidateNames("firstname1 firstname2", "lastname1 lastname2", "");

        verify(mockEntireNonAliasName).generateCandidates(expectedInputNames, expectedInputNames);
        verify(mockEntireLastNameAndEachFirstName).generateCandidates(expectedInputNames, expectedInputNames);
        verify(mockMultipleLastNames).generateCandidates(expectedInputNames, expectedInputNames);
        verify(mockNamesWithFullStopSpaceCombinations).generateCandidates(expectedInputNames, expectedInputNames);
        verify(mockAliasCombinations).generateCandidates(expectedInputNames, expectedInputNames);
        verify(mockNameCombinations).generateCandidates(expectedInputNames, expectedInputNames);
        verify(mockSpecialCharacters).generateCandidates(expectedInputNames, expectedInputNames);
    }

    @Test
    @SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void attemptsCorrectCandidateGeneratorsForNameWithAliases() {
        InputNames expectedInputNames = new InputNames("firstname1 firstname2", "lastname1 lastname2", "aliasSurname1 aliasSurname2");

        nameMatchingCandidatesService.generateCandidateNames("firstname1 firstname2", "lastname1 lastname2", "aliasSurname1 aliasSurname2");

        verify(mockEntireNonAliasName).generateCandidates(expectedInputNames, expectedInputNames);
        verify(mockEntireLastNameAndEachFirstName).generateCandidates(expectedInputNames, expectedInputNames);
        verify(mockMultipleLastNames).generateCandidates(expectedInputNames, expectedInputNames);
        verify(mockNamesWithFullStopSpaceCombinations).generateCandidates(expectedInputNames, expectedInputNames);
        verify(mockAliasCombinations).generateCandidates(expectedInputNames, expectedInputNames);
        verify(mockNameCombinations).generateCandidates(expectedInputNames, expectedInputNames);
        verify(mockSpecialCharacters).generateCandidates(expectedInputNames, expectedInputNames);
    }

    @Test
    public void duplicateNamesAreRemoved() {
        List<CandidateName> nameCombinationCandidateNames =
                Arrays.asList(
                        new CandidateName("firstname1", "lastname1"),
                        new CandidateName("firstname2", "lastname1"),
                        new CandidateName("firstname1", "lastname2"),
                        new CandidateName("firstname2", "lastname2")
                );
        when(mockNameCombinations.generateCandidates(any(InputNames.class), any(InputNames.class))).thenReturn(nameCombinationCandidateNames);

        List<CandidateName> candidateNames = nameMatchingCandidatesService.generateCandidateNames("ignored", "ignored", "");

        assertThat(candidateNames.size()).isEqualTo(1);
        assertThat(candidateNames.get(0).firstName().substring(0, 3)).isEqualTo("fir");
        assertThat(candidateNames.get(0).lastName().substring(0, 1)).isEqualTo("l");
    }
}
