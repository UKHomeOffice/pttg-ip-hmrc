package uk.gov.digital.ho.pttg.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.apache.commons.io.IOUtils
import org.junit.Rule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import uk.gov.digital.ho.pttg.dto.AccessCode

import java.time.LocalDate

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.http.HttpMethod.POST
import static uk.gov.digital.ho.pttg.api.RequestHeaderData.CORRELATION_ID_HEADER
import static uk.gov.digital.ho.pttg.api.RequestHeaderData.USER_ID_HEADER

@SpringBootTest(
        webEnvironment = RANDOM_PORT
)
@TestPropertySource(locations = "classpath:test-application-override.properties")
@ContextConfiguration
class HMRCResourceHeadersIntSpec extends Specification {

    public static final int WIREMOCK_PORT = 8999
    public static final String CORRELATION_ID = "12426223-353434"
    public static final String USER_ID = "Terry"
    public static final String MATCH_ID = "87654321"
    public static final String ACCESS_ID = "987987987"

    @Rule
    WireMockRule wireMockRule = new WireMockRule(options().port(WIREMOCK_PORT))

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private ObjectMapper objectMapper

    def 'Incoming correlation id and user id headers are added to MDC and transferred to further rest calls'() {

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

        stubFor(get(urlEqualTo("/individuals/matching/" + MATCH_ID))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildMatchedIndividualResponse())))

        stubFor(get(urlEqualTo("/individuals/income/?matchId=" + MATCH_ID))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildIncomeResponse())))

        stubFor(get(urlEqualTo("/individuals/employments/?matchId=" + MATCH_ID))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildEmploymentsResponse())))

        stubFor(get(urlEqualTo("/individuals/employments/paye?matchId=" + MATCH_ID + "&fromDate=2017-01-01&toDate=2017-06-01"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildEmploymentsPayeResponse())))

        stubFor(get(urlEqualTo("/individuals/income/paye?matchId=" + MATCH_ID + "&fromDate=2017-01-01&toDate=2017-06-01"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildPayeIncomeResponse())))

        stubFor(get(urlEqualTo("/individuals/income/sa?matchId="+MATCH_ID+"&fromTaxYear=2016-17&toTaxYear=2017-18"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildEmptySaResponse())))

        stubFor(get(urlEqualTo("/individuals/income/sa/self-employments?matchId="+MATCH_ID+"&fromTaxYear=2016-17"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody(buildSaSelfEmploymentResponse())))

        when:
        sleep(2000)
        IncomeDataRequest incomeDataRequest = new IncomeDataRequest("Bob", "Brown", "AA123456A",
                                                                    LocalDate.of(2000, 03, 01),
                                                                    LocalDate.of(2017, 01, 01),
                                                                    LocalDate.of(2017, 06, 01),
                                                                    "")

        restTemplate.exchange("/income", POST, createEntityForHeaders(incomeDataRequest), String.class)
        then:

        verify(postRequestedFor(urlEqualTo("/audit"))
                .withHeader(CORRELATION_ID_HEADER, equalTo(CORRELATION_ID)))
        verify(postRequestedFor(urlEqualTo("/audit"))
                .withHeader(USER_ID_HEADER, equalTo(USER_ID)))

        verify(getRequestedFor(urlEqualTo("/access"))
                .withHeader(CORRELATION_ID_HEADER, equalTo(CORRELATION_ID)))
        verify(getRequestedFor(urlEqualTo("/access"))
                .withHeader(USER_ID_HEADER, equalTo(USER_ID)))

        verify(postRequestedFor(urlEqualTo("/individuals/matching/"))
                .withHeader(CORRELATION_ID_HEADER, equalTo(CORRELATION_ID)))
        verify(postRequestedFor(urlEqualTo("/individuals/matching/"))
                .withHeader(USER_ID_HEADER, equalTo(USER_ID)))

        verify(getRequestedFor(urlEqualTo("/individuals/matching/87654321"))
                .withHeader(CORRELATION_ID_HEADER, equalTo(CORRELATION_ID)))
        verify(getRequestedFor(urlEqualTo("/individuals/matching/87654321"))
                .withHeader(USER_ID_HEADER, equalTo(USER_ID)))

        verify(getRequestedFor(urlEqualTo("/individuals/income/?matchId=87654321"))
                .withHeader(CORRELATION_ID_HEADER, equalTo(CORRELATION_ID)))
        verify(getRequestedFor(urlEqualTo("/individuals/income/?matchId=87654321"))
                .withHeader(USER_ID_HEADER, equalTo(USER_ID)))

        verify(getRequestedFor(urlEqualTo("/individuals/income/paye?matchId=87654321&fromDate=2017-01-01&toDate=2017-06-01"))
                .withHeader(CORRELATION_ID_HEADER, equalTo(CORRELATION_ID)))
        verify(getRequestedFor(urlEqualTo("/individuals/income/paye?matchId=87654321&fromDate=2017-01-01&toDate=2017-06-01"))
                .withHeader(USER_ID_HEADER, equalTo(USER_ID)))

        verify(getRequestedFor(urlEqualTo("/individuals/employments/paye?matchId=87654321&fromDate=2017-01-01&toDate=2017-06-01"))
                .withHeader(CORRELATION_ID_HEADER, equalTo(CORRELATION_ID)))
        verify(getRequestedFor(urlEqualTo("/individuals/employments/paye?matchId=87654321&fromDate=2017-01-01&toDate=2017-06-01"))
                .withHeader(USER_ID_HEADER, equalTo(USER_ID)))

        verify(getRequestedFor(urlEqualTo("/individuals/income/sa?matchId=87654321&fromTaxYear=2016-17&toTaxYear=2017-18"))
                .withHeader(CORRELATION_ID_HEADER, equalTo(CORRELATION_ID)))
        verify(getRequestedFor(urlEqualTo("/individuals/income/sa?matchId=87654321&fromTaxYear=2016-17&toTaxYear=2017-18"))
                .withHeader(USER_ID_HEADER, equalTo(USER_ID)))

        verify(getRequestedFor(urlEqualTo("/individuals/income/sa/self-employments?matchId=87654321&fromTaxYear=2016-17"))
                .withHeader(CORRELATION_ID_HEADER, equalTo(CORRELATION_ID)))
        verify(getRequestedFor(urlEqualTo("/individuals/income/sa/self-employments?matchId=87654321&fromTaxYear=2016-17"))
                .withHeader(USER_ID_HEADER, equalTo(USER_ID)))
    }

    static HttpEntity createEntityForHeaders(Object entity) {
        HttpHeaders headers = new HttpHeaders()
        headers.add(CORRELATION_ID_HEADER, CORRELATION_ID)
        headers.add(USER_ID_HEADER, USER_ID)
        headers.setContentType(MediaType.APPLICATION_JSON)
        return new HttpEntity<>(entity, headers)
    }

    String asJson(Object input) {
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
        return asJson(new AccessCode(ACCESS_ID, null, null))
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
