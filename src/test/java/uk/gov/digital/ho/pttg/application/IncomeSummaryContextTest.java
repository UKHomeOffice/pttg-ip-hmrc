package uk.gov.digital.ho.pttg.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import uk.gov.digital.ho.pttg.application.domain.Individual;
import uk.gov.digital.ho.pttg.dto.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static uk.gov.digital.ho.pttg.application.IncomeDataTypes.PAYE;
import static uk.gov.digital.ho.pttg.application.IncomeDataTypes.SELF_ASSESSMENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_EMPLOYERLESS_EMPLOYMENT;

public class IncomeSummaryContextTest {

    private static final String LOG_TEST_APPENDER = "LOG_TEST_APPENDER";
    private IncomeSummaryContext incomeSummaryContext;
    private Link mockLink;

    @Before
    public void setup() {
        incomeSummaryContext = new IncomeSummaryContext();
        mockLink = mock(Link.class);
    }

    @Test
    public void shouldRequireAllData() {
        assertThat(incomeSummaryContext.needsMatchResource()).isTrue();
        assertThat(incomeSummaryContext.needsIndividualResource()).isTrue();
        assertThat(incomeSummaryContext.needsIncomeResource()).isTrue();
        assertThat(incomeSummaryContext.needsEmploymentResource()).isTrue();
        assertThat(incomeSummaryContext.needsSelfAssessmentResource()).isTrue();
        assertThat(incomeSummaryContext.needsPayeIncome()).isTrue();
        assertThat(incomeSummaryContext.needsEmployments()).isTrue();
        assertThat(incomeSummaryContext.needsSelfAssessmentSelfEmploymentIncome()).isTrue();
    }

    @Test
    public void shouldSetMatchResource() {
        Resource<String> mockResource = mock(Resource.class);

        given(mockResource.getLink(anyString())).willReturn(mockLink);

        incomeSummaryContext.matchResource(mockResource);

        assertThat(incomeSummaryContext.needsMatchResource()).isFalse();
        assertThat(incomeSummaryContext.getMatchLink("some link")).isEqualTo(mockLink);
    }

    @Test
    public void shouldSetIndividualResource() {
        Resource<EmbeddedIndividual> mockResource = mock(Resource.class);

        given(mockResource.getLink(anyString())).willReturn(mockLink);

        incomeSummaryContext.individualResource(mockResource);

        assertThat(incomeSummaryContext.needsIndividualResource()).isFalse();
        assertThat(incomeSummaryContext.getIndividualLink("some link")).isEqualTo(mockLink);
    }

    @Test
    public void shouldSetIndividual() {
        Resource<EmbeddedIndividual> mockResource = mock(Resource.class);
        EmbeddedIndividual mockContent = mock(EmbeddedIndividual.class);
        Individual mockIndividual = mock(Individual.class);

        given(mockResource.getContent()).willReturn(mockContent);
        given(mockContent.getIndividual()).willReturn(mockIndividual);

        incomeSummaryContext.individualResource(mockResource);

        assertThat(incomeSummaryContext.getIndividual()).isEqualTo(mockIndividual);
    }

    @Test
    public void shouldSetIncomeResource() {
        Resource<String> mockResource = mock(Resource.class);

        given(mockResource.getLink(anyString())).willReturn(mockLink);

        incomeSummaryContext.incomeResource(mockResource);

        assertThat(incomeSummaryContext.needsIncomeResource()).isFalse();
        assertThat(incomeSummaryContext.getIncomeLink("some link")).isEqualTo(mockLink);
    }

    @Test
    public void shouldSetEmploymentResource() {
        Resource<String> mockResource = mock(Resource.class);

        given(mockResource.getLink(anyString())).willReturn(mockLink);

        incomeSummaryContext.employmentResource(mockResource);

        assertThat(incomeSummaryContext.needsEmploymentResource()).isFalse();
        assertThat(incomeSummaryContext.getEmploymentLink("some link")).isEqualTo(mockLink);
    }

    @Test
    public void shouldSetSelfAssessmentResource() {
        Resource<String> mockResource = mock(Resource.class);

        given(mockResource.getLink(anyString())).willReturn(mockLink);

        incomeSummaryContext.selfAssessmentResource(mockResource);

        assertThat(incomeSummaryContext.needsSelfAssessmentResource()).isFalse();
        assertThat(incomeSummaryContext.getSelfAssessmentLink("some link")).isEqualTo(mockLink);
    }

    @Test
    public void shouldSetPayeIncome() {
        List<Income> mockIncome = mock(List.class);

        incomeSummaryContext.payeIncome(mockIncome);

        assertThat(incomeSummaryContext.needsPayeIncome()).isFalse();
        assertThat(incomeSummaryContext.payeIncome()).isEqualTo(mockIncome);
    }

