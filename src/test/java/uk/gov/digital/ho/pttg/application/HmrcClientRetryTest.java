package uk.gov.digital.ho.pttg.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.application.util.CompositeNameNormalizer;
import uk.gov.digital.ho.pttg.application.util.DiacriticNameNormalizer;
import uk.gov.digital.ho.pttg.application.util.MaxLengthNameNormalizer;
import uk.gov.digital.ho.pttg.application.util.NameNormalizer;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.net.URI;
import java.time.LocalDate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.*;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "hmrc.api.version=someApiVersion",
        "hmrc.endpoint=someEndpoint",
        "hmrc.retry.attempts=3",
        "hmrc.retry.delay=10"
})
public class HmrcClientRetryTest {

    @MockBean
    private RestTemplate mockRestTemplate;

    @TestConfiguration
    @EnableRetry
    @Import(HmrcClient.class)
    public static class HmrcClientRetryTestConfig {
        @Bean
        public NinoUtils createNinoUtils() {
            return new NinoUtils();
        }
        @Bean
        public NameNormalizer createNameNormalizer() {
            NameNormalizer[] nameNormalizers = {
                    new MaxLengthNameNormalizer(35),
                    new DiacriticNameNormalizer()
            };
            return new CompositeNameNormalizer(nameNormalizers);
        }
    }

    @Autowired
    private HmrcClient hmrcClient;

    @Test
    public void shouldNotRetryHttpClientErrorException() {

        String someAccessToken = "some access token";
        LocalDate dob = LocalDate.of(1990,1,1);
        Individual someIndividual = new Individual("first", "last", "nino", dob);
        LocalDate from = LocalDate.of(2018, 4, 4);
        LocalDate to = LocalDate.of(2018, 6, 30);

        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .willThrow((new HttpClientErrorException(BAD_GATEWAY)));

        assertThatThrownBy(() ->hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to))
                .isInstanceOf(HttpClientErrorException.class);

        verify(mockRestTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void shouldNotRetryHttpClientErrorException_403_Not_MATCHING_FAILED() {

        String someAccessToken = "some access token";
        LocalDate dob = LocalDate.of(1990,1,1);
        Individual someIndividual = new Individual("first", "last", "nino", dob);
        LocalDate from = LocalDate.of(2018, 4, 4);
        LocalDate to = LocalDate.of(2018, 6, 30);

        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .willThrow(new HttpClientErrorException(FORBIDDEN));

        assertThatThrownBy(() ->hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to))
                .isInstanceOf(ProxyForbiddenException.class);

        verify(mockRestTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void shouldNotRetryHttpClientErrorException_403_MATCHING_FAILED() {

        String someAccessToken = "some access token";
        LocalDate dob = LocalDate.of(1990,1,1);
        Individual someIndividual = new Individual("first", "last", "nino", dob);
        LocalDate from = LocalDate.of(2018, 4, 4);
        LocalDate to = LocalDate.of(2018, 6, 30);

        byte[] responseBody = "blah blahMATCHING_FAILEDblah blah".getBytes(UTF_8);

        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .willThrow(new HttpClientErrorException(FORBIDDEN, "", responseBody, UTF_8));

        assertThatThrownBy(() ->hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to))
                .isInstanceOf(HmrcNotFoundException.class);

        verify(mockRestTemplate, times(2)).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void shouldNotRetryHttpClientErrorException_401() {

        String someAccessToken = "some access token";
        LocalDate dob = LocalDate.of(1990,1,1);
        Individual someIndividual = new Individual("first", "last", "nino", dob);
        LocalDate from = LocalDate.of(2018, 4, 4);
        LocalDate to = LocalDate.of(2018, 6, 30);

        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .willThrow(new HttpClientErrorException(UNAUTHORIZED));

        assertThatThrownBy(() ->hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to))
                .isInstanceOf(HmrcUnauthorisedException.class);

        verify(mockRestTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void shouldNotRetryHmrcUnauthorisedException() {

        String someAccessToken = "some access token";
        LocalDate dob = LocalDate.of(1990,1,1);
        Individual someIndividual = new Individual("first", "last", "nino", dob);
        LocalDate from = LocalDate.of(2018, 4, 4);
        LocalDate to = LocalDate.of(2018, 6, 30);

        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .willThrow(HmrcUnauthorisedException.class);

        assertThatThrownBy(() ->hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to))
                .isInstanceOf(HmrcUnauthorisedException.class);

        verify(mockRestTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void shouldNotRetryHmrcNotFoundException() {

        String someAccessToken = "some access token";
        LocalDate dob = LocalDate.of(1990,1,1);
        Individual someIndividual = new Individual("first", "last", "nino", dob);
        LocalDate from = LocalDate.of(2018, 4, 4);
        LocalDate to = LocalDate.of(2018, 6, 30);

        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(HmrcNotFoundException.class);

        assertThatThrownBy(() ->hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to))
                .isInstanceOf(HmrcNotFoundException.class);

        verify(mockRestTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void shouldRetryHttpServerErrorException() {

        String someAccessToken = "some access token";
        LocalDate dob = LocalDate.of(1990,1,1);
        Individual someIndividual = new Individual("first", "last", "nino", dob);
        LocalDate from = LocalDate.of(2018, 4, 4);
        LocalDate to = LocalDate.of(2018, 6, 30);

        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(HttpServerErrorException.class);

        assertThatThrownBy(() ->hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to))
                .isInstanceOf(HttpServerErrorException.class);

        verify(mockRestTemplate, times(3)).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

}
