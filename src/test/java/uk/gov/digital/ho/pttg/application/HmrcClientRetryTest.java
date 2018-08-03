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
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.application.util.*;
import uk.gov.digital.ho.pttg.dto.*;

import java.net.URI;
import java.time.LocalDate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.*;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
        "hmrc.api.version=application/vnd.hmrc.P1.0+json",
        "hmrc.endpoint=http://test.com",
        "hmrc.retry.attempts=4",
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
        public TraversonFollower createTraversonUtils() {
            return new TraversonFollower();
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
    public void shouldThrowProxyForbiddenExceptionOn_403_Not_MATCHING_FAILED() {

        String someAccessToken = "some access token";
        LocalDate dob = LocalDate.of(1990,1,1);
        Individual someIndividual = new Individual("first", "last", "nino", dob);
        LocalDate from = LocalDate.of(2018, 4, 4);
        LocalDate to = LocalDate.of(2018, 6, 30);

        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .willThrow(new HttpClientErrorException(FORBIDDEN));

        assertThatThrownBy(() -> hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to, new IncomeSummaryContext()))
                .isInstanceOf(ProxyForbiddenException.class);

        verify(mockRestTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void shouldThrowHmrcNotFoundExceptionOn_403_MATCHING_FAILED() {

        String someAccessToken = "some access token";
        LocalDate dob = LocalDate.of(1990,1,1);
        Individual someIndividual = new Individual("first", "last", "nino", dob);
        LocalDate from = LocalDate.of(2018, 4, 4);
        LocalDate to = LocalDate.of(2018, 6, 30);

        byte[] responseBody = "blah blahMATCHING_FAILEDblah blah".getBytes(UTF_8);

        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .willThrow(new HttpClientErrorException(FORBIDDEN, "", responseBody, UTF_8));

        assertThatThrownBy(() -> hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to, new IncomeSummaryContext()))
                .isInstanceOf(HmrcNotFoundException.class);

        verify(mockRestTemplate, times(2)).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void shouldThrowHmrcUnauthorisedExceptionOn_HttpClientErrorException401() {

        String someAccessToken = "some access token";
        LocalDate dob = LocalDate.of(1990,1,1);
        Individual someIndividual = new Individual("first", "last", "nino", dob);
        LocalDate from = LocalDate.of(2018, 4, 4);
        LocalDate to = LocalDate.of(2018, 6, 30);

        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
            .willThrow(new HttpClientErrorException(UNAUTHORIZED));

        assertThatThrownBy(() -> hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to, new IncomeSummaryContext()))
                .isInstanceOf(HmrcUnauthorisedException.class);

        verify(mockRestTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

}
