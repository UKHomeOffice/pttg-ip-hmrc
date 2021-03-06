package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.api.ComponentTraceHeaderData;
import uk.gov.digital.ho.pttg.application.util.TraversonFollower;

import java.net.URI;
import java.util.function.Supplier;

import static org.springframework.http.HttpStatus.*;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.*;

@Service
@Slf4j
public class HmrcCallWrapper {

    private RestTemplate restTemplate;
    private TraversonFollower traversonFollower;
    private ComponentTraceHeaderData componentTraceHeaderData;

    public HmrcCallWrapper(@Qualifier("hmrcApiRestTemplate") RestTemplate restTemplate, TraversonFollower traversonFollower, ComponentTraceHeaderData componentTraceHeaderData) {
        this.restTemplate = restTemplate;
        this.traversonFollower = traversonFollower;
        this.componentTraceHeaderData = componentTraceHeaderData;
    }

    @AbortIfBeyondMaxResponseDuration
    public <T> ResponseEntity<Resource<T>> exchange(URI uri, HttpMethod httpMethod, HttpEntity httpEntity, ParameterizedTypeReference<Resource<T>> reference) {
        return handleExceptions(() -> restTemplate.exchange(uri, httpMethod, httpEntity, reference));
    }

    @AbortIfBeyondMaxResponseDuration
    <T> Resource<T> followTraverson(String link, String accessToken, ParameterizedTypeReference<Resource<T>> reference) {
        return handleExceptions(() -> traversonFollower.followTraverson(link, accessToken, restTemplate, reference));
    }

    private <T> T handleExceptions(Supplier<T> hmrcRequest) {
        try {
            T result = hmrcRequest.get();
            addHmrcToComponentTrace();
            return result;
        } catch (HttpServerErrorException e) {
            log.info("HttpServerErrorException: {} - {}", e.getStatusCode(), e.getStatusText());
            throw e;
        } catch (HttpClientErrorException e) {
            log.info("HttpClientErrorException: {} - {}", e.getStatusCode(), e.getStatusText());
            addHmrcToComponentTrace();
            throw handleClientErrorExceptions(e);
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
        }

        return ex;
    }

    private boolean isHmrcMatchFailedError(HttpClientErrorException exception) {

        HttpStatus statusCode = exception.getStatusCode();

        if (!statusCode.equals(FORBIDDEN)) {
            return false;
        }

        return exception.getResponseBodyAsString().contains("MATCHING_FAILED");
    }

    private void addHmrcToComponentTrace() {
        componentTraceHeaderData.appendComponentToTrace("HMRC");
    }
}
