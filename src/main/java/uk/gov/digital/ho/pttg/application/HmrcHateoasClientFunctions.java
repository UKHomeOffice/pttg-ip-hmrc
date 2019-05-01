package uk.gov.digital.ho.pttg.application;

import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

final class HmrcHateoasClientFunctions {

    private static final MonthDay END_OF_TAX_YEAR = MonthDay.of(4, 5);
    private static final String QUERY_PARAM_TO_DATE = "toDate";
    private static final String QUERY_PARAM_FROM_DATE = "fromDate";
    private static final String QUERY_PARAM_TO_TAX_YEAR = "toTaxYear";
    private static final String QUERY_PARAM_FROM_TAX_YEAR = "fromTaxYear";

    private HmrcHateoasClientFunctions() {
        throw new UnsupportedOperationException("Companion class for HmrcHateoasClient - do not instatiate.");
    }

    static String buildLinkWithDateRangeQueryParams(LocalDate fromDate, LocalDate toDate, String href) {
        String uri;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(stripPlaceholderQueryParams(href));
        UriComponentsBuilder withFromDate = uriComponentsBuilder.queryParam(QUERY_PARAM_FROM_DATE, fromDate.format(DateTimeFormatter.ISO_DATE));
        if (toDate != null) {
            uri = withFromDate.queryParam(QUERY_PARAM_TO_DATE, toDate.format(DateTimeFormatter.ISO_DATE)).build().toUriString();
        } else {
            uri = withFromDate.build().toUriString();
        }
        return uri;
    }

    static String buildLinkWithTaxYearRangeQueryParams(String fromTaxYear, String toTaxYear, String href) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(stripPlaceholderQueryParams(href));
        uriComponentsBuilder.queryParam(QUERY_PARAM_FROM_TAX_YEAR, fromTaxYear);
        if (toTaxYear != null) {
            uriComponentsBuilder.queryParam(QUERY_PARAM_TO_TAX_YEAR, toTaxYear);
        }
        return uriComponentsBuilder
                .build()
                .toUriString();
    }

    private static String stripPlaceholderQueryParams(String href) {
        return href.replaceFirst("\\{&.*\\}", "");
    }
}
