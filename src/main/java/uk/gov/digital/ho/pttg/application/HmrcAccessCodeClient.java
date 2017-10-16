package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.api.RequestData;
import uk.gov.digital.ho.pttg.dto.AuthToken;

import java.time.LocalDateTime;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.digital.ho.pttg.api.RequestData.*;

@Component
@Slf4j
public class HmrcAccessCodeClient {

    private AuthToken currentToken;
    private RestTemplate restTemplate;
    private final String accessCodeurl;
    private final int expiryMargin;
    private final RequestData requestData;

    public HmrcAccessCodeClient(RestTemplate restTemplate,
                                @Value("${base.hmrc.access.code.url}") String accessCodeurl,
                                @Value("${access.code.expiry.margin.in.seconds}") int expiryMargin,
                                RequestData requestData) {
        this.restTemplate = restTemplate;
        this.accessCodeurl = accessCodeurl;
        this.expiryMargin = expiryMargin;
        this.requestData = requestData;
    }

    public String getAccessCode() {

        if (isExpiring()) {
            log.info("Need to get new HMRC access code");
            getAuthToken();
        }

        return currentToken.getCode();
    }

    private boolean isExpiring() {
        return this.currentToken == null ||
                this.currentToken.getExpiry() == null ||
                LocalDateTime.now().plusSeconds(expiryMargin).isAfter(this.currentToken.getExpiry());
    }

    private synchronized void getAuthToken() {
        currentToken = restTemplate.exchange(accessCodeurl + "/access",
                                                HttpMethod.GET,
                                                generateRestHeaders(),
                                                AuthToken.class).getBody();
    }

    private HttpEntity generateRestHeaders() {

        HttpHeaders headers = new HttpHeaders();

        headers.add(AUTHORIZATION, requestData.hmrcBasicAuth());
        headers.add(SESSION_ID_HEADER, requestData.sessionId());
        headers.add(CORRELATION_ID_HEADER, requestData.correlationId());
        headers.add(USER_ID_HEADER, requestData.userId());
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>("headers", headers);
    }
}
