package uk.gov.digital.ho.pttg.api;


import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.*;

public class ResourceExceptionHandlerTest {

    private ResourceExceptionHandler handler = new ResourceExceptionHandler();

    @Test
    public void shouldProduceInternalServerErrorForAuditDataException() {
        AuditDataException mockAuditDataException = mock(AuditDataException.class);
        when(mockAuditDataException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.auditDataMarshalFailureHandler(mockAuditDataException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldProduceInternalServerErrorForHmrcException() {
        HmrcException mockHmrcException = mock(HmrcException.class);
        when(mockHmrcException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handleHmrcException(mockHmrcException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldProduceInternalServerErrorForHttpClientErrorException() {
        HttpClientErrorException mockHttpClientErrorException = mock(HttpClientErrorException.class);
        when(mockHttpClientErrorException.getMessage()).thenReturn("any message");
        when(mockHttpClientErrorException.getStatusCode()).thenReturn(I_AM_A_TEAPOT);

        ResponseEntity responseEntity = handler.handleHttpClientErrorException(mockHttpClientErrorException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(I_AM_A_TEAPOT);
    }

    @Test
    public void shouldProduceInternalServerErrorForHttpServerErrorException() {
        HttpServerErrorException mockHttpServerErrorException = mock(HttpServerErrorException.class);
        when(mockHttpServerErrorException.getMessage()).thenReturn("any message");
        when(mockHttpServerErrorException.getStatusCode()).thenReturn(I_AM_A_TEAPOT);

        ResponseEntity responseEntity = handler.handleHttpServerErrorException(mockHttpServerErrorException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(I_AM_A_TEAPOT);
    }

    @Test
    public void shouldProduceInternalServerErrorForException() {
        Exception mockException = mock(Exception.class);
        when(mockException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handleException(mockException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldProduceInternalServerErrorForHmrcNotFoundException() {
        HmrcNotFoundException mockHmrcNotFoundException = mock(HmrcNotFoundException.class);
        when(mockHmrcNotFoundException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handleHmrcNotFoundException(mockHmrcNotFoundException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
    }
}