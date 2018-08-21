package uk.gov.digital.ho.pttg.dto;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class AccessCodeTest {

    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    public void shouldBeExpired() {
        AccessCode accessCode = new AccessCode("some code", NOW.minusSeconds(1));
        assertThat(accessCode.hasExpired()).isTrue();
    }

    @Test
    public void shouldNotBeExpired() {
        AccessCode accessCode = new AccessCode("some code", NOW.plusMinutes(1));
        assertThat(accessCode.hasExpired()).isFalse();
    }

}