package uk.gov.digital.ho.pttg.application

import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import uk.gov.digital.ho.pttg.api.RequestData
import uk.gov.digital.ho.pttg.dto.AuthToken

import java.time.LocalDateTime

import static org.springframework.http.HttpHeaders.AUTHORIZATION
import static uk.gov.digital.ho.pttg.api.RequestData.*

@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
        value="SE_NO_SERIALVERSIONID",
        justification="Ignore error for serialization of closure")
class HmrcAccessCodeClientTest extends Specification {

    public static final int EXPIRY_MARGIN_FOR_TEST = 99

    HmrcAccessCodeClient client
    RestTemplate mockRestTemplate = Mock(RestTemplate.class)
    RequestData mockRequestData = Mock(RequestData.class)

    def setup() {
        client = new HmrcAccessCodeClient(mockRestTemplate, "some.hmrc.url", EXPIRY_MARGIN_FOR_TEST, mockRequestData)
    }


    def "should call access code service when access code is null"() {

        when:

        def result = client.getAccessCode()

        then:

        1 * mockRestTemplate.exchange("some.hmrc.url/access", HttpMethod.GET, _, AuthToken.class) >>
            new ResponseEntity<AuthToken>(new AuthToken("1234", LocalDateTime.now().plusHours(1)),
                                            HttpStatus.OK)

        result == "1234"
    }

    def "should call access code service when expiry of access code is null"() {

        when:

        client.getAccessCode()
        def result = client.getAccessCode()

        then:

        2 * mockRestTemplate.exchange("some.hmrc.url/access", HttpMethod.GET, _, AuthToken.class) >>
            new ResponseEntity<AuthToken>(new AuthToken("1234", null),
                                            HttpStatus.OK)

        result == "1234"
    }

    def "should call access code service when access code has expired"() {

        given:

            def timeNow = LocalDateTime.now()

        when:

            def firstCall = client.getAccessCode()
            def secondCall = client.getAccessCode()

        then:

            2 * mockRestTemplate.exchange("some.hmrc.url/access", HttpMethod.GET, _, AuthToken.class) >>>
                    [new ResponseEntity<AuthToken>(new AuthToken("1234", timeNow.minusSeconds(EXPIRY_MARGIN_FOR_TEST + 1)),
                                                    HttpStatus.OK),
                     new ResponseEntity<AuthToken>(new AuthToken("4321", timeNow.plusHours(1)),
                                                    HttpStatus.OK)]

            firstCall == "1234"
            secondCall == "4321"
    }

    def "should not call access code service when access code expiry is within the margin"() {

        given:

        def timeNow = LocalDateTime.now()

        when:

            def firstCall = client.getAccessCode()
            def secondCall = client.getAccessCode()

        then:

            1 * mockRestTemplate.exchange("some.hmrc.url/access", HttpMethod.GET, _, AuthToken.class) >>
                    new ResponseEntity<AuthToken>(new AuthToken("1234", timeNow.plusHours(1)),
                                                    HttpStatus.OK)

            firstCall == "1234"
            secondCall == "1234"
    }

    def "should set headers for call to access code service"() {

        given:

            1 * mockRequestData.sessionId() >> "some session id"
            1 * mockRequestData.correlationId() >> "some correlation id"
            1 * mockRequestData.userId() >> "some user id"
            1 * mockRequestData.hmrcBasicAuth() >> "some basic auth"

        when:

        def result = client.getAccessCode()

        then:

        1 * mockRestTemplate.exchange("some.hmrc.url/access",
                                        HttpMethod.GET,
                                        {HttpEntity httpEntity ->


                                            httpEntity.headers.containsKey(AUTHORIZATION)
                                            httpEntity.headers.get(AUTHORIZATION).get(0) == "some basic auth"
                                            httpEntity.headers.containsKey(SESSION_ID_HEADER)
                                            httpEntity.headers.get(SESSION_ID_HEADER).get(0) == "some session"
                                            httpEntity.headers.containsKey(CORRELATION_ID_HEADER)
                                            httpEntity.headers.get(CORRELATION_ID_HEADER).get(0) == "some correlation id"
                                            httpEntity.headers.containsKey(USER_ID_HEADER)
                                            httpEntity.headers.get(USER_ID_HEADER).get(0) == "some user id"
                                        },
                                        AuthToken.class) >>
                new ResponseEntity<AuthToken>(new AuthToken("1234", LocalDateTime.now().plusHours(1)),
                        HttpStatus.OK)

        result == "1234"
    }

}