    @Test
    public void shouldSetEmployments() {
        List<Employment> mockEmployments = mock(List.class);

        incomeSummaryContext.employments(mockEmployments);

        assertThat(incomeSummaryContext.needsEmployments()).isFalse();
        assertThat(incomeSummaryContext.employments()).isEqualTo(mockEmployments);
    }

    @Test
    public void shouldSetSelfAssessmentSelfEmploymentIncome() {
        List<AnnualSelfAssessmentTaxReturn> mockAnnualSelfAssessmentTaxReturns = mock(List.class);

        incomeSummaryContext.selfAssessmentSelfEmploymentIncome(mockAnnualSelfAssessmentTaxReturns);

        assertThat(incomeSummaryContext.needsSelfAssessmentSelfEmploymentIncome()).isFalse();
        assertThat(incomeSummaryContext.selfAssessmentSelfEmploymentIncome()).isEqualTo(mockAnnualSelfAssessmentTaxReturns);
    }

    @Test
    public void incomeDataAvailable_whenNone_shouldReturnEmptyList() {
        assertThat(incomeSummaryContext.availableIncomeData()).isEmpty();
    }

    @Test
    public void incomeDataAvailable_whenPAYE_shouldReturnPAYE() {
        incomeSummaryContext.payeIncome(asList(new Income(null, null , null, null, null, null, null)));
        incomeSummaryContext.selfAssessmentSelfEmploymentIncome(EMPTY_LIST);
        assertThat(incomeSummaryContext.availableIncomeData()).containsExactly(PAYE);
    }

    @Test
    public void incomeDataAvailable_whenSAE_shouldReturnSA() {
        incomeSummaryContext.payeIncome(EMPTY_LIST);
        incomeSummaryContext.selfAssessmentSelfEmploymentIncome(asList(new AnnualSelfAssessmentTaxReturn(null, null, null)));
        assertThat(incomeSummaryContext.availableIncomeData()).containsExactly(SELF_ASSESSMENT);
    }

    @Test
    public void incomeDataAvailable_whenBOTH_shouldReturnBOTH() {
        incomeSummaryContext.payeIncome(asList(new Income(null, null , null, null, null, null, null)));
        incomeSummaryContext.selfAssessmentSelfEmploymentIncome(asList(new AnnualSelfAssessmentTaxReturn(null, null, null)));
        assertThat(incomeSummaryContext.availableIncomeData()).containsExactly(PAYE, SELF_ASSESSMENT);
    }


    @Test
    public void shouldProduceEmptyMap() {
        Map<String, String> p = incomeSummaryContext.createEmployerPaymentRefMap();
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

        List<Employment> employments = asList(
                new Employment(
                        somePayFrequency,
                        anyStartDate.toString(),
                        anyEndDate.toString(),
                        new Employer(
                                somePayeReference,
                                anyEmployer,
                                anyEmployerAddress)));

        incomeSummaryContext.employments(employments);

        Map<String, String> p = incomeSummaryContext.createEmployerPaymentRefMap();

        assertThat(p).size().isEqualTo(1);
        assertThat(p).containsKey(somePayeReference);
        assertThat(p.get(somePayeReference)).isEqualTo(somePayFrequency);
    }

