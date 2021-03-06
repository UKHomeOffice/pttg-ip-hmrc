package uk.gov.digital.ho.pttg;

import static org.assertj.core.api.Assertions.fail;

@FunctionalInterface
public interface Failable {
    void run() throws Exception;

    static void when_ExceptionThrownBy(Failable action) {
        try {
            action.run();
            fail("expected action to throw, but it did not!");
        } catch (Exception e) {
            // This method is supposed to swallow this exception.
        }
    }
}
