package uk.gov.digital.ho.pttg

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.junit.WireMockRule
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
    WireMockRule wireMockRule = new WireMockRule(options().port(WIREMOCK_PORT))

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private ObjectMapper objectMapper

    def jsonSlurper = new JsonSlurper()


    def 'Happy path - HMRC data returned'() {
        given:
        stubFor(get(urlEqualTo("/access"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(buildOauthResponse())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        stubFor(get(urlEqualTo("/individuals/"))
              .willReturn(aResponse().withStatus(HttpStatus.OK.value())
              .withBody(buildEntryPointResponse())
              .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        stubFor(post(urlEqualTo("/individuals/match"))
                .willReturn(aResponse().withStatus(HttpStatus.SEE_OTHER.value())
                .withHeader("Location", String.format("/individuals/",WIREMOCK_PORT) + MATCH_ID)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        stubFor(get(urlEqualTo("/individuals/"+MATCH_ID))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildMatchedIndividualResponse())))
        stubFor(get(urlPathMatching("/individuals/"+MATCH_ID+"/employments/paye"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildEmploymentsResponse())))
        stubFor(get(urlPathMatching("/individuals/"+MATCH_ID+"/income/paye"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildIncomeResponse())))

        when:
        def response = restTemplate.getForEntity("/income?firstName=Bob&nino=AA123456A&lastName=Brown&fromDate=2017-01-01&toDate=2017-03-01&dateOfBirth=2000-03-01", String.class)
        then:
        response.statusCode == HttpStatus.OK
        def hmrcSummary = jsonSlurper.parseText(response.body)
        hmrcSummary.employments[0].employer.name == 'Acme Inc'
        hmrcSummary.income[0].weekPayNumber == 49
    }

    def 'Allow optional toDate'() {
        given:
        stubFor(get(urlEqualTo("/access"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(buildOauthResponse())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        stubFor(get(urlEqualTo("/individuals/"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(buildEntryPointResponse())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        stubFor(post(urlEqualTo("/individuals/match"))
                .willReturn(aResponse().withStatus(HttpStatus.SEE_OTHER.value())
                .withHeader("Location", String.format("/individuals/",WIREMOCK_PORT) + MATCH_ID)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        stubFor(get(urlEqualTo("/individuals/"+MATCH_ID))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildMatchedIndividualResponse())))
        stubFor(get(urlPathMatching("/individuals/"+MATCH_ID+"/employments/paye"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildEmploymentsResponse())))
        stubFor(get(urlPathMatching("/individuals/"+MATCH_ID+"/income/paye"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildIncomeResponse())))

        when:
        sleep(2000)
        def response = restTemplate.getForEntity("/income?firstName=Bob&nino=AA123456A&lastName=Brown&fromDate=2017-01-01&dateOfBirth=2000-03-01", String.class)
        then:
        response.statusCode == HttpStatus.OK
        def hmrcSummary = jsonSlurper.parseText(response.body)
        hmrcSummary.employments[0].employer.name == 'Acme Inc'
        hmrcSummary.income[0].weekPayNumber == 49
    }

    def 'Any HMRC error should be perculated through'() {
        given:
        stubFor(get(urlEqualTo("/access"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(buildOauthResponse())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        stubFor(get(urlEqualTo("/individuals/"))
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
        stubFor(get(urlEqualTo("/access"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withBody(buildOauthResponse())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        stubFor(get(urlEqualTo("/individuals/"))
                .willReturn(aResponse().withStatus(HttpStatus.BAD_REQUEST.value())
                .withBody(asJson(buildErrorBody("INVALID_REQUEST", "Missing parameter")))
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        sleep(2000)

        when:
        def response = restTemplate.getForEntity("/income?firstName=Bob&nino=AA123456A&lastName=Brown&fromDate=2017-01-01&toDate=2017-03-01&dateOfBirth=2000-03-01", String.class)
        then:
        response.statusCode == HttpStatus.BAD_REQUEST
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

    String buildEntryPointResponse() {
        IOUtils.toString(this.getClass().getResourceAsStream("/template/entrypoint.json"))
                .replace("\${port}", WIREMOCK_PORT.toString())
    }

    String buildOauthResponse() {
        return asJson(new AuthToken(ACCESS_ID));
    }

    String buildMatchedIndividualResponse() {
        IOUtils.toString(this.getClass().getResourceAsStream("/template/matchResponse.json"))
                .replace("\${port}", WIREMOCK_PORT.toString())
                .replace("\${matchId}", MATCH_ID)
    }

    String buildIncomeResponse() {
        IOUtils.toString(this.getClass().getResourceAsStream("/template/incomeResponse.json"))
                .replace("\${port}", WIREMOCK_PORT.toString())
                .replace("\${matchId}", MATCH_ID)
    }

    String buildEmploymentsResponse() {
        IOUtils.toString(this.getClass().getResourceAsStream("/template/employmentsResponse.json"))
                .replace("\${port}", WIREMOCK_PORT.toString())
                .replace("\${matchId}", MATCH_ID)
    }

}



