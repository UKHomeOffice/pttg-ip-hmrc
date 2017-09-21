package uk.gov.digital.ho.pttg

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.junit.Rule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static uk.gov.digital.ho.pttg.api.CorrelationHeaderFilter.CORRELATION_ID_HEADER
import static uk.gov.digital.ho.pttg.api.UserHeaderFilter.USER_ID_HEADER

@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(locations = "classpath:test-application-override.properties")
@ContextConfiguration
class HMRCResourceHeadersIntSpec extends Specification {

    public static final int WIREMOCK_PORT = 8999
    public static final String CORRELATION_ID = "12426223-353434"
    public static final String USER_ID = "Terry"

    @Rule
    WireMockRule wireMockRule = new WireMockRule(options().port(WIREMOCK_PORT))

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private ObjectMapper objectMapper

    def 'Incoming correlation id and user id headers are added to MDC and transferred to further rest calls'() {
        given:
        stubFor(get(urlEqualTo("/access"))
                .willReturn(aResponse().withStatus(HttpStatus.OK.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        when:
        sleep(2000)
        restTemplate.exchange("/income?firstName=Bob&nino=AA123456A&lastName=Brown&fromDate=2017-01-01&dateOfBirth=2000-03-01", HttpMethod.GET, createEntityForHeaders("headers"), String.class)
        then:
        verify(getRequestedFor(urlEqualTo("/access"))
                .withHeader(CORRELATION_ID_HEADER, equalTo(CORRELATION_ID)))
        verify(getRequestedFor(urlEqualTo("/access"))
                .withHeader(USER_ID_HEADER, equalTo(USER_ID)))
    }

    static HttpEntity createEntityForHeaders(Object entity) {
        HttpHeaders headers = new HttpHeaders()
        headers.add(CORRELATION_ID_HEADER, CORRELATION_ID)
        headers.add(USER_ID_HEADER, USER_ID)
        headers.setContentType(MediaType.APPLICATION_JSON)
        return new HttpEntity<>(entity, headers)
    }

}
