package uk.gov.digital.ho.pttg

import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import uk.gov.digital.ho.pttg.application.HmrcAccessCodeClient
import uk.gov.digital.ho.pttg.application.HmrcClient
import uk.gov.digital.ho.pttg.dto.Individual

import java.time.LocalDate

class HmrcClientSpec extends Specification {

    static final LocalDate FROM_DATE = LocalDate.of(2016, 6, 21)
    static final LocalDate TO_DATE = LocalDate.of(2016, 8, 1)
    public static final String HMRC_BASE_URL = "http://hmrc.com"
    public static final String ABSOLUTE_URI_WITH_QUERY_PARAMS = HMRC_BASE_URL + "/individuals?existingParam=123"
    public static final String ABSOLUTE_URI_WITH_QUERY_PARAMS_AND_CURLY_BRACES = HMRC_BASE_URL + "/individuals{?existingParam=123}"
    public static final String ABSOLUTE_URL_WITHOUT_URL_QUERY_PARAMS = HMRC_BASE_URL +  "/individuals"
    public RestTemplate restTemplate= Mock(RestTemplate.class)
    public HmrcAccessCodeClient accessCodeClient = Mock(HmrcAccessCodeClient)
    public HmrcClient client

    def setup() {
        client = new HmrcClient(restTemplate, HMRC_BASE_URL,accessCodeClient)
    }

    def "should call HMRC access code client"() {
        when:
            client.getIncome(new Individual("test", "test", LocalDate.of(1980,2,1)),LocalDate.of(2017,11,11),LocalDate.of(2017,12,11) )
        then:
            1 * accessCodeClient.getAccessCode()

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

}
