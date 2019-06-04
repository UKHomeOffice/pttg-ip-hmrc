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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
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
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcOverRateLimitException;

@RunWith(MockitoJUnitRunner.class)
public class HmrcCallWrapperTest {

    @InjectMocks
    private HmrcCallWrapper hmrcCallWrapper;

    @Mock
    private RestTemplate mockRestTemplate;
    @Mock
    private TraversonFollower mockTraversonFollower;

    @Test
    public void shouldThrowCustomExceptionForHttpForbidden() {
        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(FORBIDDEN));

        assertThatExceptionOfType(ProxyForbiddenException.class)
                .isThrownBy(() -> hmrcCallWrapper.exchange(new URI("some-uri"), POST, new HttpEntity("some-body"), new ParameterizedTypeReference<Resource<String>>() {
                }));
    }

    @Test
    public void shouldThrowCustomExceptionForHttpUnauthorised() {
        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(UNAUTHORIZED));

        assertThatExceptionOfType(HmrcUnauthorisedException.class)
                .isThrownBy(() -> hmrcCallWrapper.exchange(new URI("some-uri"), POST, new HttpEntity("some-body"), new ParameterizedTypeReference<Resource<String>>() {
                }));
    }

    @Test
    public void shouldThrowCustomExceptionForHmrcMatchingFailed() {
        byte[] responseBody = "blah blahMATCHING_FAILEDblah blah".getBytes(UTF_8);

        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(FORBIDDEN, "", responseBody, UTF_8));

        assertThatExceptionOfType(HmrcNotFoundException.class)
                .isThrownBy(() -> hmrcCallWrapper.exchange(new URI("some-uri"), POST, new HttpEntity("some-body"), new ParameterizedTypeReference<Resource<String>>() {
                }));
    }

    @Test
    public void shouldThrowCustomExceptionForHmrcOverRateLimit() {
        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        assertThatExceptionOfType(HmrcOverRateLimitException.class)
                .isThrownBy(() -> hmrcCallWrapper.exchange(new URI("some-uri"), POST, new HttpEntity("some-body"), new ParameterizedTypeReference<Resource<String>>() {
                }));
    }

    @Test
    public void shouldRethrowOtherExceptions() {
        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(new NullPointerException());

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> hmrcCallWrapper.exchange(new URI("some-uri"), POST, new HttpEntity("some-body"), new ParameterizedTypeReference<Resource<String>>() {
                }));
    }

    @Test
    public void shouldThrowCustomExceptionForHttpForbiddenWithTraverson() {
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(FORBIDDEN));

        assertThatExceptionOfType(ProxyForbiddenException.class)
                .isThrownBy(() -> hmrcCallWrapper.followTraverson("some-link", "some-access-token", new ParameterizedTypeReference<Resource<String>>() {
                }));
    }

    @Test
    public void shouldThrowCustomExceptionForHttpUnauthorisedWithTraverson() {
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(UNAUTHORIZED));

        assertThatExceptionOfType(HmrcUnauthorisedException.class)
                .isThrownBy(() -> hmrcCallWrapper.followTraverson("some-link", "some-access-token", new ParameterizedTypeReference<Resource<String>>() {
                }));
    }

    @Test
    public void shouldThrowCustomExceptionForHmrcNotFoundWithTraverson() {
        byte[] responseBody = "blah blahMATCHING_FAILEDblah blah".getBytes(UTF_8);
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(FORBIDDEN, "", responseBody, UTF_8));

        assertThatExceptionOfType(HmrcNotFoundException.class)
                .isThrownBy(() -> hmrcCallWrapper.followTraverson("some-link", "some-access-token", new ParameterizedTypeReference<Resource<String>>() {
                }));
    }

    @Test
    public void shouldThrowCustomExceptionForHmrcOverRateLimitWithTraverson() {
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .willThrow(new HttpClientErrorException(TOO_MANY_REQUESTS));

        assertThatExceptionOfType(HmrcOverRateLimitException.class)
                .isThrownBy(() -> hmrcCallWrapper.followTraverson("some-link", "some-access-token", new ParameterizedTypeReference<Resource<String>>() {
                }));
    }

    @Test
    public void shouldRethrowOtherExceptionsWithTraverson() {
        given(mockTraversonFollower.followTraverson(anyString(), anyString(), any(RestTemplate.class), any(ParameterizedTypeReference.class)))
                .willThrow(new NullPointerException());

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> hmrcCallWrapper.followTraverson("some-link", "some-access-token", new ParameterizedTypeReference<Resource<String>>() {
                }));
    }

}
