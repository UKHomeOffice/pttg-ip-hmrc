package uk.gov.digital.ho.pttg

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.junit.WireMockClassRule
import groovy.json.JsonSlurper
import org.apache.commons.io.IOUtils
import org.junit.Rule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import uk.gov.digital.ho.pttg.dto.AuthToken

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(locations = "classpath:test-application-override.properties")
@ContextConfiguration
class HMRCResourceIntSpec extends Specification {

    public static final int WIREMOCK_PORT = 8999
    public static final String MATCH_ID = "87654321"
    public static final String ACCESS_ID = "987987987"

    @Rule
    WireMockClassRule wireMockRule = new WireMockClassRule(options().port(WIREMOCK_PORT))

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private ObjectMapper objectMapper

    def jsonSlurper = new JsonSlurper()

    def 'Happy path - HMRC data returned'() {

        given:
        stubFor(post(urlEqualTo("/audit"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))

        stubFor(get(urlEqualTo("/access"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(buildOauthResponse())

                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        stubFor(post(urlEqualTo("/individuals/matching/"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(buildMatchResponse())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))

        stubFor(get(urlEqualTo("/individuals/matching/"+MATCH_ID))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildMatchedIndividualResponse())))

        stubFor(get(urlEqualTo("/individuals/income/?matchId="+MATCH_ID))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildIncomeResponse())))

        stubFor(get(urlEqualTo("/individuals/employments/?matchId="+MATCH_ID))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildEmploymentsResponse())))

        stubFor(get(urlEqualTo("/individuals/employments/paye?matchId="+MATCH_ID+"&fromDate=2017-01-01&toDate=2017-06-01"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildEmploymentsPayeResponse())))

        stubFor(get(urlEqualTo("/individuals/income/paye?matchId="+MATCH_ID+"&fromDate=2017-01-01&toDate=2017-06-01"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildPayeIncomeResponse())))

        stubFor(get(urlEqualTo("/individuals/income/sa?matchId="+MATCH_ID+"&fromTaxYear=2016-17&toTaxYear=2017-18"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildSaResponse())))

        stubFor(get(urlEqualTo("/individuals/income/sa/self-employments?matchId="+MATCH_ID+"&fromTaxYear=2016-17&toTaxYear=2017-18"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildSaSelfEmploymentResponse())))

