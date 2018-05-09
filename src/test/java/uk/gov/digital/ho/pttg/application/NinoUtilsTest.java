package uk.gov.digital.ho.pttg.application;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NinoUtilsTest {

    @Test
    public void shouldRedactNino() {

        NinoUtils ninoUtils = new NinoUtils();

        assertThat(ninoUtils.redactedNino("AB123456A")).isEqualTo("AB12***6A");
    }
}