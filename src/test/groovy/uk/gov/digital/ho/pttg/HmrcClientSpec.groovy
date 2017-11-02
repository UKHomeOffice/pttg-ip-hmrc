package uk.gov.digital.ho.pttg

import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import uk.gov.digital.ho.pttg.application.HmrcClient

import java.time.LocalDate

class HmrcClientSpec extends Specification {

    private static final LocalDate FROM_DATE = LocalDate.of(2016, 6, 21)
    private static final LocalDate TO_DATE = LocalDate.of(2016, 8, 1)
    private static final String HMRC_BASE_URL = "http://hmrc.com"
    private static final String ABSOLUTE_URI_WITH_QUERY_PARAMS = HMRC_BASE_URL + "/individuals?existingParam=123"
    private static final String ABSOLUTE_URI_WITH_QUERY_PARAMS_AND_CURLY_BRACES = HMRC_BASE_URL + "/individuals{?existingParam=123}"
    private static final String ABSOLUTE_URL_WITHOUT_URL_QUERY_PARAMS = HMRC_BASE_URL +  "/individuals"

    public RestTemplate mockRestTemplate = Mock(RestTemplate.class)

    public HmrcClient client

    def setup() {
        client = new HmrcClient(mockRestTemplate, HMRC_BASE_URL)
    }

    def 'should replace any returned query params from absolute url'() {
        when:
            def link = client.buildLinkWithDateRangeQueryParams(FROM_DATE, TO_DATE, ABSOLUTE_URI_WITH_QUERY_PARAMS)
        then:
            link == HMRC_BASE_URL + '/individuals?fromDate=2016-06-21&toDate=2016-08-01'
    }

    def 'should strip curly braces from url'() {
        when:
            def link = client.buildLinkWithDateRangeQueryParams(FROM_DATE, TO_DATE, ABSOLUTE_URI_WITH_QUERY_PARAMS_AND_CURLY_BRACES)
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

    def "should rethrow exception when retry limit reached cos of RestClientException"() {

        when:
            client.getIncomeRetryFailureRecovery(new HttpServerErrorException(HttpStatus.BAD_GATEWAY, "test"))
        then:
        HttpServerErrorException e = thrown()
            e.message == "502 test"
    }

}
