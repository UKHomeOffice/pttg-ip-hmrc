package uk.gov.digital.ho.pttg.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.application.util.NameNormalizer;
import uk.gov.digital.ho.pttg.dto.*;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcNotFoundException;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcUnauthorisedException;

@RunWith(MockitoJUnitRunner.class)
public class HmrcClientTest {

    @Mock
    private RestTemplate mockRestTemplate;

    @Mock
    private NinoUtils mockNinoUtils;

    @Mock
    private NameNormalizer mockNameNormalizer;

    @Test
    public void shouldProduceEmptyMap() {

        HmrcClient client = new HmrcClient(new RestTemplate(), new NinoUtils(), mockNameNormalizer, "any api version", "any url");

        Map<String, String> p = client.createEmployerPaymentRefMap(new ArrayList<>());

        assertThat(p).isEmpty();
    }

    @Test
    public void shouldProduceMapWithOneEntry() {

        RestTemplate anyRestTemplate = new RestTemplate();
        NinoUtils anyNinoUtils = new NinoUtils();
        String anyApiVersion = "any api version";
        String anyUrl = "any url";
        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        Address anyEmployerAddress = null;
        String somePayFrequency = "some pay frequency";
        String anyEmployer = "any employer";

        String somePayeReference = "some ref";

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, mockNameNormalizer, anyApiVersion, anyUrl);
        List<Employment> employments = Arrays.asList(
                new Employment(
                    somePayFrequency,
                    anyStartDate.toString(),
                    anyEndDate.toString(),
                    new Employer(
                            somePayeReference,
                            anyEmployer,
                            anyEmployerAddress)));

        Map<String, String> p = client.createEmployerPaymentRefMap(employments);

