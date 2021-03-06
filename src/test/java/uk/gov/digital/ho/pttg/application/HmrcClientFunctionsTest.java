package uk.gov.digital.ho.pttg.application;

import org.junit.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.HmrcClientFunctions.getTaxYear;

public class HmrcClientFunctionsTest {

    private static final LocalDate DATE_5_APRIL_2015 = LocalDate.of(2015, 4, 5);
    private static final LocalDate DATE_1_MAY_2013 = LocalDate.of(2013, 5, 1);
    private static final LocalDate DATE_6_APRIL_2011 = LocalDate.of(2011, 4, 6);

    @Test
    public void shouldReturnTaxYearFromDate() {
        String taxYear1 = getTaxYear(DATE_5_APRIL_2015);
        String taxYear2 = getTaxYear(DATE_1_MAY_2013);
        String taxYear3 = getTaxYear(DATE_6_APRIL_2011);

        assertThat(taxYear1).isEqualTo("2014-15");
        assertThat(taxYear2).isEqualTo("2013-14");
        assertThat(taxYear3).isEqualTo("2011-12");
    }

    @Test
    public void getTaxYear_dateBefore2010_returnTaxYear() {
        assertThat(getTaxYear(LocalDate.of(2001, 1, 1)))
                .isEqualTo("2000-01");
    }

    @Test
    public void getTaxYear_dateAfter2100_returnTaxYear() {
        assertThat(getTaxYear(LocalDate.of(2100, 1, 1)))
                .isEqualTo("2099-00");
    }

    @Test
    public void getTaxYear_taxYear1999_2000_returnTaxYear() {
        assertThat(getTaxYear(LocalDate.of(2000, 1, 1)))
                .isEqualTo("1999-00");
    }
}