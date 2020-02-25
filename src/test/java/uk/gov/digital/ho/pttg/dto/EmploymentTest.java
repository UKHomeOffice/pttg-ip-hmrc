package uk.gov.digital.ho.pttg.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmploymentTest {

    @Test
    public void withoutEmployer_noEmployer_true() {
        assertThat(new Employment(null, null, null, null).withoutEmployer()).isTrue();
    }

    @Test
    public void withoutEmployer_Employer_false() {
        assertThat(new Employment(null, null, null, new Employer(null, null, null)).withoutEmployer()).isFalse();
    }

}
