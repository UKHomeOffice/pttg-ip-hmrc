package uk.gov.digital.ho.pttg.application.util;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.junit.Test;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

public class DiacriticNameNormalizerTest {
    private static final String[] UNMAPPED_CHARACTERS = {
            "ǝ",
            "Ʌ",
            "Ɑ",
            "Ɒ",
            "ⱱ",
            "Ⱳ",
            "ⱳ",
            "ⱴ",
            "ⱸ",
            "ⱺ",
            "ⱻ",
            "ⱼ",
            "ⱽ",
            "Ȿ",
            "Ɀ",
            "ẜ",
            "ẝ",
            "Ỻ",
            "ỻ",
            "Ỿ",
            "Ə",
            "ƻ",
            "Ƽ",
            "ƽ",
            "ǀ",
            "ǁ",
            "ǂ",
            "ǃ",
            "Ɂ",
            "ɂ",
            "Ⱶ",
            "ⱶ",
            "ⱷ",
            "ⱹ",
            "ẟ",
            "Ỽ",
            "ỽ",
            "Ƅ",
            "ƅ",
            "Ǝ",
            "Ƨ",
            "ƨ"
    };

    private static final String UNICODE_MAPPING_CSV_FILENAME = "expected_unicode_replacements.csv";

    private static final String TEST_NINO = "Test Nino";
    private static final LocalDate TEST_DOB = LocalDate.of(2000, Month.DECEMBER, 25);

    private final DiacriticNameNormalizer accentNameNormalizer = new DiacriticNameNormalizer();

    @Test
    public void shouldReturnNamesWithAccentsRemoved() {
        // given
        String firstName = "Tĥïŝ ĩš";
        String lastName = "â fůňķŷ Šťŕĭńġ";
        Individual individual = new Individual(firstName, lastName, TEST_NINO, TEST_DOB);

        // when
        Individual normalizedIndividual = accentNameNormalizer.normalizeNames(individual);

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
        String lastName = "null";
        Individual individual = new Individual(firstName, lastName, TEST_NINO, TEST_DOB);

        // when
        Individual normalizedIndividual = accentNameNormalizer.normalizeNames(individual);

        // then
        assertThat(normalizedIndividual.getFirstName()).isEqualTo("Querty");
        assertThat(normalizedIndividual.getLastName()).isEqualTo("null");

        assertThat(normalizedIndividual.getNino()).isEqualTo(TEST_NINO);
        assertThat(normalizedIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
    }

    @Test
    public void shouldRemoveAllNonBasicLatinCharactersThatCannotBeNormalized() {

        for (String firstName : UNMAPPED_CHARACTERS) {
            // given
            String lastName = "Smith";
            Individual individual = new Individual(firstName, lastName, TEST_NINO, TEST_DOB);

            // when
            Individual normalizedIndividual = accentNameNormalizer.normalizeNames(individual);

            // then
            assertThat(normalizedIndividual.getFirstName()).withFailMessage("Expected `%s` to map to `%s` but was `%s`", firstName, "", normalizedIndividual.getFirstName()).isEqualTo("");
            assertThat(normalizedIndividual.getLastName()).isEqualTo("Smith");

            assertThat(normalizedIndividual.getNino()).isEqualTo(TEST_NINO);
            assertThat(normalizedIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
        }
    }

    @Test
    public void shouldReturnEmptyNamesWhenNamesAreEmpty() {
        // given
        String firstName = "";
        String lastName = "";
        Individual individual = new Individual(firstName, lastName, TEST_NINO, TEST_DOB);

        // when
        Individual normalizedIndividual = accentNameNormalizer.normalizeNames(individual);

        // then
        assertThat(normalizedIndividual.getFirstName()).isEmpty();
        assertThat(normalizedIndividual.getLastName()).isEmpty();

        assertThat(normalizedIndividual.getNino()).isEqualTo(TEST_NINO);
        assertThat(normalizedIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
    }

    @Test
    public void shouldReturnNullNamesWhenNamesAreNull() {
        // given
        Individual individual = new Individual(null, null, TEST_NINO, TEST_DOB);

        // when
        Individual normalizedIndividual = accentNameNormalizer.normalizeNames(individual);

        // then
        assertThat(normalizedIndividual.getFirstName()).isNull();
        assertThat(normalizedIndividual.getLastName()).isNull();

        assertThat(normalizedIndividual.getNino()).isEqualTo(TEST_NINO);
        assertThat(normalizedIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
    }

    @Test
    public void shouldCorrectlyMapAllExpectedUnicodeCharacters() throws Exception {
        for (UnicodeMapEntry entry : getExpectedUnicodeMapping()) {
            // given
            Character unicodeCharacter = entry.getKey();
            String expectedReplacement = entry.getValue();

            Individual inputIndividual = new Individual(unicodeCharacter.toString(), "Test", TEST_NINO, TEST_DOB);

            // when
            Individual outputIndividual = accentNameNormalizer.normalizeNames(inputIndividual);

            // then
            String actualValue = outputIndividual.getFirstName();
            assertThat(actualValue).withFailMessage("Expected `%s` to map to `%s` but was `%s`", unicodeCharacter, expectedReplacement, actualValue)
                    .isEqualTo(expectedReplacement);
        }
    }

    private List<UnicodeMapEntry> getExpectedUnicodeMapping() throws IOException {
        MappingIterator<UnicodeMapEntry> mappingIterator = new CsvMapper()
                .readerWithTypedSchemaFor(UnicodeMapEntry.class)
                .readValues(getUnicodeMappingFile());

        return mappingIterator
                .readAll();
    }

    private File getUnicodeMappingFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = requireNonNull(classLoader.getResource(UNICODE_MAPPING_CSV_FILENAME));
        return new File(resource.getFile());
    }

    private static class UnicodeMapEntry {
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