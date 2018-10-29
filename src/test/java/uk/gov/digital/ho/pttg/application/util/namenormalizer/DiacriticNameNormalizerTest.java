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

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

public class DiacriticNameNormalizerTest {

    private static final String UNICODE_MAPPING_CSV_FILENAME = "expected_unicode_replacements.csv";
    private static final String CHARACTERS_THAT_DO_NOT_MAP_TO_LETTERS_FILENAME = "characters_that_do_not_map_to_letters.csv";

    private static final String TEST_NINO = "Test Nino";
    private static final LocalDate TEST_DOB = LocalDate.of(2000, Month.DECEMBER, 25);

    private final DiacriticNameNormalizer accentNameNormalizer = new DiacriticNameNormalizer();

    @Test
    public void shouldReturnNamesWithAccentsRemoved() {
        // given
        String firstName = "Tĥïŝ ĩš";
        String lastName = "â fůňķŷ Šťŕĭńġ";
        HmrcIndividual individual = new HmrcIndividual(firstName, lastName, TEST_NINO, TEST_DOB);

        // when
        HmrcIndividual normalizedIndividual = accentNameNormalizer.normalizeNames(individual);

        // then
        assertThat(normalizedIndividual.getFirstName()).isEqualTo("This is");
        assertThat(normalizedIndividual.getLastName()).isEqualTo("a funky String");

        assertThat(normalizedIndividual.getNino()).isEqualTo(TEST_NINO);
        assertThat(normalizedIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
    }

    @Test
    public void shouldLeaveLatinBasicCharactersAsTheyWereInputted() {
        // given
        String firstName = "Querty";
        String lastName = "Smith";
        HmrcIndividual individual = new HmrcIndividual(firstName, lastName, TEST_NINO, TEST_DOB);

        // when
        HmrcIndividual normalizedIndividual = accentNameNormalizer.normalizeNames(individual);

        // then
        assertThat(normalizedIndividual.getFirstName()).isEqualTo("Querty");
        assertThat(normalizedIndividual.getLastName()).isEqualTo("Smith");

        assertThat(normalizedIndividual.getNino()).isEqualTo(TEST_NINO);
        assertThat(normalizedIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
    }

    @Test
    public void shouldRetainAllNonBasicLatinCharactersThatCannotBeNormalized() throws IOException {

        for (CharacterMapEntry entry : getExpectedMapForCharactersNotMappingToLetters()) {
            // given
            String firstName = String.valueOf(entry.getKey());
            String expectedNormalizedFirstName = entry.getValue();

            String lastName = "Smith";
            HmrcIndividual individual = new HmrcIndividual(firstName, lastName, TEST_NINO, TEST_DOB);

            // when
            HmrcIndividual normalizedIndividual = accentNameNormalizer.normalizeNames(individual);

            // then
            assertThat(normalizedIndividual.getFirstName())
                    .withFailMessage("Expected `%s` to map to `%s` but was `%s`", firstName, expectedNormalizedFirstName, normalizedIndividual.getFirstName())
                    .isEqualTo(expectedNormalizedFirstName);
            assertContainsNoLetters(normalizedIndividual.getFirstName());
            assertThat(normalizedIndividual.getLastName()).isEqualTo("Smith");

            assertThat(normalizedIndividual.getNino()).isEqualTo(TEST_NINO);
            assertThat(normalizedIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
        }
    }

    @Test
    public void shouldCorrectlyMapAllExpectedUnicodeCharacters() throws Exception {
        for (CharacterMapEntry entry : getExpectedUnicodeMapping()) {
            // given
            Character unicodeCharacter = entry.getKey();
            String expectedReplacement = entry.getValue();

            HmrcIndividual inputIndividual = new HmrcIndividual(unicodeCharacter.toString(), null, TEST_NINO, TEST_DOB);

            // when
            HmrcIndividual outputIndividual = accentNameNormalizer.normalizeNames(inputIndividual);

            // then
            String actualValue = outputIndividual.getFirstName();
            assertThat(actualValue).withFailMessage("Expected `%s` to map to `%s` but was `%s`", unicodeCharacter, expectedReplacement, actualValue)
                    .isEqualTo(expectedReplacement);
        }
    }

    @Test
    public void shouldCorrectlyHandleNullStringLiterals() {
        // given
        String firstName = "nủll";
        String lastName = "null";
        HmrcIndividual individual = new HmrcIndividual(firstName, lastName, TEST_NINO, TEST_DOB);

        // when
        HmrcIndividual normalizedIndividual = accentNameNormalizer.normalizeNames(individual);

        // then
        assertThat(normalizedIndividual.getFirstName()).isEqualTo("null");
        assertThat(normalizedIndividual.getLastName()).isEqualTo("null");

        assertThat(normalizedIndividual.getNino()).isEqualTo(TEST_NINO);
        assertThat(normalizedIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
    }

    private void assertContainsNoLetters(String firstName) {
        for (char character : firstName.toCharArray()) {
            assertThat(Character.isLetter(character))
                    .withFailMessage("Normalized character should not be a letter")
                    .isFalse();
        }
    }

    private List<CharacterMapEntry> getExpectedUnicodeMapping() throws IOException {
        MappingIterator<CharacterMapEntry> mappingIterator = new CsvMapper()
                .readerWithTypedSchemaFor(CharacterMapEntry.class)
                .readValues(getUnicodeMappingFile());

        return mappingIterator.readAll();
    }

    private List<CharacterMapEntry> getExpectedMapForCharactersNotMappingToLetters() throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(CharacterMapEntry.class).withHeader();

        MappingIterator<CharacterMapEntry> mappingIterator = mapper
                .readerFor(CharacterMapEntry.class)
                .with(schema)
                .readValues(getCharactersThatDoNotMapToLettersFile());

        return mappingIterator.readAll();

    }

    private File getUnicodeMappingFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = requireNonNull(classLoader.getResource(UNICODE_MAPPING_CSV_FILENAME));
        return new File(resource.getFile());
    }

    private File getCharactersThatDoNotMapToLettersFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = requireNonNull(classLoader.getResource(CHARACTERS_THAT_DO_NOT_MAP_TO_LETTERS_FILENAME));
        return new File(resource.getFile());
    }

    private static class CharacterMapEntry {
        private char key;
        private String value;

        public char getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}