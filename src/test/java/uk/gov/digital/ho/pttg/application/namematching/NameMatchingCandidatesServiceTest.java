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

    @Mock private EntireNonAliasName entireNonAliasName;
    @Mock private EntireLastNameAndEachFirstName entireLastNameAndEachFirstName;
    @Mock private MultipleLastNames multipleLastNames;
    @Mock private NamesWithFullStopSpaceCombinations namesWithFullStopSpaceCombinations;
    @Mock private AliasCombinations aliasCombinations;
    @Mock private NameCombinations nameCombinations;
    @Mock private SpecialCharacters specialCharacters;

    @Before
    public void setUp() {
        nameMatchingCandidatesService = new NameMatchingCandidatesService(
                Arrays.asList(
                        entireNonAliasName,
                        entireLastNameAndEachFirstName,
                        multipleLastNames,
                        namesWithFullStopSpaceCombinations,
                        aliasCombinations,
                        nameCombinations,
                        specialCharacters
                        ));

        when(entireNonAliasName.generateCandidates(any(InputNames.class))).thenReturn(Collections.emptyList());
        when(entireLastNameAndEachFirstName.generateCandidates(any(InputNames.class))).thenReturn(Collections.emptyList());
        when(multipleLastNames.generateCandidates(any(InputNames.class))).thenReturn(Collections.emptyList());
        when(namesWithFullStopSpaceCombinations.generateCandidates(any(InputNames.class))).thenReturn(Collections.emptyList());
        when(aliasCombinations.generateCandidates(any(InputNames.class))).thenReturn(Collections.emptyList());
        when(nameCombinations.generateCandidates(any(InputNames.class))).thenReturn(Collections.emptyList());
        when(specialCharacters.generateCandidates(any(InputNames.class))).thenReturn(Collections.emptyList());
    }

    @Test
    @SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void attemptsCorrectCandidateGeneratorsForNameWithoutAliases() {
        InputNames expectedInputNames = new InputNames("firstname1 firstname2", "lastname1 lastname2");

        nameMatchingCandidatesService.generateCandidateNames("firstname1 firstname2", "lastname1 lastname2", "");

        verify(entireNonAliasName).generateCandidates(expectedInputNames);
        verify(entireLastNameAndEachFirstName).generateCandidates(expectedInputNames);
        verify(multipleLastNames).generateCandidates(expectedInputNames);
        verify(namesWithFullStopSpaceCombinations).generateCandidates(expectedInputNames);
        verify(aliasCombinations).generateCandidates(expectedInputNames);
        verify(nameCombinations).generateCandidates(expectedInputNames);
        verify(specialCharacters).generateCandidates(expectedInputNames);
    }

    @Test
    @SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void attemptsCorrectCandidateGeneratorsForNameWithAliases() {
        InputNames expectedInputNames = new InputNames("firstname1 firstname2", "lastname1 lastname2", "aliasSurname1 aliasSurname2");

        nameMatchingCandidatesService.generateCandidateNames("firstname1 firstname2", "lastname1 lastname2", "aliasSurname1 aliasSurname2");

        verify(entireNonAliasName).generateCandidates(expectedInputNames);
        verify(entireLastNameAndEachFirstName).generateCandidates(expectedInputNames);
        verify(multipleLastNames).generateCandidates(expectedInputNames);
        verify(namesWithFullStopSpaceCombinations).generateCandidates(expectedInputNames);
        verify(aliasCombinations).generateCandidates(expectedInputNames);
        verify(nameCombinations).generateCandidates(expectedInputNames);
        verify(specialCharacters).generateCandidates(expectedInputNames);
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
        when(nameCombinations.generateCandidates(any(InputNames.class))).thenReturn(nameCombinationCandidateNames);

        List<CandidateName> candidateNames = nameMatchingCandidatesService.generateCandidateNames("ignored", "ignored", "");

        assertThat(candidateNames.size()).isEqualTo(1);
        assertThat(candidateNames.get(0).firstName().substring(0, 3)).isEqualTo("fir");
        assertThat(candidateNames.get(0).lastName().substring(0, 1)).isEqualTo("l");
    }
}
