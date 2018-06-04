package uk.gov.digital.ho.pttg.application.util;

import org.junit.Test;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

public class MaxLengthNameNormalizerTest {
    private static final int MAX_NAME_LENGTH = 35;
    private static final String TEST_NINO = "TestNino";
    private static final LocalDate TEST_DOB = LocalDate.of(2000, Month.DECEMBER, 25);

    private MaxLengthNameNormalizer maxLengthNameNormalizer;

    @Test
    public void shouldNotTruncateEitherNameIfUnderMaxLength() {
        // given
        maxLengthNameNormalizer = new MaxLengthNameNormalizer(MAX_NAME_LENGTH);

        String firstName = "Janice";
        String lastName = "Smith";
        Individual individual = new Individual(firstName, lastName, TEST_NINO, TEST_DOB);

        // when
        Individual actualIndividual = maxLengthNameNormalizer.normalizeNames(individual);

        // then
        assertThat(actualIndividual.getFirstName()).isEqualTo("Janice");
        assertThat(actualIndividual.getLastName()).isEqualTo("Smith");

        assertThat(actualIndividual.getNino()).isEqualTo(TEST_NINO);
        assertThat(actualIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
    }

    @Test
    public void shouldTruncateFirstNameThatIsTooLong() {
        // given
        maxLengthNameNormalizer = new MaxLengthNameNormalizer(MAX_NAME_LENGTH);

        String firstName = "Qawsedrftgyhujikolpqawsedrftgyhujiko";
        String lastName = "Smith";
        Individual individual = new Individual(firstName, lastName, TEST_NINO, TEST_DOB);

        // when
        Individual actualIndividual = maxLengthNameNormalizer.normalizeNames(individual);

        // then
        assertThat(actualIndividual.getFirstName()).isEqualTo("Qawsedrftgyhujikolpqawsedrftgyhujik");
        assertThat(actualIndividual.getLastName()).isEqualTo("Smith");

        assertThat(actualIndividual.getNino()).isEqualTo(TEST_NINO);
        assertThat(actualIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
    }

    @Test
    public void shouldTruncateLastNameThatIsTooLong() {
        // given
        maxLengthNameNormalizer = new MaxLengthNameNormalizer(MAX_NAME_LENGTH);

        String firstName = "Janice";
        String lastName = "Keihanaikukauakahihuliheekahaunaeles";
        Individual individual = new Individual(firstName, lastName, TEST_NINO, TEST_DOB);

        // when
        Individual actualIndividual = maxLengthNameNormalizer.normalizeNames(individual);

        // then
        assertThat(actualIndividual.getFirstName()).isEqualTo("Janice");
        assertThat(actualIndividual.getLastName()).isEqualTo("Keihanaikukauakahihuliheekahaunaele");

        assertThat(actualIndividual.getNino()).isEqualTo(TEST_NINO);
        assertThat(actualIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
    }

    @Test
    public void shouldNotTruncateFirstNameThatIsMaxAllowedLength() {
        // given
        maxLengthNameNormalizer = new MaxLengthNameNormalizer(MAX_NAME_LENGTH);

        String firstName = "qqqqqwwwwweeeeerrrrrtttttyyyyyuuuuu";
        String lastName = "Smith";
        Individual individual = new Individual(firstName, lastName, TEST_NINO, TEST_DOB);

        // when
        Individual actualIndividual = maxLengthNameNormalizer.normalizeNames(individual);

        // then
        assertThat(actualIndividual.getFirstName()).isEqualTo("qqqqqwwwwweeeeerrrrrtttttyyyyyuuuuu");
        assertThat(actualIndividual.getLastName()).isEqualTo("Smith");

        assertThat(actualIndividual.getNino()).isEqualTo(TEST_NINO);
        assertThat(actualIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
    }

    @Test
    public void shouldNotTruncateLastNameThatIsMaxAllowedLength() {
        // given
        maxLengthNameNormalizer = new MaxLengthNameNormalizer(MAX_NAME_LENGTH);

        String firstName = "Janice";
        String lastName = "qwertqwertqwertqwertqwertqwertqwert";
        Individual individual = new Individual(firstName, lastName, TEST_NINO, TEST_DOB);

        // when
        Individual actualIndividual = maxLengthNameNormalizer.normalizeNames(individual);

        // then
        assertThat(actualIndividual.getFirstName()).isEqualTo("Janice");
        assertThat(actualIndividual.getLastName()).isEqualTo("qwertqwertqwertqwertqwertqwertqwert");

        assertThat(actualIndividual.getNino()).isEqualTo(TEST_NINO);
        assertThat(actualIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
    }

    @Test
    public void shouldReturnEmptyNamesIfNamesAreEmpty() {
        // given
        maxLengthNameNormalizer = new MaxLengthNameNormalizer(MAX_NAME_LENGTH);

        String firstName = "";
        String lastName = "";
        Individual individual = new Individual(firstName, lastName, TEST_NINO, TEST_DOB);

        // when
        Individual actualIndividual = maxLengthNameNormalizer.normalizeNames(individual);

        // then
        assertThat(actualIndividual.getFirstName()).isEmpty();
        assertThat(actualIndividual.getLastName()).isEmpty();

        assertThat(actualIndividual.getNino()).isEqualTo(TEST_NINO);
        assertThat(actualIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
    }

    @Test
    public void shouldReturnNullIfNameIsNull() {
        // given
        maxLengthNameNormalizer = new MaxLengthNameNormalizer(MAX_NAME_LENGTH);

        Individual individual = new Individual(null, null, TEST_NINO, TEST_DOB);

        // when
        Individual actualIndividual = maxLengthNameNormalizer.normalizeNames(individual);

        // then
        assertThat(actualIndividual.getFirstName()).isNull();
        assertThat(actualIndividual.getLastName()).isNull();

        assertThat(actualIndividual.getNino()).isEqualTo(TEST_NINO);
        assertThat(actualIndividual.getDateOfBirth()).isEqualTo(TEST_DOB);
    }
}