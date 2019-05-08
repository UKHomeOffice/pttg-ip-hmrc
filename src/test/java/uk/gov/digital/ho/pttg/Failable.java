package uk.gov.digital.ho.pttg;

import static org.assertj.core.api.Assertions.fail;

@FunctionalInterface
public interface Failable {
    void run() throws Exception;

    static void assertThatExceptionNotThrownBy(Failable action) {
        try {
            action.run();
        } catch (Exception ex) {
            fail("expected action not to throw, but it did!", ex);
        }
    }
}
