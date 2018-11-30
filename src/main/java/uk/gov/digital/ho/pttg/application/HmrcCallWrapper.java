package uk.gov.digital.ho.pttg.application;

import org.springframework.beans.factory.annotation.Qualifier;
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

import static org.springframework.http.HttpStatus.*;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.*;

@Service
public class HmrcCallWrapper {

    private RestTemplate restTemplate;
    private TraversonFollower traversonFollower;

    public HmrcCallWrapper(@Qualifier("hmrcApiRestTemplate") RestTemplate restTemplate, TraversonFollower traversonFollower) {
        this.restTemplate = restTemplate;
        this.traversonFollower = traversonFollower;
    }

    public <T> ResponseEntity<Resource<T>> exchange(URI uri, HttpMethod httpMethod, HttpEntity httpEntity, ParameterizedTypeReference<Resource<T>> reference) {
        try {
            return restTemplate.exchange(uri, httpMethod, httpEntity, reference);
        } catch (HttpClientErrorException ex) {
            throw handleClientErrorExceptions(ex);
        }
    }

    <T> Resource<T> followTraverson(String link, String accessToken, ParameterizedTypeReference<Resource<T>> reference) {
        try {
            return traversonFollower.followTraverson(link, accessToken, restTemplate, reference);
        } catch (HttpClientErrorException ex) {
            throw handleClientErrorExceptions(ex);
        }
    }

    private RuntimeException handleClientErrorExceptions(HttpClientErrorException ex) {
        HttpStatus statusCode = ex.getStatusCode();

        if (isHmrcMatchFailedError(ex)) {
            return new HmrcNotFoundException("Received MATCHING_FAILED from HMRC");

        } else if (statusCode.equals(FORBIDDEN)) {
            return new ProxyForbiddenException("Received a 403 Forbidden response from proxy");

        } else if (statusCode.equals(UNAUTHORIZED)) {
            return new HmrcUnauthorisedException(ex.getMessage(), ex);
        } else if (statusCode.equals(TOO_MANY_REQUESTS)) {
            return new HmrcOverRateLimitException("Too many requests to HMRC");
        } else {
            return ex;
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
