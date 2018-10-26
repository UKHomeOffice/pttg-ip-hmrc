package uk.gov.digital.ho.pttg.application.util.namenormalizer;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.google.common.collect.ImmutableMap;
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
    private static final Map<String, String> CHARACTERS_THAT_DO_NOT_MAP_TO_LETTERS = ImmutableMap.<String, String>builder()
            .put("ǝ", "@")
            .put("Ʌ", "^")
            .put("Ɑ", "")
            .put("Ɒ", "")
            .put("ⱱ", "")
            .put("Ⱳ ", "")
            .put("ⱳ", "")
            .put("ⱴ", "")
            .put("ⱸ", "")
            .put("ⱺ", "")
            .put("ⱻ", "")
            .put("ⱼ", "")
            .put("ⱽ", "")
            .put("Ȿ", "")
            .put("Ɀ", "")
            .put("ẜ", "[?]")
            .put("ẝ", "[?]")
            .put("Ỻ", "[?]")
            .put("ỻ", "[?]")
            .put("Ỿ", "[?]")
            .put("Ə", "@")
            .put("ƻ", "2")
            .put("Ƽ", "5")
            .put("ƽ", "5")
            .put("ǀ", "|")
            .put("ǁ", "||")
            .put("ǂ", "|=")
            .put("ǃ", "!")
            .put("Ɂ", "[?]")
            .put("ɂ", "[?]")
            .put("Ⱶ", "")
            .put("ⱶ", "")
            .put("ⱷ", "")
            .put("ⱹ", "")
            .put("ẟ", "[?]")
            .put("Ỽ", "[?]")
            .put("ỽ", "[?]")
            .put("Ƅ", "6")
            .put("ƅ", "6")
            .put("Ǝ", "3")
            .put("Ƨ", "2")
            .put("ƨ", "2")
            .build();

    private static final String UNICODE_MAPPING_CSV_FILENAME = "expected_unicode_replacements.csv";

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
    public void shouldRetainAllNonBasicLatinCharactersThatCannotBeNormalized() {

        for (Map.Entry<String, String> mappingEntry : CHARACTERS_THAT_DO_NOT_MAP_TO_LETTERS.entrySet()) {
            // given
            String firstName = mappingEntry.getKey();
            String expectedNormalizedFirstName = mappingEntry.getValue();

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
        for (UnicodeMapEntry entry : getExpectedUnicodeMapping()) {
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