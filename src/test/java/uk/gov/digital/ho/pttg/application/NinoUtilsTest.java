package uk.gov.digital.ho.pttg.application;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.InvalidNationalInsuranceNumberException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class NinoUtilsTest {
    private static final String[] VALID_NINOS_VALID_FORMAT = new String[]{
            "PP200000A",
            "PP210000A",
            "PP300000A",
            "PP310000A",
            "PP420000A",
            "SP100000A",
            "SP200000A",
            "SP400000A",
            "SP410000A",
            "SP420000A"
    };

    private static final String[] VALID_NINOS_INVALID_FORMAT = new String[]{
            "pp200000a",
            "PP210000A",
            "pp 3000 00a",
            "PP 3100 00A",
            "  pp420000a  ",
            "  SP100000 A  ",
            " s p 2 0 0 0 0 0 a\r",
            " S P 4 0 0 0 0 0 A\r",
            "\tsp  41  00  00  a",
            "\tSP  42  00  00  A"
    };

    private static final String[] VALID_NINOS_REDACTED = new String[]{
            "PP200****",
            "PP210****",
            "PP300****",
            "PP310****",
            "PP420****",
            "SP100****",
            "SP200****",
            "SP400****",
            "SP410****",
            "SP420****"
    };

    private static final String[] INVALID_NINOS = {
            null,
            "",
            "1234567890",
            "abc45678d",
            "ab34567c9",
            "ab3456789",
            "BG123456A",
            "GB123456A",
            "NK123456A",
            "KN123456A",
            "TN123456A",
            "NT123456A",
            "ZZ123456A",
            "AA123456E",
            "AA12",
            "SP123",
            "PA5432"
    };

    private static final String[] INVALID_NINOS_REDACTED = {
            null,
            "",
            "12345*****",
            "ABC45****",
            "AB345****",
            "AB345****",
            "BG123****",
            "GB123****",
            "NK123****",
            "KN123****",
            "TN123****",
            "NT123****",
            "ZZ123****",
            "AA123****",
            "AA12",
            "SP123",
            "PA543*"
    };

    private final NinoUtils ninoUtils = new NinoUtils();

    @Test
    public void shouldCorrectlySanitiseValidNinosWithInvalidFormats() {
        for (int i = 0; i < VALID_NINOS_VALID_FORMAT.length; i++) {
            final String invalidFormat = VALID_NINOS_INVALID_FORMAT[i];
            final String validFormat = VALID_NINOS_VALID_FORMAT[i];

            assertThat(ninoUtils.sanitise(invalidFormat)).isEqualTo(validFormat);
        }
    }

    @Test
    public void shouldReturnNullWhenNullPassedToSanitise() {
        assertThat(ninoUtils.sanitise(null)).isNull();
    }

    @Test
    public void shouldRemoveWhitespaceWhenSanitised() {
        assertThat(ninoUtils.sanitise(" N R ")).isEqualTo("NR");
    }

    @Test
    public void shouldMakeUpperCaseWhenSanitised() {
        assertThat(ninoUtils.sanitise("aB1cDe")).isEqualTo("AB1CDE");
    }

    @Test
    public void shouldThrowExceptionWhenInvalidNino() {
        for (final String invalidNino : INVALID_NINOS) {
            assertThatThrownBy(() -> ninoUtils.validate(invalidNino)).isInstanceOf(InvalidNationalInsuranceNumberException.class);
        }
    }

    @Test
    public void shouldNotThrowExceptionWhenValidNinosValidated() {
        for (final String validNino : VALID_NINOS_VALID_FORMAT) {
            ninoUtils.validate(validNino);
        }
    }

    @Test
    public void shouldRedactInvalidNinos() {
        for (int i = 0; i < INVALID_NINOS.length; i++) {
            final String invalidNino = INVALID_NINOS[i];
            final String redactedNino = INVALID_NINOS_REDACTED[i];

            assertThat(ninoUtils.redact(invalidNino)).isEqualTo(redactedNino);
        }
    }

    @Test
    public void shouldRedactValidNinosWhenCorrectlyFormatted() {
        for (int i = 0; i < VALID_NINOS_VALID_FORMAT.length; i++) {
            final String validNino = VALID_NINOS_VALID_FORMAT[i];
            final String redactedNino = VALID_NINOS_REDACTED[i];

            assertThat(ninoUtils.redact(validNino)).isEqualTo(redactedNino);
        }
    }

    @Test
    public void shouldRedactValidNinosWhenIncorrectlyFormatted() {
        for (int i = 0; i < VALID_NINOS_VALID_FORMAT.length; i++) {
            final String validNino = VALID_NINOS_INVALID_FORMAT[i];
            final String redactedNino = VALID_NINOS_REDACTED[i];

            assertThat(ninoUtils.redact(validNino)).isEqualTo(redactedNino);
        }
    }
}