    @Test
    public void shouldProduceMapWithMultipleEntries() {

        LocalDate anyStartDate = LocalDate.now().minusYears(1);
        LocalDate anyEndDate = LocalDate.now();
        Address anyEmployerAddress = null;
        String somePayFrequency = "some pay frequency";
        String anyEmployer = "any employer";
        String somePayeReference = "some ref";
        String anotherPayFrequency = "another pay frequency";
        String anotherPayeReference = "another pay reference";

        List<Employment> employments = asList(
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

        incomeSummaryContext.employments(employments);

        Map<String, String> p = incomeSummaryContext.createEmployerPaymentRefMap();

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

        List<Employment> employments = asList(
                new Employment(
                        somePayFrequency,
                        anyStartDate.toString(),
                        anyEndDate.toString(),
                        new Employer(
                                somePayeReference,
                                anyEmployer,
                                anyEmployerAddress)));

        incomeSummaryContext.employments(employments);

        Map<String, String> p = incomeSummaryContext.createEmployerPaymentRefMap();

        incomeSummaryContext.addPaymentFrequency(p);

        assertThat(incomeSummaryContext.payeIncome()).isEmpty();
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

        List<Employment> employments = asList(
                new Employment(
                        null,
                        anyStartDate.toString(),
                        anyEndDate.toString(),
                        new Employer(
                                somePayeReference,
                                anyEmployer,
                                anyEmployerAddress)));

        List<Income> incomes = asList(
                new Income(
                        somePayeReference,
                        anyTaxablePayment,
                        anyNonTaxablePayment,
                        anyPaymentDate.toString(),
                        anyWeekPayNumber,
                        anyMonthPayNumber,
                        null));

        incomeSummaryContext.payeIncome(incomes);
        incomeSummaryContext.employments(employments);

        Map<String, String> p = incomeSummaryContext.createEmployerPaymentRefMap();

        assertThat(incomes.get(0).getPaymentFrequency()).isEqualTo(null);

        incomeSummaryContext.addPaymentFrequency(p);

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

        List<Employment> employments = asList(
                new Employment(
                        somePayFrequency,
                        anyStartDate.toString(),
                        anyEndDate.toString(),
                        new Employer(
                                somePayeReference,
                                anyEmployer,
                                anyEmployerAddress)));

        List<Income> incomes = asList(
                new Income(
                        somePayeReference,
                        anyTaxablePayment,
                        anyNonTaxablePayment,
                        anyPaymentDate.toString(),
                        anyWeekPayNumber,
                        anyMonthPayNumber,
                        null));

        incomeSummaryContext.payeIncome(incomes);
        incomeSummaryContext.employments(employments);

        Map<String, String> p = incomeSummaryContext.createEmployerPaymentRefMap();

        assertThat(incomes.get(0).getPaymentFrequency()).isEqualTo(null);

        incomeSummaryContext.addPaymentFrequency(p);

        assertThat(incomes.get(0).getPaymentFrequency()).isEqualTo(somePayFrequency);
    }

    @Test
    public void payeIncome_nullArg_throwsException() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> incomeSummaryContext.payeIncome(null));
    }

    @Test
    public void payeIncome_nonNullArg_setsProperty() {
        incomeSummaryContext.payeIncome(asList(new Income(null, null , null, null, null, null, null)));
        assertThat(incomeSummaryContext.payeIncome()).isEqualTo(asList(new Income(null, null , null, null, null, null, null)));
    }

    @Test
    public void employments_nullArg_throwsException() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> incomeSummaryContext.employments(null));
    }

    @Test
    public void employments_nonNullArg_setsProperty() {
        incomeSummaryContext.employments(asList(new Employment(null, null, null, null)));
        assertThat(incomeSummaryContext.employments()).isEqualTo(asList(new Employment(null, null, null, null)));
    }

    @Test
    public void selfAssessmentSelfEmploymentIncome_nullArg_throwsException() {
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> incomeSummaryContext.selfAssessmentSelfEmploymentIncome(null));
    }

    @Test
    public void selfAssessmentSelfEmploymentIncome_nonNullArg_setsProperty() {
        incomeSummaryContext.selfAssessmentSelfEmploymentIncome(asList(new AnnualSelfAssessmentTaxReturn(null, null, null)));
        assertThat(incomeSummaryContext.selfAssessmentSelfEmploymentIncome()).isEqualTo(asList(new AnnualSelfAssessmentTaxReturn(null, null, null)));
    }

    @Test
    public void createEmployerPaymentRefMap_employerlessEmployment_ignoresTheEmployment() {
        incomeSummaryContext.employments(asList(new Employment(null, null, null, null)));

        assertThat(incomeSummaryContext.createEmployerPaymentRefMap()).isEmpty();
    }

    @Test
    public void createEmployerPaymentRefMap_employerlessEmployment_logsEvent() {
        Appender<ILoggingEvent> mockAppender = mock(Appender.class);
        mockAppender.setName(LOG_TEST_APPENDER);

        Logger logger = (Logger) LoggerFactory.getLogger(IncomeSummaryContext.class);
        logger.setLevel(Level.WARN);
        logger.addAppender(mockAppender);

        incomeSummaryContext.employments(asList(new Employment(null, null, null, null)));

        incomeSummaryContext.createEmployerPaymentRefMap();

        Mockito.verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            boolean expectedMessage = loggingEvent.getFormattedMessage().equals("HMRC Employment data without an Employer");

            ObjectAppendingMarker marker = (ObjectAppendingMarker) loggingEvent.getArgumentArray()[0];

            boolean expectedEvent = marker.getFieldName().equals("event_id") &&
                                    getField(marker, "object").equals(HMRC_EMPLOYERLESS_EMPLOYMENT);

            return expectedMessage && expectedEvent;
        }));

        logger.detachAppender(LOG_TEST_APPENDER);
    }
}
