package uk.gov.digital.ho.pttg.application.namematching;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.application.namematching.candidates.*;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NameMatchingCandidatesServiceTest {

    private NameMatchingCandidatesService nameMatchingCandidatesService;

    @Mock
    private NameCombinations nameCombinations;
    @Mock
    private MultipleLastNames multipleLastNames;
    @Mock
    private SpecialCharacters specialCharacters;
    @Mock
    private AliasCombinations aliasCombinations;
    @Mock
    private EntireNonAliasName entireNonAliasName;

    @Before
    public void setUp() {
        nameMatchingCandidatesService = new NameMatchingCandidatesService(nameCombinations, multipleLastNames, specialCharacters, aliasCombinations, entireNonAliasName);
    }

    @Test
    @SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void attemptsCorrectCandidateGeneratorsForNameWithoutAliases() {
        InputNames expectedInputNames = new InputNames("firstname1 firstname2", "lastname1 lastname2");

        nameMatchingCandidatesService.generateCandidateNames("firstname1 firstname2", "lastname1 lastname2", "");

        verify(entireNonAliasName).generateCandidates(expectedInputNames);
        verify(nameCombinations).generateCandidates(expectedInputNames);
        verify(multipleLastNames).generateCandidates(expectedInputNames);
        verify(specialCharacters).generateCandidates(expectedInputNames);

        verify(aliasCombinations, never()).generateCandidates(any());
    }

    @Test
    @SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void attemptsCorrectCandidateGeneratorsForNameWithAliases() {
        InputNames expectedInputNames = new InputNames("firstname1 firstname2", "lastname1 lastname2", "aliasSurname1 aliasSurname2");

        nameMatchingCandidatesService.generateCandidateNames("firstname1 firstname2", "lastname1 lastname2", "aliasSurname1 aliasSurname2");

        verify(entireNonAliasName).generateCandidates(expectedInputNames);
        verify(aliasCombinations).generateCandidates(expectedInputNames);
        verify(multipleLastNames).generateCandidates(expectedInputNames);
        verify(specialCharacters).generateCandidates(expectedInputNames);

        verify(nameCombinations, never()).generateCandidates(any());
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
