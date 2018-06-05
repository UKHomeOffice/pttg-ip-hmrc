package uk.gov.digital.ho.pttg.application.util;

import org.junit.Before;
import org.junit.Test;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

public class DiacriticNameNormalizerTest {
    private static final String TEST_NINO = "Test Nino";
    private static final LocalDate TEST_DOB = LocalDate.of(2000, Month.DECEMBER, 25);

    private DiacriticNameNormalizer accentNameNormalizer;

    @Before
    public void setUp() {
        accentNameNormalizer = new DiacriticNameNormalizer();
    }

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
        String lastName = "Smith";
        Individual individual = new Individual(firstName, lastName, TEST_NINO, TEST_DOB);

        // when
        Individual normalizedIndividual = accentNameNormalizer.normalizeNames(individual);

        // then
        assertThat(normalizedIndividual.getFirstName()).isEqualTo("Querty");
        assertThat(normalizedIndividual.getLastName()).isEqualTo("Smith");

        assertThat(normalizedIndividual.getNino()).isEqualTo(TEST_NINO);
        assertThat(normalizedIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
    }

    @Test
    public void shouldRemoveAllNonBasicLatinCharactersThatCannotBeNormalized() {
        // given
        String firstName = "ØJohnŁ";
        String lastName = "ƝSmithÆ";
        Individual individual = new Individual(firstName, lastName, TEST_NINO, TEST_DOB);

        // when
        Individual normalizedIndividual = accentNameNormalizer.normalizeNames(individual);

        // then
        assertThat(normalizedIndividual.getFirstName()).isEqualTo("John");
        assertThat(normalizedIndividual.getLastName()).isEqualTo("Smith");

        assertThat(normalizedIndividual.getNino()).isEqualTo(TEST_NINO);
        assertThat(normalizedIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
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
}