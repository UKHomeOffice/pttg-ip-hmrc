package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpecialCharactersTest {

    private static final String SOME_NAME = "somelastname";
    private static final String SOME_ALIAS_SURNAME = "somealiassurname";

    @Mock
    private NameCombinations mockNameCombinations;
    @Mock
    private MultipleLastNames mockMultipleLastNames;

    private SpecialCharacters specialCharacters;

    @Before
    public void setUp() {
        specialCharacters = new SpecialCharacters(mockNameCombinations, mockMultipleLastNames);
        when(mockNameCombinations.generateCandidates(any(InputNames.class))).thenReturn(emptyList());
        when(mockMultipleLastNames.generateCandidates(any(InputNames.class))).thenReturn(emptyList());
    }

    @Test
    public void shouldStripOutHyphensFromFirstName() {
        InputNames inputNames = new InputNames("-a-b-", SOME_NAME, SOME_ALIAS_SURNAME);
        InputNames inputWithHyphensRemoved = new InputNames("ab", SOME_NAME, "");

        specialCharacters.generateCandidates(inputNames);

        verify(mockNameCombinations).generateCandidates(inputWithHyphensRemoved);
        verify(mockMultipleLastNames).generateCandidates(inputWithHyphensRemoved);
    }

    @Test
    public void shouldStripOutApostrophesFromFirstName() {
        InputNames inputNames = new InputNames("'a'b'", SOME_NAME, SOME_ALIAS_SURNAME);
        InputNames inputWithApostrophesRemoved = new InputNames("ab", SOME_NAME, "");

        specialCharacters.generateCandidates(inputNames);

        verify(mockNameCombinations).generateCandidates(inputWithApostrophesRemoved);
        verify(mockMultipleLastNames).generateCandidates(inputWithApostrophesRemoved);
    }

    @Test
    public void shouldReplaceHyphensWithSpacesInFirstName() {
        InputNames inputNames = new InputNames("-a-b-", SOME_NAME, SOME_ALIAS_SURNAME);
        InputNames inputWithSpaces = new InputNames(" a b ", SOME_NAME, "");

        specialCharacters.generateCandidates(inputNames);

        verify(mockNameCombinations).generateCandidates(inputWithSpaces);
        verify(mockMultipleLastNames).generateCandidates(inputWithSpaces);
    }

    @Test
    public void shouldReplaceApostrophesWithSpacesInFirstName() {
        InputNames inputNames = new InputNames("'a'b'", SOME_NAME, SOME_ALIAS_SURNAME);
        InputNames inputWithSpaces = new InputNames(" a b ", SOME_NAME, "");

        specialCharacters.generateCandidates(inputNames);

        verify(mockNameCombinations).generateCandidates(inputWithSpaces);
        verify(mockMultipleLastNames).generateCandidates(inputWithSpaces);
    }

    @Test
    public void shouldStripOutHyphensFromLastName() {
        InputNames inputNames = new InputNames(SOME_NAME, "-a-b-", SOME_ALIAS_SURNAME);
        InputNames inputWithHyphensRemoved = new InputNames(SOME_NAME, "ab", "");

        specialCharacters.generateCandidates(inputNames);

        verify(mockNameCombinations).generateCandidates(inputWithHyphensRemoved);
        verify(mockMultipleLastNames).generateCandidates(inputWithHyphensRemoved);
    }

    @Test
    public void shouldStripOutApostrophesFromLastName() {
        InputNames inputNames = new InputNames(SOME_NAME, "'a'b'", SOME_ALIAS_SURNAME);
        InputNames inputWithApostrophesRemoved = new InputNames(SOME_NAME, "ab", "");

        specialCharacters.generateCandidates(inputNames);

        verify(mockNameCombinations).generateCandidates(inputWithApostrophesRemoved);
        verify(mockMultipleLastNames).generateCandidates(inputWithApostrophesRemoved);
    }

    @Test
    public void shouldReplaceHyphensWithSpacesInLastName() {
        InputNames inputNames = new InputNames(SOME_NAME, "-a-b-", SOME_ALIAS_SURNAME);
        InputNames inputWithSpaces = new InputNames(SOME_NAME, " a b ", "");

        specialCharacters.generateCandidates(inputNames);

        verify(mockNameCombinations).generateCandidates(inputWithSpaces);
        verify(mockMultipleLastNames).generateCandidates(inputWithSpaces);
    }

    @Test
    public void shouldReplaceApostrophesWithSpacesInLastName() {
        InputNames inputNames = new InputNames(SOME_NAME, "'a'b'", SOME_ALIAS_SURNAME);
        InputNames inputWithSpaces = new InputNames(SOME_NAME, " a b ", "");

        specialCharacters.generateCandidates(inputNames);

        verify(mockNameCombinations).generateCandidates(inputWithSpaces);
        verify(mockMultipleLastNames).generateCandidates(inputWithSpaces);
    }

    @Test
    public void shouldStripOutFullStopsFromFirstName() {
        InputNames inputNames = new InputNames(".a.b.", SOME_NAME, SOME_ALIAS_SURNAME);
        InputNames inputWithFullStopsRemoved = new InputNames("ab", SOME_NAME, "");

        specialCharacters.generateCandidates(inputNames);

        verify(mockNameCombinations).generateCandidates(inputWithFullStopsRemoved);
        verify(mockMultipleLastNames).generateCandidates(inputWithFullStopsRemoved);
    }

    @Test
    public void shouldReplaceOutFullStopsWithSpacesFirstName() {
        InputNames inputNames = new InputNames(".a.b.", SOME_NAME, SOME_ALIAS_SURNAME);
        InputNames inputWithFullStopsRemoved = new InputNames("ab", SOME_NAME, "");

        specialCharacters.generateCandidates(inputNames);

        verify(mockNameCombinations).generateCandidates(inputWithFullStopsRemoved);
        verify(mockMultipleLastNames).generateCandidates(inputWithFullStopsRemoved);
    }

    @Test
    public void shouldStripOutFullStopsFromLastName() {
        InputNames inputNames = new InputNames( SOME_NAME,".a.b.", SOME_ALIAS_SURNAME);
        InputNames inputWithFullStopsRemoved = new InputNames( SOME_NAME,"ab", "");

        specialCharacters.generateCandidates(inputNames);

        verify(mockNameCombinations).generateCandidates(inputWithFullStopsRemoved);
        verify(mockMultipleLastNames).generateCandidates(inputWithFullStopsRemoved);
    }

    @Test
    public void shouldReplaceOutFullStopsWithSpacesLastName() {
        InputNames inputNames = new InputNames( SOME_NAME, ".a.b.",SOME_ALIAS_SURNAME);
        InputNames inputWithFullStopsRemoved = new InputNames( SOME_NAME,"ab", "");

        specialCharacters.generateCandidates(inputNames);

        verify(mockNameCombinations).generateCandidates(inputWithFullStopsRemoved);
        verify(mockMultipleLastNames).generateCandidates(inputWithFullStopsRemoved);
    }
}