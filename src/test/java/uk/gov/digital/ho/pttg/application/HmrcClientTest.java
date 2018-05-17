package uk.gov.digital.ho.pttg.application;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.dto.Address;
import uk.gov.digital.ho.pttg.dto.Employer;
import uk.gov.digital.ho.pttg.dto.Employment;
import uk.gov.digital.ho.pttg.dto.Income;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class HmrcClientTest {

    @Test
    public void shouldProduceEmptyMap() {

        HmrcClient client = new HmrcClient(new RestTemplate(), new NinoUtils(),"any api version", "any url");

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

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, anyApiVersion, anyUrl);
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

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, anyApiVersion, anyUrl);
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

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, anyApiVersion, anyUrl);
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

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, anyApiVersion, anyUrl);
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

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, anyApiVersion, anyUrl);

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

        HmrcClient client = new HmrcClient(anyRestTemplate, anyNinoUtils, anyApiVersion, anyUrl);

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

}