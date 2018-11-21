package uk.gov.digital.ho.pttg.application.namematching;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.pttg.application.namematching.candidates.*;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        NameMatchingCandidatesService.class,
        GeneratorFactory.class
})
public class NameMatchingCandidateGeneratorIT {

    @MockBean EntireNonAliasName mockEntireNonAliasName;
    @MockBean EntireLastNameAndEachFirstName mockEntireLastNameAndEachFirstName;
    @MockBean MultipleLastNames mockMultipleLastNames;
    @MockBean AbbreviatedNames mockNamesWithFullStopSpaceCombinations;
    @MockBean AliasCombinations mockAliasCombinations;
    @MockBean NameCombinations mockNameCombinations;
    @MockBean SpecialCharacters mockSpecialCharacters;
    @MockBean GeneratorFactory mockGeneratorFactory;

    @Autowired
    private NameMatchingCandidatesService nameMatchingCandidatesService;

    @Before
    public void setup() {
        when(mockGeneratorFactory.createGenerators(any(InputNames.class))).thenReturn(Arrays.asList(mockEntireNonAliasName,
                mockEntireLastNameAndEachFirstName,
                mockMultipleLastNames,
                mockNamesWithFullStopSpaceCombinations,
                mockAliasCombinations,
                mockNameCombinations,
                mockSpecialCharacters));

        when(mockEntireNonAliasName.generateCandidates(any(InputNames.class), any(InputNames.class))).thenReturn(singletonList(new CandidateName("1", "1")));
        when(mockEntireLastNameAndEachFirstName.generateCandidates(any(InputNames.class), any(InputNames.class))).thenReturn(singletonList(new CandidateName("2", "2")));
        when(mockMultipleLastNames.generateCandidates(any(InputNames.class), any(InputNames.class))).thenReturn(singletonList(new CandidateName("3", "3")));
        when(mockNamesWithFullStopSpaceCombinations.generateCandidates(any(InputNames.class), any(InputNames.class))).thenReturn(singletonList(new CandidateName("4", "4")));
        when(mockAliasCombinations.generateCandidates(any(InputNames.class), any(InputNames.class))).thenReturn(singletonList(new CandidateName("5", "5")));
        when(mockNameCombinations.generateCandidates(any(InputNames.class), any(InputNames.class))).thenReturn(singletonList(new CandidateName("6", "6")));
        when(mockSpecialCharacters.generateCandidates(any(InputNames.class), any(InputNames.class))).thenReturn(singletonList(new CandidateName("7", "7")));
    }

    @Test
    public void shouldUseCollaborators() {
        nameMatchingCandidatesService.generateCandidateNames("Some First Names", "Some Last Names", "Some Alias");

        verify(mockEntireNonAliasName).generateCandidates(any(InputNames.class), any(InputNames.class));
        verify(mockEntireLastNameAndEachFirstName).generateCandidates(any(InputNames.class), any(InputNames.class));
        verify(mockMultipleLastNames).generateCandidates(any(InputNames.class), any(InputNames.class));
        verify(mockNamesWithFullStopSpaceCombinations).generateCandidates(any(InputNames.class), any(InputNames.class));
        verify(mockAliasCombinations).generateCandidates(any(InputNames.class), any(InputNames.class));
        verify(mockNameCombinations).generateCandidates(any(InputNames.class), any(InputNames.class));
        verify(mockSpecialCharacters).generateCandidates(any(InputNames.class), any(InputNames.class));
    }

    @Test
    public void shouldSupplyServiceWithGeneratorsInCorrectOrder() {
        List<CandidateName> candidateNames = nameMatchingCandidatesService.generateCandidateNames("Some First Names", "Some Last Names", "Some Alias");
        assertThat(candidateNames.get(0).firstName()).isEqualTo("1");
        assertThat(candidateNames.get(1).firstName()).isEqualTo("2");
        assertThat(candidateNames.get(2).firstName()).isEqualTo("3");
        assertThat(candidateNames.get(3).firstName()).isEqualTo("4");
        assertThat(candidateNames.get(4).firstName()).isEqualTo("5");
        assertThat(candidateNames.get(5).firstName()).isEqualTo("6");
        assertThat(candidateNames.get(6).firstName()).isEqualTo("7");
    }

}
