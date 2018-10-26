package uk.gov.digital.ho.pttg.application.util.namenormalizer;

import org.junit.Test;
import uk.gov.digital.ho.pttg.dto.HmrcIndividual;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

public class InvalidCharacterNameNormalizerTest {

    private static final String[] INVALID_CHARACTERS = {
            "@",
            "^",
            "[?]",
            "2",
            "3",
            "5",
            "6",
            "|",
            "||",
            "|=",
            "!",
    };

    private static final String SOME_NINO = "Test Nino";
    private static final LocalDate SOME_DOB = LocalDate.of(2000, Month.DECEMBER, 25);


    private final InvalidCharacterNameNormalizer invalidCharacterNameNormalizer = new InvalidCharacterNameNormalizer();

    @Test
    public void nullNamesShouldNotBeNormalized() {
        HmrcIndividual individual = new HmrcIndividual(null, null, SOME_NINO, SOME_DOB);

        assertThat(invalidCharacterNameNormalizer.normalizeNames(individual)).isEqualTo(individual);
    }

    @Test
    public void shouldRemoveAllNonBasicLatinCharactersThatCannotBeNormalized() {

        for (String firstName : INVALID_CHARACTERS) {
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
        HmrcIndividual individual = new HmrcIndividual("' ", "-'", SOME_NINO, SOME_DOB);
        assertThat(invalidCharacterNameNormalizer.normalizeNames(individual)).isEqualTo(individual);
    }
}