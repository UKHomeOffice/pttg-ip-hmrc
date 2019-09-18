package uk.gov.digital.ho.pttg.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.api.ComponentTraceHeaderData;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcNotFoundException;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcUnauthorisedException;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.ProxyForbiddenException;
import uk.gov.digital.ho.pttg.application.util.TraversonFollower;

import java.net.URI;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcOverRateLimitException;

@RunWith(MockitoJUnitRunner.class)
public class HmrcCallWrapperTest {

    private static final HttpMethod ANY_HTTP_METHOD = POST;
    private static final HttpEntity<String> ANY_ENTITY = new HttpEntity<>("some-body");
    private static final ParameterizedTypeReference<Resource<String>> ANY_REFERENCE = new ParameterizedTypeReference<Resource<String>>() {};
    private static final URI ANY_URI = URI.create("any-uri");
    private static final String ANY_LINK = "any-link";
    private static final String ANY_ACCESS_TOKEN = "any token";

    @InjectMocks
    private HmrcCallWrapper hmrcCallWrapper;

    @Mock
    private RestTemplate mockRestTemplate;
    @Mock
    private TraversonFollower mockTraversonFollower;
    @Mock
    private ComponentTraceHeaderData mockComponentTraceHeaderData;

    @Test
    public void shouldThrowCustomExceptionForHttpForbidden() {
        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(FORBIDDEN));

        assertThatExceptionOfType(ProxyForbiddenException.class)
                .isThrownBy(() -> hmrcCallWrapper.exchange(ANY_URI, ANY_HTTP_METHOD, ANY_ENTITY, ANY_REFERENCE));
    }

    @Test
    public void shouldThrowCustomExceptionForHttpUnauthorised() {
        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(UNAUTHORIZED));

        assertThatExceptionOfType(HmrcUnauthorisedException.class)
                .isThrownBy(() -> hmrcCallWrapper.exchange(ANY_URI, ANY_HTTP_METHOD, ANY_ENTITY, ANY_REFERENCE));
    }

    @Test
    public void shouldThrowCustomExceptionForHmrcMatchingFailed() {
        byte[] responseBody = "blah blahMATCHING_FAILEDblah blah".getBytes(UTF_8);

        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(FORBIDDEN, "", responseBody, UTF_8));

        assertThatExceptionOfType(HmrcNotFoundException.class)
                .isThrownBy(() -> hmrcCallWrapper.exchange(ANY_URI, ANY_HTTP_METHOD, ANY_ENTITY, ANY_REFERENCE));
    }

    @Test
    public void shouldThrowCustomExceptionForHmrcOverRateLimit() {
        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        assertThatExceptionOfType(HmrcOverRateLimitException.class)
                .isThrownBy(() -> hmrcCallWrapper.exchange(ANY_URI, ANY_HTTP_METHOD, ANY_ENTITY, ANY_REFERENCE));
    }

    @Test
    public void shouldRethrowOtherExceptions() {
        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new NullPointerException());

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> hmrcCallWrapper.exchange(ANY_URI, ANY_HTTP_METHOD, ANY_ENTITY, ANY_REFERENCE));
    }

    @Test
    public void shouldThrowCustomExceptionForHttpForbiddenWithTraverson() {
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(FORBIDDEN));

        assertThatExceptionOfType(ProxyForbiddenException.class)
                .isThrownBy(() -> hmrcCallWrapper.followTraverson("some-link", "some-access-token", ANY_REFERENCE));
    }

    @Test
    public void shouldThrowCustomExceptionForHttpUnauthorisedWithTraverson() {
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(UNAUTHORIZED));

        assertThatExceptionOfType(HmrcUnauthorisedException.class)
                .isThrownBy(() -> hmrcCallWrapper.followTraverson("some-link", "some-access-token", ANY_REFERENCE));
    }

    @Test
    public void shouldThrowCustomExceptionForHmrcNotFoundWithTraverson() {
        byte[] responseBody = "blah blahMATCHING_FAILEDblah blah".getBytes(UTF_8);
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(FORBIDDEN, "", responseBody, UTF_8));

        assertThatExceptionOfType(HmrcNotFoundException.class)
                .isThrownBy(() -> hmrcCallWrapper.followTraverson("some-link", "some-access-token", ANY_REFERENCE));
    }

    @Test
    public void shouldThrowCustomExceptionForHmrcOverRateLimitWithTraverson() {
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(TOO_MANY_REQUESTS));

        assertThatExceptionOfType(HmrcOverRateLimitException.class)
                .isThrownBy(() -> hmrcCallWrapper.followTraverson("some-link", "some-access-token", ANY_REFERENCE));
    }

    @Test
    public void shouldRethrowOtherExceptionsWithTraverson() {
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .willThrow(new NullPointerException());

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> hmrcCallWrapper.followTraverson("some-link", "some-access-token", ANY_REFERENCE));
    }

    @Test
    public void exchange_successResponse_addHmrcToComponentTrace() {
        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(new ResponseEntity<>(OK));

        hmrcCallWrapper.exchange(ANY_URI, ANY_HTTP_METHOD, ANY_ENTITY, ANY_REFERENCE);

        then(mockComponentTraceHeaderData).should().appendComponentToTrace("HMRC");
    }

    @Test
    public void exchange_serverError_doNotAddHmrcToComponentTrace() {
        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));

        try {
            hmrcCallWrapper.exchange(ANY_URI, ANY_HTTP_METHOD, ANY_ENTITY, ANY_REFERENCE);
        } catch (HttpServerErrorException ignored) {
            // Exception not of interest to this test.
        }

        then(mockComponentTraceHeaderData).should(never()).appendComponentToTrace(any());
    }

    @Test
    public void exchange_clientError_addHmrcToComponentTrace() {
        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(NOT_FOUND));

        try {
            hmrcCallWrapper.exchange(ANY_URI, ANY_HTTP_METHOD, ANY_ENTITY, ANY_REFERENCE);
        } catch (HttpClientErrorException e) {
            // Exception not of interest to this test.
        }

        then(mockComponentTraceHeaderData).should().appendComponentToTrace("HMRC");
    }

    @Test
    public void followTraverson_successResponse_addHmrcToComponentTrace() {
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .willReturn(new Resource<>(""));

        hmrcCallWrapper.followTraverson(ANY_LINK, ANY_ACCESS_TOKEN, ANY_REFERENCE);

        then(mockComponentTraceHeaderData).should().appendComponentToTrace("HMRC");
    }

    @Test
    public void followTraverson_serverError_doNotAddHmrcToComponentTrace() {
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));

        try {
            hmrcCallWrapper.followTraverson(ANY_LINK, ANY_ACCESS_TOKEN, ANY_REFERENCE);
        } catch (HttpServerErrorException ignored) {
            // Exception not of interest to this test.
        }

        then(mockComponentTraceHeaderData).should(never()).appendComponentToTrace(any());
    }

    @Test
    public void followTraverson_clientError_addHmrcToComponentTrace() {
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(NOT_FOUND));

        try {
            hmrcCallWrapper.followTraverson(ANY_LINK, ANY_ACCESS_TOKEN, ANY_REFERENCE);
        } catch (HttpClientErrorException ignored) {
            // Exception not of interest to this test.
        }

        then(mockComponentTraceHeaderData).should().appendComponentToTrace("HMRC");
    }
}
