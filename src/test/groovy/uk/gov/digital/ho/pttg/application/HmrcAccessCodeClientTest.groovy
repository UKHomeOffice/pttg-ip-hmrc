package uk.gov.digital.ho.pttg.application

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import uk.gov.digital.ho.pttg.dto.AuthToken

import java.time.LocalDateTime

class HmrcAccessCodeClientTest extends Specification {

    HmrcAccessCodeClient client;
    public RestTemplate restTemplate= Mock(RestTemplate.class)

    def setup() {
        client = new HmrcAccessCodeClient(restTemplate,"");
    }

    def "GetAccessCode should call access code when null"() {
        when:
            def result = client.getAccessCode()
        then:
            1 * restTemplate.exchange(_, HttpMethod.GET, _, _) >> new ResponseEntity<AuthToken>(new AuthToken("1234",
                        LocalDateTime.of(2017, 11, 11, 11, 10)), HttpStatus.OK)

        result == "1234"
    }

    def "GetAccessCode should call access code when expired "() {
        given:
            def timeNow = LocalDateTime.now();
        when:
            def fisrtCall = client.getAccessCode()
            def secondCall = client.getAccessCode()
        then:
            2 * restTemplate.exchange(_, HttpMethod.GET, _, _) >>>
                    [new ResponseEntity<AuthToken>(new AuthToken("1234",
                timeNow.minusSeconds(HmrcAccessCodeClient.EXPIRY_MARGIN + 1)) , HttpStatus.OK),
                     new ResponseEntity<AuthToken>(new AuthToken("4321",
                             timeNow.plusHours(1)) , HttpStatus.OK)]

        fisrtCall == "1234"
        secondCall == "4321"
    }

    def "GetAccessCode should not call access code within margin period "() {
        given:
        def timeNow = LocalDateTime.now();
        when:
        def fisrtCall = client.getAccessCode()
        def secondCall = client.getAccessCode()
        then:
        1 * restTemplate.exchange(_, HttpMethod.GET, _, _) >>
                new ResponseEntity<AuthToken>(new AuthToken("1234",
                        timeNow.plusHours(5)) , HttpStatus.OK)

        fisrtCall == "1234"
        secondCall == "1234"
    }

}
