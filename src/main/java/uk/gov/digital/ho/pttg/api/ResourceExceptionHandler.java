package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.*;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.digital.ho.pttg.api.RequestHeaderData.REQUEST_DURATION_MS;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@ControllerAdvice
@Slf4j
class ResourceExceptionHandler {

    private RequestHeaderData requestHeaderData;

    ResourceExceptionHandler(RequestHeaderData requestHeaderData) {
        this.requestHeaderData = requestHeaderData;
    }

    @ExceptionHandler
    ResponseEntity handle(HmrcException e) {
        log.error("HmrcException: {}", e.getMessage(),
                value(EVENT, HMRC_SERVICE_RESPONSE_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    ResponseEntity handle(HttpClientErrorException e) {
        log.error("HttpClientErrorException: {} {}", e.getStatusCode(), e.getMessage(),
                value(EVENT, HMRC_SERVICE_RESPONSE_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler
    ResponseEntity handle(HmrcUnauthorisedException e) {
        log.error("HmrcUnauthorisedException: {}", e.getMessage(),
                value(EVENT, HMRC_AUTHENTICATION_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    ResponseEntity handle(HttpServerErrorException e) {
        log.error("HttpServerErrorException: {}", e.getMessage(),
                value(EVENT, HMRC_SERVICE_RESPONSE_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler
    ResponseEntity handle(RestClientException e) {
        log.error("RestClientException:", e,
                value(EVENT, HMRC_SERVICE_RESPONSE_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    ResponseEntity handle(Exception e) {
        log.error("Fault Detected:", e,
                value(EVENT, HMRC_SERVICE_RESPONSE_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    ResponseEntity handle(HmrcNotFoundException e) {
        log.info("HmrcNotFoundException: {}", e.getMessage(),
                value(EVENT, HMRC_SERVICE_RESPONSE_NOT_FOUND),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler
    ResponseEntity handle(ProxyForbiddenException e) {
        log.error("Received 403 Forbidden from a request to HMRC. This was from the proxy and not HMRC.",
                value(EVENT, HMRC_PROXY_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    ResponseEntity handle(InvalidIdentityException e) {
        log.error("Service called with invalid identity: {}", e.getMessage(),
                value(EVENT, HMRC_SERVICE_RESPONSE_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(e.getMessage(), UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler
    ResponseEntity handle(InvalidNationalInsuranceNumberException e) {
        log.error("Service called with invalid NINO: {}", e.getMessage(),
                value(EVENT, HMRC_SERVICE_RESPONSE_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(e.getMessage(), UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler
    ResponseEntity handle(HttpMessageConversionException e) {
        log.error("Failed to handle request due to: {}", e.getMessage(),
                value(EVENT, HMRC_SERVICE_RESPONSE_ERROR),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
         return new ResponseEntity<>(e.getMessage(), UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler
    ResponseEntity handle(HmrcOverRateLimitException e) {
        log.error("HMRC Rate Limit Exceeded: {}", e.getMessage(),
                value(EVENT, HMRC_OVER_RATE_LIMIT),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()));
        return new ResponseEntity<>(e.getMessage(), TOO_MANY_REQUESTS);
    }
}
