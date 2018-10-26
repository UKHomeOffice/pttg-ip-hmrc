package uk.gov.digital.ho.pttg.application;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.hateoas.Link;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.digital.ho.pttg.application.domain.Individual;
import uk.gov.digital.ho.pttg.dto.Address;
import uk.gov.digital.ho.pttg.dto.Employer;
import uk.gov.digital.ho.pttg.dto.Employment;
import uk.gov.digital.ho.pttg.dto.Income;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HmrcClientTest {

    @Mock private HmrcHateoasClient mockHmrcHateoasClient;

    @Test
    public void shouldProduceEmptyMap() {

        HmrcClient client = new HmrcClient(mockHmrcHateoasClient);

        Map<String, String> p = client.createEmployerPaymentRefMap(new ArrayList<>());

        assertThat(p).isEmpty();
    }

    @Test
    public void shouldProduceMapWithOneEntry() {

        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        Address anyEmployerAddress = null;
        String somePayFrequency = "some pay frequency";
        String anyEmployer = "any employer";

        String somePayeReference = "some ref";

        HmrcClient client = new HmrcClient(mockHmrcHateoasClient);
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

        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        Address anyEmployerAddress = null;
        String somePayFrequency = "some pay frequency";
        String anyEmployer = "any employer";
        String somePayeReference = "some ref";
        String anotherPayFrequency = "another pay frequency";
        String anotherPayeReference = "another pay reference";

        HmrcClient client = new HmrcClient(mockHmrcHateoasClient);
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

        String somePayeReference = "some ref";
        String somePayFrequency = "any pay frequency";
        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        String anyEmployer = "any employer";
        Address anyEmployerAddress = null;

        HmrcClient client = new HmrcClient(mockHmrcHateoasClient);
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

        String somePayeReference = "some ref";
        String somePayFrequency = "any pay frequency";
        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        String anyEmployer = "any employer";
        Address anyEmployerAddress = null;

        HmrcClient client = new HmrcClient(mockHmrcHateoasClient);
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

        HmrcClient client = new HmrcClient(mockHmrcHateoasClient);

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

        HmrcClient client = new HmrcClient(mockHmrcHateoasClient);

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
    @SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void shouldGetSummaryIfConfigNotSelfEmploymentOnly() {
        HmrcClient hmrcClient = new HmrcClient(mockHmrcHateoasClient);
        ReflectionTestUtils.setField(hmrcClient, "selfEmploymentOnly", false);
        IncomeSummaryContext mockIncomeSummaryContext = mock(IncomeSummaryContext.class);
        when(mockIncomeSummaryContext.needsSelfAssessmentSummaryIncome()).thenReturn(true);
        when(mockIncomeSummaryContext.getSelfAssessmentLink(any(String.class))).thenReturn(mock(Link.class));

        hmrcClient.getIncomeSummary("some access token", mock(Individual.class), LocalDate.now(), LocalDate.now(), mockIncomeSummaryContext);

        verify(mockIncomeSummaryContext).needsSelfAssessmentSummaryIncome();
        verify(mockHmrcHateoasClient).getSelfAssessmentSummaryIncome(anyString(), any(Link.class));
        verify(mockHmrcHateoasClient, times(0)).getSelfAssessmentSelfEmploymentIncome(anyString(), any(Link.class));

    }

    @Test
    @SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void shouldGetSelfEmploymentIfConfigIsSelfEmploymentOnly() {
        HmrcClient hmrcClient = new HmrcClient(mockHmrcHateoasClient);
        ReflectionTestUtils.setField(hmrcClient, "selfEmploymentOnly", true);
        IncomeSummaryContext mockIncomeSummaryContext = mock(IncomeSummaryContext.class);
        when(mockIncomeSummaryContext.needsSelfAssessmentSelfEmploymentIncome()).thenReturn(true);
        when(mockIncomeSummaryContext.getSelfAssessmentLink(any(String.class))).thenReturn(mock(Link.class));

        hmrcClient.getIncomeSummary("some access token", mock(Individual.class), LocalDate.now(), LocalDate.now(), mockIncomeSummaryContext);

        verify(mockIncomeSummaryContext).needsSelfAssessmentSelfEmploymentIncome();
        verify(mockHmrcHateoasClient).getSelfAssessmentSelfEmploymentIncome(anyString(), any(Link.class));
        verify(mockHmrcHateoasClient, times(0)).getSelfAssessmentSummaryIncome(anyString(), any(Link.class));

    }
}
