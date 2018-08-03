package uk.gov.digital.ho.pttg.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.application.util.NameNormalizer;
import uk.gov.digital.ho.pttg.application.util.TraversonUtils;
import uk.gov.digital.ho.pttg.dto.*;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
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
    private TraversonUtils mockTraversonUtils;

    @Mock
    private NameNormalizer mockNameNormalizer;
    @Mock
    private Appender<ILoggingEvent> mockAppender;
    @Mock
    private ResponseEntity mockResponse;
    private Individual individual = new Individual("John", "Smith", "12345677", LocalDate.of(2018, 7, 30));

    @Before
    public void setup() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(HmrcClient.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
    }

    @Test
    public void shouldProduceEmptyMap() {

        HmrcClient client = new HmrcClient(new RestTemplate(), new NinoUtils(), new TraversonUtils(), mockNameNormalizer, "any api version", "any url");

        Map<String, String> p = client.createEmployerPaymentRefMap(new ArrayList<>());

        assertThat(p).isEmpty();
    }

    @Test
    public void shouldLogInfoBeforeMatchingRequestSent() {
        when(mockRestTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(mockResponse);
        HmrcClient client = new HmrcClient(mockRestTemplate, new NinoUtils(), new TraversonUtils(), mockNameNormalizer, "any api version", "http://something.com/anyurl");

        client.getMatchResource(individual, "", "testurl");

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Match Individual 12345*** via a POST to testurl") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoAfterMatchingRequestSent() {
        when(mockRestTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenReturn(mockResponse);
        HmrcClient client = new HmrcClient(mockRestTemplate, new NinoUtils(), new TraversonUtils(), mockNameNormalizer, "any api version", "http://something.com/anyurl");

        client.getMatchResource(individual, "", "testurl");

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Successfully matched individual 12345***") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoAfterMatchingFailure() {
        when(mockRestTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenThrow(
                new HttpClientErrorException(FORBIDDEN, "No match", "MATCHING_FAILED".getBytes(Charset.defaultCharset()), null)
        );
        HmrcClient client = new HmrcClient(mockRestTemplate, new NinoUtils(), new TraversonUtils(), mockNameNormalizer, "any api version", "http://something.com/anyurl");

        try {
            client.getMatchResource(individual, "", "testurl");
        } catch (HmrcNotFoundException e) {
            // Swallowed as not of interest for this test.
        }

        verify(mockAppender, atLeast(1)).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Failed to match individual 12345***") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[1]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoForEveryMatchingAttempt() {
        when(mockRestTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenThrow(
                new HttpClientErrorException(FORBIDDEN, "No match", "MATCHING_FAILED".getBytes(Charset.defaultCharset()), null)
        );
        HmrcClient client = new HmrcClient(mockRestTemplate, new NinoUtils(), new TraversonUtils(), mockNameNormalizer, "any api version", "http://something.com/anyurl");


        try {
            client.getMatchResource(individual, "", "testurl");
        } catch (HmrcNotFoundException e) {
            // Swallowed as not of interest for this test.
        }

        verify(mockAppender, atLeast(1)).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Match attempt 1 of 2") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
        }));

        verify(mockAppender, atLeast(1)).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Match attempt 2 of 2") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[2]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldProduceMapWithOneEntry() {

        RestTemplate anyRestTemplate = new RestTemplate();
        NinoUtils anyNinoUtils = new NinoUtils();
        TraversonUtils anyTraversonUtils = new TraversonUtils();
        String anyApiVersion = "any api version";
        String anyUrl = "any url";
        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        Address anyEmployerAddress = null;
        String somePayFrequency = "some pay frequency";
        String anyEmployer = "any employer";

        String somePayeReference = "some ref";

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, anyTraversonUtils, mockNameNormalizer, anyApiVersion, anyUrl);
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
        TraversonUtils anyTraversonUtils = new TraversonUtils();
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

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, anyTraversonUtils, mockNameNormalizer, anyApiVersion, anyUrl);
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
        TraversonUtils anyTraversonUtils = new TraversonUtils();
        String anyApiVersion = "any api version";
        String anyUrl = "any url";
        String somePayeReference = "some ref";
        String somePayFrequency = "any pay frequency";
        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        String anyEmployer = "any employer";
        Address anyEmployerAddress = null;

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, anyTraversonUtils, mockNameNormalizer, anyApiVersion, anyUrl);
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
        TraversonUtils anyTraversonUtils = new TraversonUtils();
        String anyApiVersion = "any api version";
        String anyUrl = "any url";
        String somePayeReference = "some ref";
        String somePayFrequency = "any pay frequency";
        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        String anyEmployer = "any employer";
        Address anyEmployerAddress = null;

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, anyTraversonUtils, mockNameNormalizer, anyApiVersion, anyUrl);
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
        TraversonUtils anyTraversonUtils = new TraversonUtils();
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

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, anyTraversonUtils, mockNameNormalizer, anyApiVersion, anyUrl);

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
        TraversonUtils anyTraversonUtils = new TraversonUtils();
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

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, anyTraversonUtils, mockNameNormalizer, anyApiVersion, anyUrl);

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

        HmrcClient hmrcClient = new HmrcClient(mockRestTemplate, mockNinoUtils, mockTraversonUtils, mockNameNormalizer, hmrcApiVersion, baseHmrcUrl);

        when(mockRestTemplate.exchange(eq(uri), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> {
            hmrcClient.getIncomeSummary(
                    "ACCESS_TOKEN",
                    new Individual("first", "last", "nino", LocalDate.now()),
                    LocalDate.now(),
                    LocalDate.now(),
                    new IncomeSummaryContext()
            );
        }).isInstanceOf(HmrcUnauthorisedException.class);

    }

    @Test(expected = HttpClientErrorException.class)
    public void shouldNotThrowHmrcNotFoundExceptionWhenNot403() {
        NinoUtils anyNinoUtils = new NinoUtils();
        TraversonUtils anyTraversonUtils = new TraversonUtils();
        String anyApiVersion = "any api version";

        when(mockRestTemplate.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenThrow(
                new HttpClientErrorException(NOT_FOUND));

        HmrcClient hmrcClient = new HmrcClient(mockRestTemplate, anyNinoUtils, anyTraversonUtils, mockNameNormalizer, anyApiVersion, "some-resource");

        LocalDate now = LocalDate.now();
        hmrcClient.getIncomeSummary("some access token", new Individual("somefirstname", "somelastname", "some nino", now), now, now, new IncomeSummaryContext());
    }

    @Test(expected = HmrcNotFoundException.class)
    public void shouldThrowHmrcNotFoundExceptionWhenForbiddenFromHmrc() {
        NinoUtils anyNinoUtils = new NinoUtils();
        TraversonUtils anyTraversonUtils = new TraversonUtils();
        String anyApiVersion = "any api version";

        String responseBody = "{\"code\" : \"MATCHING_FAILED\", \"message\" : \"There is no match for the information provided\"}";
        Charset defaultCharset = Charset.defaultCharset();
        HttpClientErrorException exception = new HttpClientErrorException(FORBIDDEN, "", responseBody.getBytes(defaultCharset), defaultCharset);
        when(mockRestTemplate.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenThrow(exception);

        HmrcClient hmrcClient = new HmrcClient(mockRestTemplate, anyNinoUtils, anyTraversonUtils, mockNameNormalizer, anyApiVersion, "some-resource");

        LocalDate now = LocalDate.now();
        hmrcClient.getIncomeSummary("some access token", new Individual("somefirstname", "somelastname", "some nino", now), now, now, new IncomeSummaryContext());
    }

    @Test
    public void shouldThrowProxyForbiddenExceptionWhenForbiddenFromProxy() {
        when(mockRestTemplate.exchange(any(), eq(POST), any(HttpEntity.class), any(ParameterizedTypeReference.class))).thenThrow(new HttpClientErrorException(FORBIDDEN));

        HmrcClient hmrcClient = new HmrcClient(mockRestTemplate, new NinoUtils(), new TraversonUtils(), mockNameNormalizer, "", "some-resource");

        LocalDate now = LocalDate.now();
        Individual testIndividual = new Individual("somefirstname", "somelastname", "some nino", now);

        assertThatThrownBy(() -> hmrcClient.getIncomeSummary("some access token", testIndividual, now, now, new IncomeSummaryContext()))
                .isInstanceOf(ApplicationExceptions.ProxyForbiddenException.class);
    }

    @Test
    public void shouldLogInfoBeforePayeRequestSent() {
        // given
        Resource<Object> incomeResource = new Resource<>(new PayeIncome(new Incomes(new ArrayList<Income>())), new Link("http://www.foo.com/bar"));
        given(mockTraversonUtils.followTraverson(anyString(), anyString(), anyString(), any(RestTemplate.class), any())).willReturn(incomeResource);

        HmrcClient client = new HmrcClient(mockRestTemplate, new NinoUtils(), mockTraversonUtils, mockNameNormalizer, "application/json", "http://something.com/anyurl");

        // when
        client.getPayeIncome(LocalDate.of(2018, 8, 1), LocalDate.of(2018, 8, 1), "token", new Link("http://foo.com/bar"));

        // then
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Sending PAYE request to HMRC") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogInfoAfterPayeResponseReceived() {
        // given
        Resource<Object> incomeResource = new Resource<>(new PayeIncome(new Incomes(new ArrayList<Income>())), new Link("http://www.foo.com/bar"));
        given(mockTraversonUtils.followTraverson(anyString(), anyString(), anyString(), any(RestTemplate.class), any())).willReturn(incomeResource);

        HmrcClient client = new HmrcClient(mockRestTemplate, new NinoUtils(), mockTraversonUtils, mockNameNormalizer, "application/json", "http://something.com/anyurl");

        // when
        client.getPayeIncome(LocalDate.of(2018, 8, 1), LocalDate.of(2018, 8, 1), "token", new Link("http://foo.com/bar"));

        // then
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("PAYE response received from HMRC") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }
}
