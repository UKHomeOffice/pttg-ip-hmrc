package uk.gov.digital.ho.pttg.application;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.application.util.TraversonFollower;

import java.net.URI;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class HmrcCallWrapper {

    private RestTemplate restTemplate;
    private TraversonFollower traversonFollower;

    public HmrcCallWrapper(RestTemplate restTemplate, TraversonFollower traversonFollower) {
        this.restTemplate = restTemplate;
        this.traversonFollower = traversonFollower;
    }

    <T> ResponseEntity<Resource<T>> exchange(URI uri, HttpMethod httpMethod, HttpEntity httpEntity, ParameterizedTypeReference<Resource<T>> reference) {
        try {
            return restTemplate.exchange(uri, httpMethod, httpEntity, reference);

        } catch (HttpClientErrorException ex) {
            handleClientErrorExceptions(ex);
        }
        //noop
        return null;
    }

    <T> Resource<T> followTraverson(String link, String accessToken, String apiVerion, ParameterizedTypeReference<Resource<T>> reference) {
        try {
            return traversonFollower.followTraverson(link, accessToken, apiVerion, restTemplate, reference);

        } catch (HttpClientErrorException ex) {
            handleClientErrorExceptions(ex);
        }
        // noop
        return null;
    }

    private void handleClientErrorExceptions(HttpClientErrorException ex) {
        HttpStatus statusCode = ex.getStatusCode();

        if (isHmrcMatchFailedError(ex)) {
            throw new ApplicationExceptions.HmrcNotFoundException("Received MATCHING_FAILED from HMRC");

        } else if (statusCode.equals(FORBIDDEN)) {
            throw new ApplicationExceptions.ProxyForbiddenException("Received a 403 Forbidden response from proxy");

        } else if (statusCode.equals(UNAUTHORIZED)) {
            throw new ApplicationExceptions.HmrcUnauthorisedException(ex.getMessage(), ex);

        } else {
            throw ex;
        }
    }

    private boolean isHmrcMatchFailedError(HttpClientErrorException exception) {

        HttpStatus statusCode = exception.getStatusCode();

        if (!statusCode.equals(FORBIDDEN)) {
            return false;
        }

        return exception.getResponseBodyAsString().contains("MATCHING_FAILED");
    }


}