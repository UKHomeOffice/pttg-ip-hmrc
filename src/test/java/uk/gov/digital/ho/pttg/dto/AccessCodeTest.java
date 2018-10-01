package uk.gov.digital.ho.pttg.dto;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class AccessCodeTest {

    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final LocalDateTime ANY_TIME = LocalDateTime.MAX;

    @Test
    public void shouldBeExpired() {
        AccessCode accessCode = new AccessCode("some code", NOW.minusSeconds(1), ANY_TIME);
        assertThat(accessCode.hasExpired()).isTrue();
    }

    @Test
    public void shouldNotBeExpired() {
        AccessCode accessCode = new AccessCode("some code", NOW.plusMinutes(1), ANY_TIME);
        assertThat(accessCode.hasExpired()).isFalse();
    }

    @Test
    public void shouldBeExpiredRefreshed() {
        AccessCode accessCode = new AccessCode("some code", ANY_TIME, NOW.minusSeconds(1));
        assertThat(accessCode.needsRefreshing()).isTrue();
    }

    @Test
    public void shouldNotBeRefreshed() {
        AccessCode accessCode = new AccessCode("some code", ANY_TIME, NOW.plusMinutes(1));
        assertThat(accessCode.needsRefreshing()).isFalse();
    }

}