package uk.gov.digital.ho.pttg.api;


import org.junit.Test;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.CircuitBreakerException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CircuitBreakerTest {

    @Test
    public void shouldBreakTheCircuit() {
        CircuitBreaker circuitBreaker = new CircuitBreaker(4);

        circuitBreaker.check();
        circuitBreaker.check();
        circuitBreaker.check();

        assertThatThrownBy(circuitBreaker::check)
                .isInstanceOf(CircuitBreakerException.class);
    }
}