package uk.gov.digital.ho.pttg

import spock.lang.Specification
import uk.gov.digital.ho.pttg.api.RequestHeaderData
import uk.gov.digital.ho.pttg.application.HmrcCallWrapper
import uk.gov.digital.ho.pttg.application.HmrcHateoasClient
import uk.gov.digital.ho.pttg.application.namematching.NameMatchingCandidatesService
import uk.gov.digital.ho.pttg.application.util.namenormalizer.NameNormalizer

import java.time.LocalDate

class HmrcHateoasClientSpec extends Specification {

    private static final LocalDate FROM_DATE = LocalDate.of(2016, 6, 21)
    private static final LocalDate TO_DATE = LocalDate.of(2016, 8, 1)
    private static final String HMRC_API_VERSION = "hmrc.api.version=application/vnd.hmrc.P1.0+json"
    private static final String HMRC_BASE_URL = "http://hmrc.com"
    private static final String ABSOLUTE_URI_WITH_QUERY_PARAMS = HMRC_BASE_URL + "/individuals?existingParam=123"
    private static final String ABSOLUTE_URI_WITH_QUERY_PARAMS_AND_PLACEHOLDER_PARAMS = HMRC_BASE_URL + "/individuals?existingParam=123{&toDate,fromDate}"
    private static final String ABSOLUTE_URI_WITH_QUERY_PARAMS_AND_PLACEHOLDER_PARAMS_TAX_YEAR = HMRC_BASE_URL + "/individuals?existingParam=123{&toTaxYear,fromTaxYear}"
    private static final String ABSOLUTE_URL_WITHOUT_URL_QUERY_PARAMS = HMRC_BASE_URL +  "/individuals"
    private static final LocalDate DATE_5_APRIL_2015 = LocalDate.of(2015, 4, 5)
    private static final LocalDate DATE_1_MAY_2013 = LocalDate.of(2013, 5, 1)
    private static final LocalDate DATE_6_APRIL_2011 = LocalDate.of(2011, 4, 6)

    private NameNormalizer mockNameNormalizer = Mock(NameNormalizer.class)
    private HmrcCallWrapper mockHmrcCallWrapper = Mock(HmrcCallWrapper.class)
    private RequestHeaderData mockRequestHeaderData = Mock(RequestHeaderData.class)
    private NameMatchingCandidatesService mockNameMatchingCandidatesService = Mock(NameMatchingCandidatesService.class)

    public HmrcHateoasClient client

    def setup() {
        client = new HmrcHateoasClient(mockRequestHeaderData, mockNameNormalizer, mockHmrcCallWrapper, mockNameMatchingCandidatesService, HMRC_BASE_URL)
    }

    def 'should retain any returned query params from absolute url'() {
        when:
            def link = client.buildLinkWithDateRangeQueryParams(FROM_DATE, TO_DATE, ABSOLUTE_URI_WITH_QUERY_PARAMS)
        then:
            link == HMRC_BASE_URL + '/individuals?existingParam=123&fromDate=2016-06-21&toDate=2016-08-01'
    }

    def 'should retain any returned query params from absolute url -  for taxYear params'() {
        when:
        def link = client.buildLinkWithTaxYearRangeQueryParams(FROM_DATE, TO_DATE, ABSOLUTE_URI_WITH_QUERY_PARAMS_AND_PLACEHOLDER_PARAMS_TAX_YEAR)
        then:
        link == HMRC_BASE_URL + '/individuals?existingParam=123&fromTaxYear=2016-17&toTaxYear=2016-17'
    }

    def 'should retain any returned query params from absolute url and replace date templated query params'() {
        when:
        def link = client.buildLinkWithDateRangeQueryParams(FROM_DATE, TO_DATE, ABSOLUTE_URI_WITH_QUERY_PARAMS_AND_PLACEHOLDER_PARAMS)
        then:
        link == HMRC_BASE_URL + '/individuals?existingParam=123&fromDate=2016-06-21&toDate=2016-08-01'
    }

    def 'should add query params to absolute url which does not already have query params'() {
        when:
            def link = client.buildLinkWithDateRangeQueryParams(FROM_DATE, TO_DATE, ABSOLUTE_URL_WITHOUT_URL_QUERY_PARAMS)
        then:
            link == HMRC_BASE_URL + '/individuals?fromDate=2016-06-21&toDate=2016-08-01'
    }

    def 'should exclude toDate if not provided'() {
        when:
            def link = client.buildLinkWithDateRangeQueryParams(FROM_DATE, null, ABSOLUTE_URL_WITHOUT_URL_QUERY_PARAMS)
        then:
            link == HMRC_BASE_URL + '/individuals?fromDate=2016-06-21'
    }

    def 'should return tax Year from Date'() {
        when:
        def taxYear1 = client.getTaxYear(DATE_5_APRIL_2015)
        def taxYear2 = client.getTaxYear(DATE_1_MAY_2013)
        def taxYear3 = client.getTaxYear(DATE_6_APRIL_2011)
        then:
        taxYear1 == "2014-15"
        taxYear2 == "2013-14"
        taxYear3 == "2011-12"
    }

}
