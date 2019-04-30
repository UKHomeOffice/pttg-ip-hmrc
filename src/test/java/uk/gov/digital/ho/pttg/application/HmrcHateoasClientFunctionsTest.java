package uk.gov.digital.ho.pttg.application;

import org.junit.Before;
import org.junit.Test;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;
import uk.gov.digital.ho.pttg.application.namematching.NameMatchingCandidatesService;
import uk.gov.digital.ho.pttg.application.util.namenormalizer.NameNormalizer;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.digital.ho.pttg.application.HmrcHateoasClientFunctions.*;

public class HmrcHateoasClientFunctionsTest {

    private static final LocalDate FROM_DATE = LocalDate.of(2016, 6, 21);
    private static final LocalDate TO_DATE = LocalDate.of(2016, 8, 1);
    private static final String HMRC_BASE_URL = "http://hmrc.com";
    private static final String ABSOLUTE_URI_WITH_QUERY_PARAMS = HMRC_BASE_URL + "/individuals?existingParam=123";
    private static final String ABSOLUTE_URI_WITH_QUERY_PARAMS_AND_PLACEHOLDER_PARAMS = HMRC_BASE_URL + "/individuals?existingParam=123{&toDate,fromDate}";
    private static final String ABSOLUTE_URI_WITH_QUERY_PARAMS_AND_PLACEHOLDER_PARAMS_TAX_YEAR = HMRC_BASE_URL + "/individuals?existingParam=123{&toTaxYear,fromTaxYear}";
    private static final String ABSOLUTE_URL_WITHOUT_URL_QUERY_PARAMS = HMRC_BASE_URL + "/individuals";
    private static final LocalDate DATE_5_APRIL_2015 = LocalDate.of(2015, 4, 5);
    private static final LocalDate DATE_1_MAY_2013 = LocalDate.of(2013, 5, 1);
    private static final LocalDate DATE_6_APRIL_2011 = LocalDate.of(2011, 4, 6);

    @Test
    public void shouldRetainAnyReturnedQueryParamsFromAbsoluteUrl() {
        String link = buildLinkWithDateRangeQueryParams(FROM_DATE, TO_DATE, ABSOLUTE_URI_WITH_QUERY_PARAMS);
        assertThat(link)
                .isEqualTo(HMRC_BASE_URL + "/individuals?existingParam=123&fromDate=2016-06-21&toDate=2016-08-01");
    }

    @Test
    public void shouldRetainAnyReturnedQueryParamsFromAbsoluteUrl_forTaxYearParams() {
        String link = buildLinkWithTaxYearRangeQueryParams(FROM_DATE, TO_DATE, ABSOLUTE_URI_WITH_QUERY_PARAMS_AND_PLACEHOLDER_PARAMS_TAX_YEAR);
        assertThat(link)
                .isEqualTo(HMRC_BASE_URL + "/individuals?existingParam=123&fromTaxYear=2016-17&toTaxYear=2016-17");
    }

    @Test
    public void shouldRetainAnyReturnedQueryParamsFromAabsoluteUrlAndReplaceDateTemplatedQueryParams() {
        String link = buildLinkWithDateRangeQueryParams(FROM_DATE, TO_DATE, ABSOLUTE_URI_WITH_QUERY_PARAMS_AND_PLACEHOLDER_PARAMS);
        assertThat(link)
                .isEqualTo(HMRC_BASE_URL + "/individuals?existingParam=123&fromDate=2016-06-21&toDate=2016-08-01");
    }

    @Test
    public void shouldAddQueryParamsToAbsoluteUrlWhichDoesNotAlreadyHaveQueryParams() {
        String link = buildLinkWithDateRangeQueryParams(FROM_DATE, TO_DATE, ABSOLUTE_URL_WITHOUT_URL_QUERY_PARAMS);
        assertThat(link).isEqualTo(HMRC_BASE_URL + "/individuals?fromDate=2016-06-21&toDate=2016-08-01");
    }

    @Test
    public void shouldExcludeToDateIfNotProvided() {
        String link = buildLinkWithDateRangeQueryParams(FROM_DATE, null, ABSOLUTE_URL_WITHOUT_URL_QUERY_PARAMS);
        assertThat(link).isEqualTo(HMRC_BASE_URL + "/individuals?fromDate=2016-06-21");
    }

    @Test
    public void shouldReturnTaxYearFromDate() {
        String taxYear1 = getTaxYear(DATE_5_APRIL_2015);
        String taxYear2 = getTaxYear(DATE_1_MAY_2013);
        String taxYear3 = getTaxYear(DATE_6_APRIL_2011);

        assertThat(taxYear1).isEqualTo("2014-15");
        assertThat(taxYear2).isEqualTo("2013-14");
        assertThat(taxYear3).isEqualTo("2011-12");
    }

}
