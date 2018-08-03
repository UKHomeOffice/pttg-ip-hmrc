package uk.gov.digital.ho.pttg.application.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;

import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.digital.ho.pttg.api.RequestData.CORRELATION_ID_HEADER;
import static uk.gov.digital.ho.pttg.api.RequestData.USER_ID_HEADER;

@Slf4j
@Component
public class TraversonUtils {

    public <T> Resource<T> followTraverson(String link, String accessToken, RestTemplate restTemplate, ParameterizedTypeReference<Resource<T>> resourceTypeRef) {
        log.info("following traverson for {}", link);
        return traversonFor(link, restTemplate).follow().withHeaders(generateHeaders(accessToken)).toObject(resourceTypeRef);
    }

    public Traverson traversonFor(String link, RestTemplate restTemplate) {
        try {
            return new Traverson(new URI(link), APPLICATION_JSON).setRestOperations(restTemplate);
        } catch (URISyntaxException e) {
            throw new ApplicationExceptions.HmrcException("Problem building hmrc API url", e);
        }
    }

    private HttpHeaders generateHeaders(final String apiVersion, final String accessToken) {
        final HttpHeaders headers = generateHeaders(apiVersion);
        headers.add("Authorization", format("Bearer %s", accessToken));
        return headers;
    }

    private HttpHeaders generateHeaders(final String apiVersion) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.add(ACCEPT, apiVersion);
        headers.add(CORRELATION_ID_HEADER, MDC.get(CORRELATION_ID_HEADER));
        headers.add(USER_ID_HEADER, MDC.get(USER_ID_HEADER));
        return headers;
    }
}
