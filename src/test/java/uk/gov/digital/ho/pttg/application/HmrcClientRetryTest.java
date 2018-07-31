package uk.gov.digital.ho.pttg.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import uk.gov.digital.ho.pttg.dto.*;

import java.net.URI;
import java.time.LocalDate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.*;
import static uk.gov.digital.ho.pttg.application.HmrcClient.*;

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

        assertThatThrownBy(() -> hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to, new IncomeSummaryContext()))
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

        assertThatThrownBy(() -> hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to, new IncomeSummaryContext()))
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

        assertThatThrownBy(() -> hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to, new IncomeSummaryContext()))
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

        assertThatThrownBy(() -> hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to, new IncomeSummaryContext()))
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

        assertThatThrownBy(() -> hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to, new IncomeSummaryContext()))
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

        assertThatThrownBy(() -> hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to, new IncomeSummaryContext()))
                .isInstanceOf(HmrcNotFoundException.class);

        verify(mockRestTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void shouldRetryHttpServerErrorExceptionMaxTimes() {

        String someAccessToken = "some access token";
        LocalDate dob = LocalDate.of(1990,1,1);
        Individual someIndividual = new Individual("first", "last", "nino", dob);
        LocalDate from = LocalDate.of(2018, 4, 4);
        LocalDate to = LocalDate.of(2018, 6, 30);

        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(HttpServerErrorException.class);

        assertThatThrownBy(() -> hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to, new IncomeSummaryContext()))
                .isInstanceOf(HttpServerErrorException.class);

        verify(mockRestTemplate, times(4)).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void shouldTraverseAllHypermediaLinksWhenMaximumRetries() {

        String someAccessToken = "some access token";
        LocalDate dob = LocalDate.of(1990,1,1);
        Individual someIndividual = new Individual("first", "last", "nino", dob);
        LocalDate from = LocalDate.of(2018, 4, 4);
        LocalDate to = LocalDate.of(2018, 6, 30);

        given(mockRestTemplate.exchange(eq(URI.create("http://test.com/individuals/matching/")), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(HttpServerErrorException.class)
                .willReturn(new ResponseEntity(new Resource<>(INDIVIDUAL, new Link("/individuals/matching", INDIVIDUAL)), HttpStatus.OK));

        given(mockRestTemplate.exchange(eq(URI.create("http://test.com/individuals/matching")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(
                        new ResponseEntity(
                                new Resource<>(new EmbeddedIndividual(someIndividual), new Link("/individuals/income", INCOME), new Link("/individuals/employments", EMPLOYMENTS)), HttpStatus.OK));

        given(mockRestTemplate.exchange(eq(URI.create("http://test.com/individuals/income")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(new ResponseEntity(new Resource<>(EMPLOYMENTS, new Link("/individuals/income/paye", PAYE_INCOME), new Link("/individuals/income/sa", SELF_ASSESSMENT)), HttpStatus.OK));

        given(mockRestTemplate.exchange(eq(URI.create("http://test.com/individuals/employments")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(new ResponseEntity(new Resource<>(EMPLOYMENTS, new Link("/individuals/employments/paye", PAYE_EMPLOYMENT)), HttpStatus.OK));

        given(mockRestTemplate.exchange(eq(URI.create("http://test.com/individuals/income/sa?fromTaxYear=2017-18&toTaxYear=2018-19")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(HttpServerErrorException.class)
                .willReturn(new ResponseEntity(new Resource<>(SELF_ASSESSMENT, new Link("/individuals/income/sa/employments", SELF_EMPLOYMENTS)), HttpStatus.OK));

        PayeIncome payeIncome = new PayeIncome(new Incomes(emptyList()));

        given(mockRestTemplate.exchange(eq(URI.create("http://test.com/individuals/income/paye?fromDate=2018-04-04&toDate=2018-06-30")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(new ResponseEntity(new Resource<>(payeIncome), HttpStatus.OK));

        Employments employments = new Employments(emptyList());

        given(mockRestTemplate.exchange(eq(URI.create("http://test.com/individuals/employments/paye?fromDate=2018-04-04&toDate=2018-06-30")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(new ResponseEntity(new Resource<>(employments), HttpStatus.OK));

        SelfEmployments selfEmployments = new SelfEmployments(new TaxReturns(emptyList()));

        given(mockRestTemplate.exchange(eq(URI.create("http://test.com/individuals/income/sa/employments")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(HttpServerErrorException.class)
                .willReturn(new ResponseEntity(new Resource<>(selfEmployments), HttpStatus.OK));

        IncomeSummaryContext context = new IncomeSummaryContext();

        IncomeSummary incomeSummary = hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to, context);

        assertThat(incomeSummary.getPaye()).isEmpty();
        assertThat(incomeSummary.getSelfAssessment()).isEmpty();
        assertThat(incomeSummary.getEmployments()).isEmpty();
        assertThat(incomeSummary.getIndividual().getFirstName()).isEqualTo("first");
        assertThat(incomeSummary.getIndividual().getLastName()).isEqualTo("last");
        assertThat(incomeSummary.getIndividual().getNino()).isEqualTo("nino");
        assertThat(incomeSummary.getIndividual().getDateOfBirth()).isEqualTo(dob);

        verify(mockRestTemplate, times(2)).exchange(eq(URI.create("http://test.com/individuals/matching/")), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(mockRestTemplate, times(1)).exchange(eq(URI.create("http://test.com/individuals/matching")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(mockRestTemplate, times(1)).exchange(eq(URI.create("http://test.com/individuals/income")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(mockRestTemplate, times(1)).exchange(eq(URI.create("http://test.com/individuals/employments")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(mockRestTemplate, times(2)).exchange(eq(URI.create("http://test.com/individuals/income/sa?fromTaxYear=2017-18&toTaxYear=2018-19")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(mockRestTemplate, times(1)).exchange(eq(URI.create("http://test.com/individuals/income/paye?fromDate=2018-04-04&toDate=2018-06-30")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(mockRestTemplate, times(1)).exchange(eq(URI.create("http://test.com/individuals/employments/paye?fromDate=2018-04-04&toDate=2018-06-30")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(mockRestTemplate, times(2)).exchange(eq(URI.create("http://test.com/individuals/income/sa/employments")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

    @Test
    public void shouldTraverseAllHypermediaLinks() {

        String someAccessToken = "some access token";
        LocalDate dob = LocalDate.of(1990,1,1);
        Individual someIndividual = new Individual("first", "last", "nino", dob);
        LocalDate from = LocalDate.of(2018, 4, 4);
        LocalDate to = LocalDate.of(2018, 6, 30);

        given(mockRestTemplate.exchange(eq(URI.create("http://test.com/individuals/matching/")), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(new ResponseEntity(new Resource<>(INDIVIDUAL, new Link("/individuals/matching", INDIVIDUAL)), HttpStatus.OK));

        given(mockRestTemplate.exchange(eq(URI.create("http://test.com/individuals/matching")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(
                        new ResponseEntity(
                                new Resource<>(new EmbeddedIndividual(someIndividual), new Link("/individuals/income", INCOME), new Link("/individuals/employments", EMPLOYMENTS)), HttpStatus.OK));

        given(mockRestTemplate.exchange(eq(URI.create("http://test.com/individuals/income")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(new ResponseEntity(new Resource<>(EMPLOYMENTS, new Link("/individuals/income/paye", PAYE_INCOME), new Link("/individuals/income/sa", SELF_ASSESSMENT)), HttpStatus.OK));

        given(mockRestTemplate.exchange(eq(URI.create("http://test.com/individuals/employments")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(new ResponseEntity(new Resource<>(EMPLOYMENTS, new Link("/individuals/employments/paye", PAYE_EMPLOYMENT)), HttpStatus.OK));

        given(mockRestTemplate.exchange(eq(URI.create("http://test.com/individuals/income/sa?fromTaxYear=2017-18&toTaxYear=2018-19")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(new ResponseEntity(new Resource<>(SELF_ASSESSMENT, new Link("/individuals/income/sa/employments", SELF_EMPLOYMENTS)), HttpStatus.OK));

        PayeIncome payeIncome = new PayeIncome(new Incomes(emptyList()));

        given(mockRestTemplate.exchange(eq(URI.create("http://test.com/individuals/income/paye?fromDate=2018-04-04&toDate=2018-06-30")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(new ResponseEntity(new Resource<>(payeIncome), HttpStatus.OK));

        Employments employments = new Employments(emptyList());

        given(mockRestTemplate.exchange(eq(URI.create("http://test.com/individuals/employments/paye?fromDate=2018-04-04&toDate=2018-06-30")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(new ResponseEntity(new Resource<>(employments), HttpStatus.OK));

        SelfEmployments selfEmployments = new SelfEmployments(new TaxReturns(emptyList()));

        given(mockRestTemplate.exchange(eq(URI.create("http://test.com/individuals/income/sa/employments")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willReturn(new ResponseEntity(new Resource<>(selfEmployments), HttpStatus.OK));


        IncomeSummaryContext context = new IncomeSummaryContext();

        IncomeSummary incomeSummary = hmrcClient.getIncomeSummary(someAccessToken, someIndividual, from, to, context);

        assertThat(incomeSummary.getPaye()).isEmpty();
        assertThat(incomeSummary.getSelfAssessment()).isEmpty();
        assertThat(incomeSummary.getEmployments()).isEmpty();
        assertThat(incomeSummary.getIndividual().getFirstName()).isEqualTo("first");
        assertThat(incomeSummary.getIndividual().getLastName()).isEqualTo("last");
        assertThat(incomeSummary.getIndividual().getNino()).isEqualTo("nino");
        assertThat(incomeSummary.getIndividual().getDateOfBirth()).isEqualTo(dob);

        verify(mockRestTemplate, times(1)).exchange(eq(URI.create("http://test.com/individuals/matching/")), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(mockRestTemplate, times(1)).exchange(eq(URI.create("http://test.com/individuals/matching")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(mockRestTemplate, times(1)).exchange(eq(URI.create("http://test.com/individuals/income")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(mockRestTemplate, times(1)).exchange(eq(URI.create("http://test.com/individuals/employments")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(mockRestTemplate, times(1)).exchange(eq(URI.create("http://test.com/individuals/income/sa?fromTaxYear=2017-18&toTaxYear=2018-19")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(mockRestTemplate, times(1)).exchange(eq(URI.create("http://test.com/individuals/income/paye?fromDate=2018-04-04&toDate=2018-06-30")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(mockRestTemplate, times(1)).exchange(eq(URI.create("http://test.com/individuals/employments/paye?fromDate=2018-04-04&toDate=2018-06-30")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(mockRestTemplate, times(1)).exchange(eq(URI.create("http://test.com/individuals/income/sa/employments")), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
    }

}
