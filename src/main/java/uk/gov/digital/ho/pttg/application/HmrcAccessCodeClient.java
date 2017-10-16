package uk.gov.digital.ho.pttg.application;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.dto.AuthToken;

import java.time.LocalDateTime;

import static uk.gov.digital.ho.pttg.api.RequestData.CORRELATION_ID_HEADER;
import static uk.gov.digital.ho.pttg.api.RequestData.USER_ID_HEADER;

@Component
public class HmrcAccessCodeClient {
    private AuthToken currentToken;
    private RestTemplate restTemplate;
    private final String accessCodeurl;
    final static int EXPIRY_MARGIN = 60;


    public HmrcAccessCodeClient(RestTemplate restTemplate, @Value("${base.hmrc.access.code.url}") String accessCodeurl) {
        this.restTemplate = restTemplate;
        this.accessCodeurl = accessCodeurl;
    }

    public String getAccessCode() {

        if(isExpiring()) {
            currentToken = getAuthToken();
        }
        return currentToken.getCode();
    }

    synchronized private AuthToken getAuthToken() {
        return restTemplate.exchange(accessCodeurl + "/access", HttpMethod.GET, createHeadersEntityWithMDC(), AuthToken.class).getBody();
    }

   private boolean isExpiring() {
        return this.currentToken == null || LocalDateTime.now().plusSeconds(EXPIRY_MARGIN).isAfter(this.currentToken.getExpiry());
    }

    private HttpEntity createHeadersEntityWithMDC() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(CORRELATION_ID_HEADER, MDC.get(CORRELATION_ID_HEADER));
        headers.add(USER_ID_HEADER, MDC.get(USER_ID_HEADER));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>("headers", headers);
    }
}