        assertThat(p).size().isEqualTo(1);
        assertThat(p).containsKey(somePayeReference);
        assertThat(p.get(somePayeReference)).isEqualTo(somePayFrequency);
    }

    @Test
    public void shouldProduceMapWithMutlipleEntries() {

        RestTemplate anyRestTemplate = new RestTemplate();
        NinoUtils anyNinoUtils = new NinoUtils();
        String anyApiVersion = "any api version";
        String anyUrl = "any url";
        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        Address anyEmployerAddress = null;
        String somePayFrequency = "some pay frequency";
        String anyEmployer = "any employer";
        String somePayeReference = "some ref";
        String anotherPayFrequency = "another pay frequency";
        String anotherPayeReference = "another pay reference";

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, mockNameNormalizer, anyApiVersion, anyUrl);
        List<Employment> employments = Arrays.asList(
                new Employment(
                    somePayFrequency,
                    anyStartDate.toString(),
                    anyEndDate.toString(),
                    new Employer(
                            somePayeReference,
                            anyEmployer,
                            anyEmployerAddress)),
                new Employment(
                    anotherPayFrequency,
                    anyStartDate.toString(),
                    anyEndDate.toString(),
                    new Employer(
                            anotherPayeReference,
                            anyEmployer,
                            anyEmployerAddress)));

        Map<String, String> p = client.createEmployerPaymentRefMap(employments);

        assertThat(p).size().isEqualTo(2);
        assertThat(p).containsKey(somePayeReference);
        assertThat(p.get(somePayeReference)).isEqualTo(somePayFrequency);
        assertThat(p).containsKey(anotherPayeReference);
        assertThat(p.get(anotherPayeReference)).isEqualTo(anotherPayFrequency);
    }

    @Test
    public void shouldDoNothingWhenNoIncome() {

        RestTemplate anyRestTemplate = new RestTemplate();
        NinoUtils anyNinoUtils = new NinoUtils();
        String anyApiVersion = "any api version";
        String anyUrl = "any url";
        String somePayeReference = "some ref";
        String somePayFrequency = "any pay frequency";
        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        String anyEmployer = "any employer";
        Address anyEmployerAddress = null;

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, mockNameNormalizer, anyApiVersion, anyUrl);
        List<Income> incomes = null;
        List<Employment> employments = Arrays.asList(
                new Employment(
                    somePayFrequency,
                    anyStartDate.toString(),
                    anyEndDate.toString(),
                    new Employer(
                            somePayeReference,
                            anyEmployer,
                            anyEmployerAddress)));

        Map<String, String> p = client.createEmployerPaymentRefMap(employments);

        client.addPaymentFrequency(incomes, p);
    }

    @Test
    public void shouldDoNothingWhenEmptyIncome() {

        RestTemplate anyRestTemplate = new RestTemplate();
        NinoUtils anyNinoUtils = new NinoUtils();
        String anyApiVersion = "any api version";
        String anyUrl = "any url";
        String somePayeReference = "some ref";
        String somePayFrequency = "any pay frequency";
        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        String anyEmployer = "any employer";
        Address anyEmployerAddress = null;

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, mockNameNormalizer, anyApiVersion, anyUrl);
        List<Income> incomes = Collections.emptyList();
        List<Employment> employments = Arrays.asList(
                new Employment(
                    somePayFrequency,
                    anyStartDate.toString(),
                    anyEndDate.toString(),
                    new Employer(
                            somePayeReference,
                            anyEmployer,
                            anyEmployerAddress)));

        Map<String, String> p = client.createEmployerPaymentRefMap(employments);

        client.addPaymentFrequency(incomes, p);

        assertThat(incomes).isEmpty();
    }

    @Test
    public void shouldDefaultPaymentFrequencyWhenNoPaymentFrequency() {

        RestTemplate anyRestTemplate = new RestTemplate();
        NinoUtils anyNinoUtils = new NinoUtils();
        String anyApiVersion = "any api version";
        String anyUrl = "any url";
        LocalDate anyPaymentDate = LocalDate.now().minusMonths(1);
        String somePayeReference = "some ref";
        BigDecimal anyTaxablePayment = new BigDecimal("0");
        BigDecimal anyNonTaxablePayment = new BigDecimal("0");
        Integer anyWeekPayNumber = 1;
        Integer anyMonthPayNumber = 1;
        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        String anyEmployer = "any employer";
        Address anyEmployerAddress = null;

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, mockNameNormalizer, anyApiVersion, anyUrl);

        List<Employment> employments = Arrays.asList(
            new Employment(
                null,
                anyStartDate.toString(),
                anyEndDate.toString(),
                new Employer(
                        somePayeReference,
                        anyEmployer,
                        anyEmployerAddress)));

        List<Income> incomes = Arrays.asList(
            new Income(
                somePayeReference,
                anyTaxablePayment,
                anyNonTaxablePayment,
                anyPaymentDate.toString(),
                anyWeekPayNumber,
                anyMonthPayNumber,
                null));

        Map<String, String> p = client.createEmployerPaymentRefMap(employments);

        assertThat(incomes.get(0).getPaymentFrequency()).isEqualTo(null);

        client.addPaymentFrequency(incomes, p);

        assertThat(incomes.get(0).getPaymentFrequency()).isEqualTo("ONE_OFF");
    }

    @Test
    public void shouldAddPaymentFrequencyToIncomeData() {

        RestTemplate anyRestTemplate = new RestTemplate();
        NinoUtils anyNinoUtils = new NinoUtils();
        String anyApiVersion = "any api version";
        String anyUrl = "any url";
        LocalDate anyPaymentDate = LocalDate.now().minusMonths(1);
        String somePayeReference = "some ref";
        BigDecimal anyTaxablePayment = new BigDecimal("0");
        BigDecimal anyNonTaxablePayment = new BigDecimal("0");
        Integer anyWeekPayNumber = 1;
        Integer anyMonthPayNumber = 1;
        String somePayFrequency = "any pay frequency";
        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        String anyEmployer = "any employer";
        Address anyEmployerAddress = null;

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, mockNameNormalizer, anyApiVersion, anyUrl);

        List<Employment> employments = Arrays.asList(
                new Employment(
                        somePayFrequency,
                        anyStartDate.toString(),
                        anyEndDate.toString(),
                        new Employer(
                                somePayeReference,
                                anyEmployer,
                                anyEmployerAddress)));

        List<Income> incomes = Arrays.asList(
                new Income(
                        somePayeReference,
                        anyTaxablePayment,
                        anyNonTaxablePayment,
                        anyPaymentDate.toString(),
                        anyWeekPayNumber,
                        anyMonthPayNumber,
                        null));

        Map<String, String> p = client.createEmployerPaymentRefMap(employments);

        assertThat(incomes.get(0).getPaymentFrequency()).isEqualTo(null);

        client.addPaymentFrequency(incomes, p);

        assertThat(incomes.get(0).getPaymentFrequency()).isEqualTo(somePayFrequency);
    }

    @Test
    public void shouldThrowExceptionForHttpUnauthorised() {
        final String baseHmrcUrl = "http://localhost";
        final URI uri = URI.create(baseHmrcUrl + "/individuals/matching/");
        final String hmrcApiVersion = "1";

        HmrcClient hmrcClient = new HmrcClient(mockRestTemplate, mockNinoUtils, mockNameNormalizer, hmrcApiVersion, baseHmrcUrl);

        when(mockRestTemplate.exchange(eq(uri), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> {
            hmrcClient.getIncome(
                    "ACCESS_TOKEN",
                    new Individual("first", "last", "nino", LocalDate.now()),
                    LocalDate.now(),
                    LocalDate.now()
            );
        }).isInstanceOf(HmrcUnauthorisedException.class);

    }


    @Test(expected = HttpClientErrorException.class)
    public void shouldNotThrowHmrcNotFoundExceptionWhenNot403() {
        NinoUtils anyNinoUtils = new NinoUtils();
        String anyApiVersion = "any api version";

        when(mockRestTemplate.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenThrow(
                new HttpClientErrorException(NOT_FOUND));

        HmrcClient hmrcClient = new HmrcClient(mockRestTemplate, anyNinoUtils, mockNameNormalizer, anyApiVersion, "some-resource");

        LocalDate now = LocalDate.now();
        hmrcClient.getIncome("some access token", new Individual("somefirstname", "somelastname", "some nino", now), now, now);
    }

    @Test(expected = HmrcNotFoundException.class)
    public void shouldThrowHmrcNotFoundExceptionWhenForbiddenFromHmrc() {
        NinoUtils anyNinoUtils = new NinoUtils();
        String anyApiVersion = "any api version";

        String responseBody = "{\"code\" : \"MATCHING_FAILED\", \"message\" : \"There is no match for the information provided\"}";
        Charset defaultCharset = Charset.defaultCharset();
        HttpClientErrorException exception = new HttpClientErrorException(FORBIDDEN, "", responseBody.getBytes(defaultCharset), defaultCharset);
        when(mockRestTemplate.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenThrow(exception);

        HmrcClient hmrcClient = new HmrcClient(mockRestTemplate, anyNinoUtils, mockNameNormalizer, anyApiVersion, "some-resource");

        LocalDate now = LocalDate.now();
        hmrcClient.getIncome("some access token", new Individual("somefirstname", "somelastname", "some nino", now), now, now);
    }

    @Test
    public void shouldThrowProxyForbiddenExceptionWhenForbiddenFromProxy() {
        // given
        when(mockRestTemplate.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenThrow(new HttpClientErrorException(FORBIDDEN));

        HmrcClient hmrcClient = new HmrcClient(mockRestTemplate, new NinoUtils(), mockNameNormalizer, "", "some-resource");

        LocalDate now = LocalDate.now();
        Individual testIndividual = new Individual("somefirstname", "somelastname", "some nino", now);

        // when
        assertThatThrownBy(() -> hmrcClient.getIncome("some access token", testIndividual, now, now))
                .isInstanceOf(ApplicationExceptions.ProxyForbiddenException.class);
    }
}
