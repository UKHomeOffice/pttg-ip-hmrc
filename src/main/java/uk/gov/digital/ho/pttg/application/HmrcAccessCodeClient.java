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

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.digital.ho.pttg.api.RequestData.*;

@Component
@Slf4j
public class HmrcAccessCodeClient {

    private RestTemplate restTemplate;
    private final String accessCodeUrl;
    private final RequestData requestData;

    public HmrcAccessCodeClient(RestTemplate restTemplate,
                                @Value("${base.hmrc.access.code.url}") String accessCodeUrl,
                                RequestData requestData) {
        this.restTemplate = restTemplate;
        this.accessCodeUrl = accessCodeUrl;
        this.requestData = requestData;
    }

    public String getAccessCode() {

        AuthToken currentToken = restTemplate.exchange(accessCodeUrl + "/access",
                HttpMethod.GET,
                generateRestHeaders(),
                AuthToken.class).getBody();

        return currentToken.getCode();
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