        when:
        def response = restTemplate.getForEntity("/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01", String.class)
        then:
        response.statusCode == HttpStatus.OK
        def hmrcSummary = jsonSlurper.parseText(response.body)
        hmrcSummary.paye.size == 7
        hmrcSummary.employments[0].employer.name == 'Acme Inc'
        hmrcSummary.paye[0].weekPayNumber == 49
        hmrcSummary.individual.firstName == 'Laurie'
        hmrcSummary.individual.lastName == 'Halford'
        hmrcSummary.individual.nino == 'GH576240A'
        hmrcSummary.individual.dateOfBirth == '1992-03-01'
        hmrcSummary.selfAssessment[0].selfEmploymentProfit == 0
        hmrcSummary.selfAssessment[0].taxYear == "2014-15"
        hmrcSummary.selfAssessment[1].selfEmploymentProfit == 10500
        hmrcSummary.selfAssessment[1].taxYear == "2013-14"
    }

    def 'Allow optional toDate'() {

        given:
        stubFor(post(urlEqualTo("/audit"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))

        stubFor(get(urlEqualTo("/access"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(buildOauthResponse())

                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        stubFor(post(urlEqualTo("/individuals/matching/"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(buildMatchResponse())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))

        stubFor(get(urlEqualTo("/individuals/matching/"+MATCH_ID))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildMatchedIndividualResponse())))

        stubFor(get(urlEqualTo("/individuals/income/?matchId="+MATCH_ID))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildIncomeResponse())))

        stubFor(get(urlEqualTo("/individuals/employments/?matchId="+MATCH_ID))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildEmploymentsResponse())))

        stubFor(get(urlEqualTo("/individuals/employments/paye?matchId="+MATCH_ID+"&fromDate=2017-01-01"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildEmploymentsPayeResponse())))

        stubFor(get(urlEqualTo("/individuals/income/paye?matchId="+MATCH_ID+"&fromDate=2017-01-01"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildPayeIncomeResponse())))

        stubFor(get(urlEqualTo("/individuals/income/sa?matchId="+MATCH_ID+"&fromTaxYear=2016-17"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildEmptySaResponse())))

        stubFor(get(urlEqualTo("/individuals/income/sa/self-employments?matchId="+MATCH_ID+"&fromTaxYear=2016-17"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildSaSelfEmploymentResponse())))

        when:
        sleep(2000)
        def response = restTemplate.getForEntity("/income?firstName=Bob&nino=AA123456A&lastName=Brown&fromDate=2017-01-01&dateOfBirth=2000-03-01", String.class)
        then:
        response.statusCode == HttpStatus.OK
        def hmrcSummary = jsonSlurper.parseText(response.body)
        hmrcSummary.employments[0].employer.name == 'Acme Inc'
        hmrcSummary.paye[0].weekPayNumber == 49
    }


    def 'Any HMRC error should be perculated through'() {

        given:
        stubFor(post(urlEqualTo("/audit"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        stubFor(get(urlEqualTo("/access"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(buildOauthResponse())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        stubFor(post(urlEqualTo("/individuals/matching/"))
                .willReturn(aResponse().withStatus(HttpStatus.I_AM_A_TEAPOT.value())
                .withBody(asJson(buildErrorBody("TEAPOT", "Missing parameter")))
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        sleep(2000)

        when:
        def response = restTemplate.getForEntity("/income?firstName=Bob&nino=AA123456A&lastName=Brown&fromDate=2017-01-01&toDate=2017-03-01&dateOfBirth=2000-03-01", String.class)
        then:
        response.statusCode == HttpStatus.I_AM_A_TEAPOT
    }

    def 'HMRC bad request should be handled'() {

        given:
        stubFor(post(urlEqualTo("/audit"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        stubFor(get(urlEqualTo("/access"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(buildOauthResponse())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        stubFor(post(urlEqualTo("/individuals/matching/"))
                .willReturn(aResponse().withStatus(HttpStatus.BAD_REQUEST.value())
                .withBody(asJson(buildErrorBody("INVALID_REQUEST", "Missing parameter")))
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        sleep(2000)

        when:
        def response = restTemplate.getForEntity("/income?firstName=Bob&nino=AA123456A&lastName=Brown&fromDate=2017-01-01&toDate=2017-03-01&dateOfBirth=2000-03-01", String.class)
        then:
        response.statusCode == HttpStatus.BAD_REQUEST
    }

    def 'HMRC returns error during link traversal'() {

        given:
        stubFor(post(urlEqualTo("/audit"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))

        stubFor(get(urlEqualTo("/access"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(buildOauthResponse())

                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        stubFor(post(urlEqualTo("/individuals/matching/"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(buildMatchResponse())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))

        stubFor(get(urlEqualTo("/individuals/matching/"+MATCH_ID))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildMatchedIndividualResponse())))

        stubFor(get(urlEqualTo("/individuals/income/?matchId="+MATCH_ID))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildIncomeResponse())))

        stubFor(get(urlEqualTo("/individuals/employments/?matchId="+MATCH_ID))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildEmploymentsResponse())))

        stubFor(get(urlEqualTo("/individuals/employments/paye?matchId="+MATCH_ID+"&fromDate=2017-01-01"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildEmploymentsPayeResponse())))

        stubFor(get(urlEqualTo("/individuals/income/paye?matchId="+MATCH_ID+"&fromDate=2017-01-01"))
                .willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildInvalidIncomeResponse())))


        when:
        def response = restTemplate.getForEntity("/income?firstName=Bob&nino=AA123456A&lastName=Brown&fromDate=2017-01-01&dateOfBirth=2000-03-01", String.class)
        then:
        response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
    }

    def 'Access code service throws error'() {

        given:
        stubFor(get(urlEqualTo("/access"))
                .willReturn(aResponse().withStatus(HttpStatus.BAD_REQUEST.value())
                .withBody(asJson(buildErrorBody("INVALID_REQUEST", "Missing parameter")))
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        sleep(2000)

        when:
        def response = restTemplate.getForEntity("/income?firstName=Bob&nino=AA123456A&lastName=Brown&fromDate=2017-01-01&toDate=2017-03-01&dateOfBirth=2000-03-01", String.class)
        then:
        response.statusCode == HttpStatus.BAD_REQUEST
    }

    def 'Access code service not available'() {

        given:
        wireMockRule.stop()

        when:
        def response = restTemplate.getForEntity("/income?firstName=Bob&nino=AA123456A&lastName=Brown&fromDate=2017-01-01&toDate=2017-03-01&dateOfBirth=2000-03-01", String.class)
        then:
        response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
    }


    String buildErrorBody(String code, String message) {
        IOUtils.toString(this.getClass().getResourceAsStream("/template/errorBody.json"))
                .replace("\${code}", code)
                .replace("\${message}", message)
    }

    String asJson(Object input){
        def string = objectMapper.writeValueAsString(input)
        string
    }

    String buildMatchResponse() {
        IOUtils.toString(this.getClass().getResourceAsStream("/template/matchResponse.json"))
                .replace("\${matchId}", MATCH_ID)
    }

    String buildMatchedIndividualResponse() {
        IOUtils.toString(this.getClass().getResourceAsStream("/template/individualMatchResponse.json"))
                .replace("\${matchId}", MATCH_ID)
    }

    String buildOauthResponse() {
        return asJson(new AuthToken(ACCESS_ID, null))
    }


    String buildInvalidIncomeResponse() {
        IOUtils.toString(this.getClass().getResourceAsStream("/template/invalidIncomeResponse.json"))
                .replace("\${port}", WIREMOCK_PORT.toString())
                .replace("\${matchId}", MATCH_ID)
    }

    String buildIncomeResponse() {
        IOUtils.toString(this.getClass().getResourceAsStream("/template/incomeResponse.json"))
                .replace("\${port}", WIREMOCK_PORT.toString())
                .replace("\${matchId}", MATCH_ID)
    }

    String buildPayeIncomeResponse() {
        IOUtils.toString(this.getClass().getResourceAsStream("/template/incomePayeResponse.json"))
                .replace("\${port}", WIREMOCK_PORT.toString())
                .replace("\${matchId}", MATCH_ID)
    }


    String buildEmploymentsResponse() {
        IOUtils.toString(this.getClass().getResourceAsStream("/template/employmentsResponse.json"))
                .replace("\${port}", WIREMOCK_PORT.toString())
                .replace("\${matchId}", MATCH_ID)
    }

    String buildEmploymentsPayeResponse() {
        IOUtils.toString(this.getClass().getResourceAsStream("/template/employmentsPayeResponse.json"))
                .replace("\${port}", WIREMOCK_PORT.toString())
                .replace("\${matchId}", MATCH_ID)
    }

    String buildSaResponse() {
        IOUtils.toString(this.getClass().getResourceAsStream("/template/incomeSAResponse.json"))
                .replace("\${port}", WIREMOCK_PORT.toString())
                .replace("\${matchId}", MATCH_ID)
    }

    String buildEmptySaResponse() {
        IOUtils.toString(this.getClass().getResourceAsStream("/template/incomeSAResponseEmpty.json"))
                .replace("\${port}", WIREMOCK_PORT.toString())
                .replace("\${matchId}", MATCH_ID)
    }

    String buildSaSelfEmploymentResponse() {
        IOUtils.toString(this.getClass().getResourceAsStream("/template/incomeSASelfEmploymentsResponse.json"))
                .replace("\${port}", WIREMOCK_PORT.toString())
                .replace("\${matchId}", MATCH_ID)
    }

}



