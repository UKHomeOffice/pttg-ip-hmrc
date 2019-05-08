package uk.gov.digital.ho.pttg;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.gov.digital.ho.pttg.Failable.assertThatExceptionNotThrownBy;

public class FailableTest {

    @Test
    public void assertThatExceptionNotThrownBy_actionThatDoesNotThrow_doesNothing() {
        assertThatExceptionNotThrownBy(() -> {});
    }

    @Test
    public void assertThatExceptionNotThrownBy_actionThatThrows_throws() {
        assertThatExceptionOfType(AssertionError.class)
            .isThrownBy(() -> assertThatExceptionNotThrownBy(() -> {throw new Exception("some exception");}))
            .withMessage("expected action not to throw, but it did!");
    }
}
