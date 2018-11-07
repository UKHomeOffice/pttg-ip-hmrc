package uk.gov.digital.ho.pttg.audit;

import org.junit.Test;
import uk.gov.digital.ho.pttg.application.domain.Individual;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditIndividualDataTest {
    private static final String TEST_METHOD = "TestMethod";
    private static final String TEST_FIRST_NAME = "TestFirstName";
    private static final String TEST_LAST_NAME = "TestLastName";
    private static final String TEST_NINO = "TestNino";
    private static final LocalDate TEST_DATE_OF_BIRTH = LocalDate.of(1990, Month.DECEMBER, 25);

    @Test
    public void shouldCorrectlyConstructAuditIndividualDataFromIndividual() {
        // given
        final Individual individual = new Individual(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_NINO, TEST_DATE_OF_BIRTH, "");

        // when
        final AuditIndividualData auditIndividualData = new AuditIndividualData(TEST_METHOD, individual);

        // then
        // verify method property correctly set
        assertThat(auditIndividualData.getMethod()).isEqualTo(TEST_METHOD);

        // verify individual properties correctly set
        assertThat(auditIndividualData.getForename()).isEqualTo(TEST_FIRST_NAME);
        assertThat(auditIndividualData.getSurname()).isEqualTo(TEST_LAST_NAME);
        assertThat(auditIndividualData.getNino()).isEqualTo(TEST_NINO);
        assertThat(auditIndividualData.getDateOfBirth()).isEqualTo(TEST_DATE_OF_BIRTH);
    }
}