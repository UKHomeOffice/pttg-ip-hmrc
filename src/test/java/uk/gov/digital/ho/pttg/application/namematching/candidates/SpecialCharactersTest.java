package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SpecialCharactersTest {

    private static final String SOME_NAME = "somelastname";
    private static final String SOME_ALIAS_SURNAME = "somealiassurname";

    @Mock
    private EntireNonAliasName mockEntireNonAliasName;
    @Mock
    private EntireLastNameAndEachFirstName mockEntireLastNameAndEachFirstName;
    @Mock
    private NameCombinations mockNameCombinations;
    @Mock
    private AliasCombinations mockAliasCombinations;
    @Mock
    private MultipleLastNames mockMultipleLastNames;

    private SpecialCharacters specialCharacters;

    @Before
    public void setUp() {
        specialCharacters = new SpecialCharacters(mockEntireNonAliasName, mockEntireLastNameAndEachFirstName, mockNameCombinations, mockAliasCombinations, mockMultipleLastNames);

        when(mockEntireNonAliasName.generateCandidates(any(InputNames.class))).thenReturn(emptyList());
        when(mockEntireLastNameAndEachFirstName.generateCandidates(any(InputNames.class))).thenReturn(emptyList());
        when(mockNameCombinations.generateCandidates(any(InputNames.class))).thenReturn(emptyList());
        when(mockMultipleLastNames.generateCandidates(any(InputNames.class))).thenReturn(emptyList());
    }

    @Test
    public void shouldStripOutHyphensFromFirstName() {
        InputNames inputNames = new InputNames("-a-b-", SOME_NAME);
        InputNames inputWithHyphensRemoved = new InputNames("ab", SOME_NAME);

        specialCharacters.generateCandidates(inputNames);

        verifyMandatoryGenerators(inputWithHyphensRemoved);
    }

    @Test
    public void shouldStripOutApostrophesFromFirstName() {
        InputNames inputNames = new InputNames("'a'b'", SOME_NAME);
        InputNames inputWithApostrophesRemoved = new InputNames("ab", SOME_NAME);

        specialCharacters.generateCandidates(inputNames);

        verifyMandatoryGenerators(inputWithApostrophesRemoved);
    }

    @Test
    public void shouldReplaceHyphensWithSpacesInFirstName() {
        InputNames inputNames = new InputNames("-a-b-", SOME_NAME);
        InputNames inputWithSpaces = new InputNames(" a b ", SOME_NAME);

        specialCharacters.generateCandidates(inputNames);

        verifyMandatoryGenerators(inputWithSpaces);
    }

    @Test
    public void shouldReplaceApostrophesWithSpacesInFirstName() {
        InputNames inputNames = new InputNames("'a'b'", SOME_NAME);
        InputNames inputWithSpaces = new InputNames(" a b ", SOME_NAME);

        specialCharacters.generateCandidates(inputNames);

        verifyMandatoryGenerators(inputWithSpaces);
    }

    @Test
    public void shouldStripOutHyphensFromLastName() {
        InputNames inputNames = new InputNames(SOME_NAME, "-a-b-");
        InputNames inputWithHyphensRemoved = new InputNames(SOME_NAME, "ab");

        specialCharacters.generateCandidates(inputNames);

        verifyMandatoryGenerators(inputWithHyphensRemoved);
    }

    @Test
    public void shouldStripOutApostrophesFromLastName() {
        InputNames inputNames = new InputNames(SOME_NAME, "'a'b'");
        InputNames inputWithApostrophesRemoved = new InputNames(SOME_NAME, "ab", "");

        specialCharacters.generateCandidates(inputNames);

        verifyMandatoryGenerators(inputWithApostrophesRemoved);
    }

    @Test
    public void shouldReplaceHyphensWithSpacesInLastName() {
        InputNames inputNames = new InputNames(SOME_NAME, "-a-b-");
        InputNames inputWithSpaces = new InputNames(SOME_NAME, " a b ", "");

        specialCharacters.generateCandidates(inputNames);

        verifyMandatoryGenerators(inputWithSpaces);
    }

    private void verifyMandatoryGenerators(InputNames inputNames) {
        verify(mockEntireNonAliasName).generateCandidates(inputNames);
        verify(mockEntireLastNameAndEachFirstName).generateCandidates(inputNames);
        verify(mockNameCombinations).generateCandidates(inputNames);
        verify(mockMultipleLastNames).generateCandidates(inputNames);
    }

    @Test
    public void shouldReplaceApostrophesWithSpacesInLastName() {
        InputNames inputNames = new InputNames(SOME_NAME, "'a'b'");
        InputNames inputWithSpaces = new InputNames(SOME_NAME, " a b ", "");

        specialCharacters.generateCandidates(inputNames);

        verifyMandatoryGenerators(inputWithSpaces);
    }

    @Test
    public void shouldStripOutFullStopsFromFirstName() {
        InputNames inputNames = new InputNames(".a.b.", SOME_NAME);
        InputNames inputWithFullStopsRemoved = new InputNames("ab", SOME_NAME, "");

        specialCharacters.generateCandidates(inputNames);

        verifyMandatoryGenerators(inputWithFullStopsRemoved);
    }

    @Test
    public void shouldReplaceOutFullStopsWithSpacesFirstName() {
        InputNames inputNames = new InputNames(".a.b.", SOME_NAME);
        InputNames inputWithFullStopsRemoved = new InputNames("ab", SOME_NAME, "");

        specialCharacters.generateCandidates(inputNames);

        verifyMandatoryGenerators(inputWithFullStopsRemoved);
    }

    @Test
    public void shouldStripOutFullStopsFromLastName() {
        InputNames inputNames = new InputNames(SOME_NAME, ".a.b.");
        InputNames inputWithFullStopsRemoved = new InputNames( SOME_NAME,"ab", "");

        specialCharacters.generateCandidates(inputNames);

        verifyMandatoryGenerators(inputWithFullStopsRemoved);
    }

    @Test
    public void shouldReplaceOutFullStopsWithSpacesLastName() {
        InputNames inputNames = new InputNames(SOME_NAME, ".a.b.");
        InputNames inputWithFullStopsRemoved = new InputNames( SOME_NAME,"ab", "");

        specialCharacters.generateCandidates(inputNames);

        verifyMandatoryGenerators(inputWithFullStopsRemoved);
    }

    @Test
    public void shouldStripOutAndReplaceAllSpecialCharactersAtSameTime() {
        InputNames inputNamesWithManySpecialChars = new InputNames("a.b'", "c-.d");
        InputNames inputWithSpecialCharsRemoved = new InputNames("ab", "cd", "");
        InputNames inputWithSpecialCharsAsSpaces = new InputNames("a b ", "c  d", "");

        specialCharacters.generateCandidates(inputNamesWithManySpecialChars);

        verify(mockNameCombinations).generateCandidates(inputWithSpecialCharsRemoved);
        verify(mockNameCombinations).generateCandidates(inputWithSpecialCharsAsSpaces);
        verify(mockMultipleLastNames).generateCandidates(inputWithSpecialCharsRemoved);
        verify(mockMultipleLastNames).generateCandidates(inputWithSpecialCharsAsSpaces);
    }

    @Test
    public void shouldNotCallCollaboratorsWithEmptyNames() {
        InputNames inputNames = new InputNames(".", ".");

        specialCharacters.generateCandidates(inputNames);

        verifyNoMoreInteractions(mockNameCombinations);
        verifyNoMoreInteractions(mockMultipleLastNames);
    }

    @Test
    public void shouldNotCallAliasCombinationsWhenNoAlias() {
        InputNames inputNamesWithoutAliases = new InputNames("some-name", "some-name");

        specialCharacters.generateCandidates(inputNamesWithoutAliases);

        verifyNoMoreInteractions(mockAliasCombinations);
    }

    @Test
    public void shouldCallAliasCombinationsWhenAliasIsPresent() {
        InputNames inputNameWithAliases = new InputNames("John-Bob", SOME_NAME, SOME_ALIAS_SURNAME);

        specialCharacters.generateCandidates(inputNameWithAliases);

        verify(mockAliasCombinations).generateCandidates(new InputNames("John Bob", SOME_NAME, SOME_ALIAS_SURNAME));
    }

    @Test
    public void shouldNotCallNameCombinationsWhenAliasIsPresent() {
        InputNames inputNameWithAliases = new InputNames("some-name", SOME_NAME, SOME_ALIAS_SURNAME);

        specialCharacters.generateCandidates(inputNameWithAliases);

        verifyNoMoreInteractions(mockNameCombinations);
    }

    @Test
    public void shouldCallNameCombinationsWhenNoAlias() {
        InputNames inputNamesWithoutAliases = new InputNames("John-Bob", SOME_NAME);

        specialCharacters.generateCandidates(inputNamesWithoutAliases);

        verify(mockNameCombinations).generateCandidates(new InputNames("John Bob", SOME_NAME));
    }

    @Test
    public void shouldSplitAliasSurnameOnSpecialCharacters() {
        InputNames inputNamesWithSpecialCharAlias = new InputNames(SOME_NAME, SOME_NAME, "O'Neill-Jones");

        specialCharacters.generateCandidates(inputNamesWithSpecialCharAlias);

        verify(mockAliasCombinations).generateCandidates(new InputNames(SOME_NAME, SOME_NAME, "O Neill Jones"));
    }

    @Test
    public void shouldReplaceSpecialCharactersWithSpacesForAliasSurnames() {
        InputNames inputNamesWithSpecialCharAlias = new InputNames(SOME_NAME, SOME_NAME, "O'Neill-Jones");

        specialCharacters.generateCandidates(inputNamesWithSpecialCharAlias);

        verify(mockAliasCombinations).generateCandidates(new InputNames(SOME_NAME, SOME_NAME, "ONeillJones"));
    }
}