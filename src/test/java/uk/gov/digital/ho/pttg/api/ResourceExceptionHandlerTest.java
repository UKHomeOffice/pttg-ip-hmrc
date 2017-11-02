package uk.gov.digital.ho.pttg.api;


import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

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

        ResponseEntity responseEntity = handler.handle(mockAuditDataException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldProduceInternalServerErrorForHmrcException() {
        HmrcException mockHmrcException = mock(HmrcException.class);
        when(mockHmrcException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(mockHmrcException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldProduceInternalServerErrorForHttpClientErrorException() {
        HttpClientErrorException mockHttpClientErrorException = mock(HttpClientErrorException.class);
        when(mockHttpClientErrorException.getMessage()).thenReturn("any message");
        when(mockHttpClientErrorException.getStatusCode()).thenReturn(I_AM_A_TEAPOT);

        ResponseEntity responseEntity = handler.handle(mockHttpClientErrorException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(I_AM_A_TEAPOT);
    }

    @Test
    public void shouldProduceInternalServerErrorForHttpServerErrorException() {
        HttpServerErrorException mockHttpServerErrorException = mock(HttpServerErrorException.class);
        when(mockHttpServerErrorException.getMessage()).thenReturn("any message");
        when(mockHttpServerErrorException.getStatusCode()).thenReturn(I_AM_A_TEAPOT);

        ResponseEntity responseEntity = handler.handle(mockHttpServerErrorException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(I_AM_A_TEAPOT);
    }

    @Test
    public void shouldProduceInternalServerErrorForException() {
        Exception mockException = mock(Exception.class);
        when(mockException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(mockException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldProduceInternalServerErrorForHmrcNotFoundException() {
        HmrcNotFoundException mockHmrcNotFoundException = mock(HmrcNotFoundException.class);
        when(mockHmrcNotFoundException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(mockHmrcNotFoundException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void shouldProduceInternalServerErrorForRestClientException() {
        RestClientException mockRestClientException = mock(RestClientException.class);
        when(mockRestClientException.getMessage()).thenReturn("any message");

        ResponseEntity responseEntity = handler.handle(mockRestClientException);

        assertThat(responseEntity.getBody()).isEqualTo("any message");
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }
}