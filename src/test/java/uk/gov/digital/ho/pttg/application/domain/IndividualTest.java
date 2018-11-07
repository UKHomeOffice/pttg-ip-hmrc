package uk.gov.digital.ho.pttg.application.domain;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.InvalidIdentityException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class IndividualTest {

    @Test
    public void shouldAllowInstantiationWithBothNames() {
        assertThat(new Individual("first", "last", null, null, "")).isNotNull();
    }

    @Test
    public void shouldAllowInstantiationWithJustFirstName() {
        assertThat(new Individual("first", "", null, null, "")).isNotNull();
    }

    @Test
    public void shouldAllowInstantiationWithJustLastName() {
        assertThat(new Individual(null, "last", null, null, "")).isNotNull();
    }

    @Test
    public void shouldThrowExceptionWhenInstantiationWithNoNames() {
        assertThatThrownBy(() -> new Individual(null, null, null, null, ""))
                .isInstanceOf(InvalidIdentityException.class);
    }

    @Test
    public void shouldThrowExceptionWhenInstantiationWithEmpty() {
        assertThatThrownBy(() -> new Individual("", "", null, null, ""))
                .isInstanceOf(InvalidIdentityException.class);
    }

    @Test
    public void aliasSurnamesShouldBeEmptyStringWhenConstructedWithNull() {
        Individual individual = new Individual("first", "last", null, null, null);
        assertThat(individual.getAliasSurnames()).isEqualTo("");
    }
    @Test

    public void aliasSurnamesShouldBeInputString() {
        Individual individual = new Individual("first", "last", null, null, "alias surnames");
        assertThat(individual.getAliasSurnames()).isEqualTo("alias surnames");
    }
}