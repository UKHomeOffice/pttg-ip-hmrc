package uk.gov.digital.ho.pttg.application.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;

import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.digital.ho.pttg.api.RequestHeaderData.*;

@Slf4j
@Component
@AllArgsConstructor
public class TraversonFollower {

    private final RequestHeaderData requestHeaderData;

    public <T> Resource<T> followTraverson(String link, String accessToken, RestTemplate restTemplate, ParameterizedTypeReference<Resource<T>> resourceTypeRef) {

        log.debug("following traverson for {}", link);

        HttpHeaders headers = generateHeaders(accessToken);

        return traversonFor(link, restTemplate)
                       .follow()
                       .withHeaders(headers)
                       .toObject(resourceTypeRef);
    }

    private Traverson traversonFor(String link, RestTemplate restTemplate) {
        try {
            return new Traverson(new URI(link), APPLICATION_JSON).setRestOperations(restTemplate);
        } catch (URISyntaxException e) {
            throw new ApplicationExceptions.HmrcException("Problem building hmrc API url", e);
        }
    }

    private HttpHeaders generateHeaders(String accessToken) {

        HttpHeaders headers = new HttpHeaders();

        headers.add(AUTHORIZATION, format("Bearer %s", accessToken));
        headers.add(ACCEPT, requestHeaderData.hmrcApiVersion());
        headers.add(SESSION_ID_HEADER, requestHeaderData.sessionId());
        headers.add(CORRELATION_ID_HEADER, requestHeaderData.correlationId());
        headers.add(USER_ID_HEADER, requestHeaderData.userId());
        headers.setContentType(APPLICATION_JSON);

        return headers;
    }
}
