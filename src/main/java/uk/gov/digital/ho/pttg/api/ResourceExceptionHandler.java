package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.AuditDataException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcException;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcNotFoundException;

@ControllerAdvice
@Slf4j
public class ResourceExceptionHandler {


    @ExceptionHandler(AuditDataException.class)
    public ResponseEntity auditDataMarshalFailureHandler(AuditDataException e) {
        log.error("AuditDataException: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = HmrcException.class)
    public ResponseEntity handleHmrcException(HmrcException e) {
        log.error("HmrcException: {}", e);
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    public ResponseEntity handleHttpClientErrorException(HttpClientErrorException e) {
        log.error("HttpClientErrorException: {}", e.getMessage());
        log.error("Error response body: {}", e.getResponseBodyAsString());
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler(value = HttpServerErrorException.class)
    public ResponseEntity handleHttpServerErrorException(HttpServerErrorException e) {
        log.error("HttpServerErrorException: {}", e.getMessage());
        log.error("Error response body: {}", e.getResponseBodyAsString());
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity handleException(Exception e) {
        log.error("Error: {}", e.getMessage());
        log.error("Exception: ", e);
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = HmrcNotFoundException.class)
    public ResponseEntity handleHmrcNotFoundException(HmrcNotFoundException e) {
        log.info("HmrcNotFoundException: {}", e);
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }

}


