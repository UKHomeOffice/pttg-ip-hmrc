package uk.gov.digital.ho.pttg.application

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import uk.gov.digital.ho.pttg.dto.AuthToken

import java.time.LocalDateTime

class HmrcAccessCodeClientTest extends Specification {

    public static final int EXPIRY_MARGIN_FOR_TEST = 99

    HmrcAccessCodeClient client
    public RestTemplate mockRestTemplate = Mock(RestTemplate.class)

    def setup() {
        client = new HmrcAccessCodeClient(mockRestTemplate, "some.hmrc.url", EXPIRY_MARGIN_FOR_TEST)
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

}
