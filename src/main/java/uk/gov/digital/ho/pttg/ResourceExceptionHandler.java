package uk.gov.digital.ho.pttg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
@Slf4j
public class ResourceExceptionHandler {

    @ExceptionHandler(value = HmrcException.class)
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ResponseEntity handleHmrcException(HmrcException e) {
        log.error("HmrcException: {}", e);
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    @ResponseBody
    public ResponseEntity handleHttpClientErrorException(HttpClientErrorException e) {
        log.error("HttpClientErrorException: {}", e.getMessage());
        log.error("Error response body: {}", e.getResponseBodyAsString());
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler(value = HttpServerErrorException.class)
    @ResponseBody
    public ResponseEntity handleHttpServerErrorException(HttpServerErrorException e) {
        log.error("HttpServerErrorException: {}", e.getMessage());
        log.error("Error response body: {}", e.getResponseBodyAsString());
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseEntity handleException(Exception e) {
        log.error("Error: {}", e.getMessage());
        log.error("Exception: ", e);
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = HmrcNotFoundException.class)
    @ResponseStatus(value = NOT_FOUND)
    @ResponseBody
    public ResponseEntity handleApplicationUrnNotFoundException(HmrcNotFoundException e) {
        log.info("HmrcNotFoundException: {}", e);
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

}


