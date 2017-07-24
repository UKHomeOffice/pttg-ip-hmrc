package uk.gov.digital.ho.pttg

import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import java.time.LocalDate

class HmrcClientSpec extends Specification {

    static final LocalDate FROM_DATE = LocalDate.of(2016, 6, 21)
    static final LocalDate TO_DATE = LocalDate.of(2016, 8, 1)
    public static final String HMRC_BASE_URL = "http://hmrc.com"
    public static final String HMRC_ACCESS_CODE_BASE_URL = "http://internal-access-code-service"
    public static final String ABSOLUTE_URI_WITH_QUERY_PARAMS = HMRC_BASE_URL + "/individuals?existingParam=123"
    public static final String ABSOLUTE_URL_WITHOUT_URL_QUERY_PARAMS = HMRC_BASE_URL +  "/individuals"
    public RestTemplate restTemplate= Mock(RestTemplate.class)
    public HmrcClient client

    def setup() {
        client = new HmrcClient(restTemplate, HMRC_BASE_URL, HMRC_ACCESS_CODE_BASE_URL)
    }

    def 'should replace any returned query params from absolute url'() {
        when:
        def link = client.buildLinkWithDateRangeQueryParams(FROM_DATE, TO_DATE, ABSOLUTE_URI_WITH_QUERY_PARAMS)
        then:
        link == HMRC_BASE_URL + '/individuals?fromDate=2016-06-21&toDate=2016-08-01'
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
}
