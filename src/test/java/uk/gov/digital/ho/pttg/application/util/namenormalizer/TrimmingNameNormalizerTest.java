package uk.gov.digital.ho.pttg.application.util.namenormalizer;

import org.junit.Test;
import uk.gov.digital.ho.pttg.dto.HmrcIndividual;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class TrimmingNameNormalizerTest {

    private static final LocalDate SOME_DATE = LocalDate.of(2018, 10, 25);
    private static final String SOME_NINO = "nino";

    private static final String FIRSTNAME_WITHOUT_SPACES = "somefirstname";
    private static final String LASTNAME_WITHOUT_SPACES = "somelastname";
    private static final String FIRST_NAME_INTERNAL_SPACES = "some firstname";
    private static final String LAST_NAME_INTERNAL_SPACES = "some lastname";
    private static final String FIRST_NAME_WITH_LEADING_SPACE = " somefirstname";
    private static final String FIRST_NAME_WITH_TRAILING_SPACE = "somefirstname ";
    private static final String LASTNAME_WITH_LEADING_SPACE = " somelastname";
    private static final String LASTNAME_WITH_TRAILING_SPACE = "somelastname ";

    private static final HmrcIndividual NORMALIZED_INDIVIDUAL = new HmrcIndividual(FIRSTNAME_WITHOUT_SPACES, LASTNAME_WITHOUT_SPACES, SOME_NINO, SOME_DATE);

    private final TrimmingNameNormalizer trimmingNameNormalizer = new TrimmingNameNormalizer();

    @Test
    public void shouldReturnUnalteredIndividualWhenNothingToTrim() {
        HmrcIndividual hmrcIndividual = new HmrcIndividual(FIRSTNAME_WITHOUT_SPACES, LASTNAME_WITHOUT_SPACES, SOME_NINO, SOME_DATE);
        assertThat(trimmingNameNormalizer.normalizeNames(hmrcIndividual)).isEqualTo(hmrcIndividual);
    }

    @Test
    public void shouldReturnUnalteredIndividualWhenSpacesAreBetweenWords() {
        HmrcIndividual hmrcIndividual = new HmrcIndividual(FIRST_NAME_INTERNAL_SPACES, LAST_NAME_INTERNAL_SPACES, SOME_NINO, SOME_DATE);
        assertThat(trimmingNameNormalizer.normalizeNames(hmrcIndividual)).isEqualTo(hmrcIndividual);
    }

    @Test
    public void shouldRemoveLeadingSpacesInFirstName() {
        HmrcIndividual hmrcIndividual = new HmrcIndividual(FIRST_NAME_WITH_LEADING_SPACE, LASTNAME_WITHOUT_SPACES, SOME_NINO, SOME_DATE);
        assertThat(trimmingNameNormalizer.normalizeNames(hmrcIndividual)).isEqualTo(NORMALIZED_INDIVIDUAL);
    }

    @Test
    public void shouldRemoveTrailingSpacesInFirstName() {
        HmrcIndividual hmrcIndividual = new HmrcIndividual(FIRST_NAME_WITH_TRAILING_SPACE, LASTNAME_WITHOUT_SPACES, SOME_NINO, SOME_DATE);
        assertThat(trimmingNameNormalizer.normalizeNames(hmrcIndividual)).isEqualTo(NORMALIZED_INDIVIDUAL);
    }

    @Test
    public void shouldRemoveLeadingSpacesInLastName() {
        HmrcIndividual hmrcIndividual = new HmrcIndividual(FIRSTNAME_WITHOUT_SPACES, LASTNAME_WITH_LEADING_SPACE, SOME_NINO, SOME_DATE);
        assertThat(trimmingNameNormalizer.normalizeNames(hmrcIndividual)).isEqualTo(NORMALIZED_INDIVIDUAL);
    }

    @Test
    public void shouldRemoveTrailingSpacesInLastName() {
        HmrcIndividual hmrcIndividual = new HmrcIndividual(FIRSTNAME_WITHOUT_SPACES, LASTNAME_WITH_TRAILING_SPACE, SOME_NINO, SOME_DATE);
        assertThat(trimmingNameNormalizer.normalizeNames(hmrcIndividual)).isEqualTo(NORMALIZED_INDIVIDUAL);
    }

    @Test
    public void shouldNotStripInternalSpaces() {
        HmrcIndividual hmrcIndividual = new HmrcIndividual(" some first  name ", "  some last name ", SOME_NINO, SOME_DATE);
        HmrcIndividual expected = new HmrcIndividual("some first  name", "some last name", SOME_NINO, SOME_DATE);
        assertThat(trimmingNameNormalizer.normalizeNames(hmrcIndividual)).isEqualTo(expected);
    }
}