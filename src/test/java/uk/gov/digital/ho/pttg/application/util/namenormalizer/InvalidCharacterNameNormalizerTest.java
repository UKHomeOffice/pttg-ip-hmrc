package uk.gov.digital.ho.pttg.application.util.namenormalizer;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.junit.Test;
import uk.gov.digital.ho.pttg.dto.HmrcIndividual;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

public class InvalidCharacterNameNormalizerTest {

    private static final String CHARACTERS_THAT_DO_NOT_MAP_TO_LETTERS_FILENAME = "characters_that_do_not_map_to_letters.csv";

    private static final String SOME_NINO = "Test Nino";
    private static final LocalDate SOME_DOB = LocalDate.of(2000, Month.DECEMBER, 25);


    private final InvalidCharacterNameNormalizer invalidCharacterNameNormalizer = new InvalidCharacterNameNormalizer();

    @Test
    public void nullNamesShouldNotBeNormalized() {
        HmrcIndividual individual = new HmrcIndividual(null, null, SOME_NINO, SOME_DOB);

        assertThat(invalidCharacterNameNormalizer.normalizeNames(individual)).isEqualTo(individual);
    }

    @Test
    public void shouldRemoveAllNonBasicLatinCharactersThatCannotBeNormalized() throws IOException {

        for (String firstName : getInvalidCharacters()) {
            // given
            String lastName = "Smith";
            HmrcIndividual individual = new HmrcIndividual(firstName, lastName, SOME_NINO, SOME_DOB);

            // when
            HmrcIndividual normalizedIndividual = invalidCharacterNameNormalizer.normalizeNames(individual);

            // then
            assertThat(normalizedIndividual.getFirstName()).withFailMessage("Expected `%s` to map to `%s` but was `%s`", firstName, "", normalizedIndividual.getFirstName()).isEqualTo("");
            assertThat(normalizedIndividual.getLastName()).isEqualTo("Smith");

            assertThat(normalizedIndividual.getNino()).isEqualTo(SOME_NINO);
            assertThat(normalizedIndividual.getDateOfBirth()).isEqualTo(SOME_DOB);
        }
    }

    @Test
    public void shouldNotRemoveJoinerCharacters() {
        HmrcIndividual individual = new HmrcIndividual("'. ", "-'", SOME_NINO, SOME_DOB);
        assertThat(invalidCharacterNameNormalizer.normalizeNames(individual)).isEqualTo(individual);
    }

    private List<String> getInvalidCharacters() throws IOException {
        return getExpectedMappingsOnly(parseCharactersNotMatchingCsv())
                .filter(invalidCharacter -> !invalidCharacter.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private MappingIterator<Map<Character, String>> parseCharactersNotMatchingCsv() throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schema().withHeader();
        return mapper
                .readerFor(Map.class)
                .with(schema)
                .readValues(getCharacterNotMappingToLettersFile());
    }

    private File getCharacterNotMappingToLettersFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = requireNonNull(classLoader.getResource(CHARACTERS_THAT_DO_NOT_MAP_TO_LETTERS_FILENAME));
        return new File(resource.getFile());
    }

    private Stream<String> getExpectedMappingsOnly(MappingIterator<Map<Character, String>> mappingIterator) throws IOException {
        return mappingIterator.readAll()
                .stream()
                .map(row -> row.get("Non-Letter Expected Mapping"));
    }
}