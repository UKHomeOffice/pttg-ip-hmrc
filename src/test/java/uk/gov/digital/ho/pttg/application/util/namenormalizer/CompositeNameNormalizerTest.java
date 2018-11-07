package uk.gov.digital.ho.pttg.application.util.namenormalizer;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.dto.HmrcIndividual;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CompositeNameNormalizerTest {

    private static final String CHARACTERS_THAT_DO_NOT_MAP_TO_LETTERS_FILENAME = "characters_that_do_not_map_to_letters.csv";

    @Mock
    private HmrcIndividual mockInputIndividual;

    @Mock
    private HmrcIndividual mockOutputIndividual;

    private CompositeNameNormalizer compositeNameNormalizer;

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenArrayIsNull() {
        new CompositeNameNormalizer(null);
    }

    @Test
    public void shouldReturnInputtedIndividualWhenNoNameNormalizers() {
        // given
        NameNormalizer[] nameNormalizers = {};
        compositeNameNormalizer = new CompositeNameNormalizer(nameNormalizers);

        // when
        HmrcIndividual actualIndividual = compositeNameNormalizer.normalizeNames(mockInputIndividual);

        // then
        assertThat(actualIndividual).isEqualTo(mockInputIndividual);
        verifyNoMoreInteractions(mockInputIndividual);
    }

    @Test
    public void shouldCallSingleNameNormalizer() {
        // given

        NameNormalizer mockNameNormalizerOne = setupNameNormalizerMock();
        NameNormalizer[] nameNormalizers = {mockNameNormalizerOne};

        compositeNameNormalizer = new CompositeNameNormalizer(nameNormalizers);

        // when
        HmrcIndividual actualIndividual = compositeNameNormalizer.normalizeNames(mockInputIndividual);

        // then
        verify(mockNameNormalizerOne).normalizeNames(mockInputIndividual);

        assertThat(actualIndividual).isEqualTo(mockOutputIndividual);
        verifyNoMoreInteractions(mockOutputIndividual);
    }

    @Test
    public void shouldCallMultipleNameNormalizers() {
        // given

        NameNormalizer mockNameNormalizerOne = setupNameNormalizerMock();
        NameNormalizer mockNameNormalizerTwo = setupNameNormalizerMock();
        NameNormalizer[] nameNormalizers = {mockNameNormalizerOne, mockNameNormalizerTwo};

        compositeNameNormalizer = new CompositeNameNormalizer(nameNormalizers);

        // when
        HmrcIndividual actualIndividual = compositeNameNormalizer.normalizeNames(mockInputIndividual);

        // then
        verify(mockNameNormalizerOne).normalizeNames(mockInputIndividual);
        verify(mockNameNormalizerTwo).normalizeNames(mockOutputIndividual);

        assertThat(actualIndividual).isEqualTo(mockOutputIndividual);
        verifyNoMoreInteractions(mockOutputIndividual);
    }

    @Test
    public void shouldStripCharactersThatDoNotMapToLetters() throws IOException {
        NameNormalizer[] nameNormalizers = {new DiacriticNameNormalizer(), new InvalidCharacterNameNormalizer()};
        compositeNameNormalizer = new CompositeNameNormalizer(nameNormalizers);

        LocalDate someDob = LocalDate.now();
        for (String nonMappingCharacter : getCharactersThatDoNotMapToLetters()) {
            HmrcIndividual inputIndividual = new HmrcIndividual(nonMappingCharacter, nonMappingCharacter, "some nino", someDob);
            HmrcIndividual expectedNormalizedIndividual = new HmrcIndividual("", "", "some nino", someDob);

            assertThat(compositeNameNormalizer.normalizeNames(inputIndividual)).isEqualTo(expectedNormalizedIndividual);
        }
    }

    private List<String> getCharactersThatDoNotMapToLetters() throws IOException {
        return getInputCharactersOnly(parseCharactersNotMatchingCsv());
    }

    private MappingIterator<Map<String, String>> parseCharactersNotMatchingCsv() throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schema().withHeader();
        return mapper
                .readerFor(Map.class)
                .with(schema)
                .readValues(getCharacterNotMappingToLettersFile());
    }

    private List<String> getInputCharactersOnly(MappingIterator<Map<String, String>> mappingIterator) throws IOException {
        return mappingIterator.readAll()
                .stream()
                .map(row -> row.get("Input Character"))
                .collect(Collectors.toList());
    }

    private File getCharacterNotMappingToLettersFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = requireNonNull(classLoader.getResource(CHARACTERS_THAT_DO_NOT_MAP_TO_LETTERS_FILENAME));
        return new File(resource.getFile());
    }

    private NameNormalizer setupNameNormalizerMock() {
        NameNormalizer mock = mock(NameNormalizer.class);
        when(mock.normalizeNames(any())).thenReturn(mockOutputIndividual);
        return mock;
    }
}