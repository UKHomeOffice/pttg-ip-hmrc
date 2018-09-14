package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.*;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@ControllerAdvice
@Slf4j
public class ResourceExceptionHandler {

    @ExceptionHandler
    public ResponseEntity handle(HmrcException e) {
        log.error("HmrcException: {}", e.getMessage(), value(EVENT, HMRC_SERVICE_RESPONSE_ERROR));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity handle(HttpClientErrorException e) {
        log.error("HttpClientErrorException: {} {}", e.getStatusCode(), e.getMessage(), value(EVENT, HMRC_SERVICE_RESPONSE_ERROR));
        log.error("Error response body: {}", e.getResponseBodyAsString());
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler
    public ResponseEntity handle(HmrcUnauthorisedException e) {
        log.error("HmrcUnauthorisedException: {}", e.getMessage(), value(EVENT, HMRC_AUTHENTICATION_ERROR));
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    public ResponseEntity handle(HttpServerErrorException e) {
        log.error("HttpServerErrorException: {}", e.getMessage(), value(EVENT, HMRC_SERVICE_RESPONSE_ERROR));
        log.error("Error response body: {}", e.getResponseBodyAsString());
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler
    public ResponseEntity handle(RestClientException e) {
        log.error("Error: {}", e.getMessage());
        log.error("RestClientException:", e, value(EVENT, HMRC_SERVICE_RESPONSE_ERROR));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler
    public ResponseEntity handle(Exception e) {
        log.error("Fault Detected:", e, value(EVENT, HMRC_SERVICE_RESPONSE_ERROR));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity handle(HmrcNotFoundException e) {
        log.info("HmrcNotFoundException: {}", e.getMessage(), value(EVENT, HMRC_SERVICE_RESPONSE_NOT_FOUND));
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity handle(ProxyForbiddenException e) {
        log.error("Received 403 Forbidden from a request to HMRC. This was from the proxy and not HMRC.", value(EVENT, HMRC_PROXY_ERROR));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity handle(InvalidIdentityException e) {
        log.error("Service called with invalid identity: {}", e.getMessage(), value(EVENT, HMRC_SERVICE_RESPONSE_ERROR));
        return new ResponseEntity<>(e.getMessage(), UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler
    public ResponseEntity handle(InvalidNationalInsuranceNumberException e) {
        log.error("Service called with invalid NINO: {}", e.getMessage(), value(EVENT, HMRC_SERVICE_RESPONSE_ERROR));
        return new ResponseEntity<>(e.getMessage(), UNPROCESSABLE_ENTITY);
    }
}
