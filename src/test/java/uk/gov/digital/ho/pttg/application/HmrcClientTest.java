package uk.gov.digital.ho.pttg.application;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.dto.Address;
import uk.gov.digital.ho.pttg.dto.Employer;
import uk.gov.digital.ho.pttg.dto.Employment;
import uk.gov.digital.ho.pttg.dto.Income;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

public class HmrcClientTest {

    @Test
    public void shouldProduceEmptyMap() {

        RestTemplate anyRestTemplate = new RestTemplate();
        String anyUrl = "any url";

        HmrcClient client = new HmrcClient(anyRestTemplate, anyUrl);
        List<Employment> employments = new ArrayList<>();

        Map<String, String> p = client.produceMap(employments);

        assertThat(p).isEmpty();
    }

    @Test
    public void shouldProduceMapWithOneEntry() {

        RestTemplate anyRestTemplate = new RestTemplate();
        String anyUrl = "any url";
        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        Address anyEmployerAddress = null;
        String somePayFrequency = "any pay frequency";
        String anyEmployer = "any employer";

        String somePayeReference = "some ref";

        HmrcClient client = new HmrcClient(anyRestTemplate, anyUrl);
        List<Employment> employments = new ArrayList<>();

        employments.add(0, new Employment(
                somePayFrequency,
                anyStartDate.toString(),
                anyEndDate.toString(),
                new Employer(
                        somePayeReference,
                        anyEmployer,
                        anyEmployerAddress)));

        Map<String, String> p = client.produceMap(employments);

        assertThat(p).size().isEqualTo(1);
        assertThat(p).containsKey(somePayeReference);
        assertThat(p.get(somePayeReference)).isEqualTo(somePayFrequency);
    }

    @Test
    public void shouldProduceMapWithMutlipleEntries() {

        RestTemplate anyRestTemplate = new RestTemplate();
        String anyUrl = "any url";
        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        Address anyEmployerAddress = null;
        String somePayFrequency = "any pay frequency";
        String anyEmployer = "any employer";
        String somePayeReference = "some ref";
        String anotherPayFrequency = "another pay frequency";
        String anotherPayeReference = "another pay reference";

        HmrcClient client = new HmrcClient(anyRestTemplate, anyUrl);
        List<Employment> employments = new ArrayList<>();

        employments.add(0, new Employment(
                somePayFrequency,
                anyStartDate.toString(),
                anyEndDate.toString(),
                new Employer(
                        somePayeReference,
                        anyEmployer,
                        anyEmployerAddress)));

        employments.add(1, new Employment(
                anotherPayFrequency,
                anyStartDate.toString(),
                anyEndDate.toString(),
                new Employer(
                        anotherPayeReference,
                        anyEmployer,
                        anyEmployerAddress)));

        Map<String, String> p = client.produceMap(employments);

        assertThat(p).size().isEqualTo(2);
        assertThat(p).containsKey(anotherPayeReference);
        assertThat(p.get(anotherPayeReference)).isEqualTo(anotherPayFrequency);
    }

    @Test
    public void shouldAddPaymentFrequencyToIncomeData() {

        RestTemplate anyRestTemplate = new RestTemplate();
        String anyUrl = "any url";
        LocalDate anyPaymentDate = LocalDate.now().minusMonths(1);
        String somePayeReference = "some ref";
        BigDecimal anyTaxablePayment = new BigDecimal("0");
        BigDecimal anyNonTaxablePayment = new BigDecimal("0");
        Integer anyWeekPayNumber = new Integer("1");
        Integer anyMonthPayNumber = new Integer("1");
        String somePayFrequency = "any pay frequency";
        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        String anyEmployer = "any employer";
        Address anyEmployerAddress = null;

        HmrcClient client = new HmrcClient(anyRestTemplate, anyUrl);
        List<Income> incomes = new ArrayList<>();
        List<Employment> employments = new ArrayList<>();

        Income someIncome = new Income(
                somePayeReference,
                anyTaxablePayment,
                anyNonTaxablePayment,
                anyPaymentDate.toString(),
                anyWeekPayNumber,
                anyMonthPayNumber, "");

        incomes.add(0, someIncome);

        employments.add(0, new Employment(
                somePayFrequency,
                anyStartDate.toString(),
                anyEndDate.toString(),
                new Employer(
                        somePayeReference,
                        anyEmployer,
                        anyEmployerAddress)));

        Map<String, String> p = client.produceMap(employments);

        client.addPaymentFrequency(incomes, p);

        assertThat(someIncome.getPaymentFrequency()).isEqualTo(somePayFrequency);
    }


}
