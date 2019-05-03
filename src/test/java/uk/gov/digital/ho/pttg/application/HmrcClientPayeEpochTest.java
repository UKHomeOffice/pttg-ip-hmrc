package uk.gov.digital.ho.pttg.application;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.hateoas.Link;
import uk.gov.digital.ho.pttg.application.domain.Individual;

import java.time.LocalDate;
import java.time.Month;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
public class HmrcClientPayeEpochTest {

    private static final LocalDate DEFAULT_PAYE_EPOCH = LocalDate.of(2013, Month.MARCH, 31);
    private static final LocalDate SOME_TO_DATE = DEFAULT_PAYE_EPOCH.plusWeeks(1);

    private final LocalDate requestedFromDate;
    private final LocalDate epoch;
    private final LocalDate actualDateUsed;

    private HmrcHateoasClient mockHmrcHateoasClient;
    private IncomeSummaryContext mockIncomeSummaryContext;
    private Link anyLink;
    private Individual anyIndividual;

    private HmrcClient hmrcClient;

    @Before
    public void setUp() {
        anyLink = mock(Link.class);
        mockHmrcHateoasClient = mock(HmrcHateoasClient.class);
        anyIndividual = mock(Individual.class);

        mockIncomeSummaryContext = mock(IncomeSummaryContext.class);
        given(mockIncomeSummaryContext.needsPayeIncome()).willReturn(true);
        given(mockIncomeSummaryContext.getIncomeLink(anyString())).willReturn(anyLink);
        given(mockIncomeSummaryContext.needsEmployments()).willReturn(true);
        given(mockIncomeSummaryContext.getEmploymentLink(anyString())).willReturn(anyLink);

        hmrcClient = new HmrcClient(mockHmrcHateoasClient, epoch);
    }

    @Parameterized.Parameters(name = "When requesting a fromDate of {0}, with and epoch of {1} the date of {2} will actually be used")
    public static Iterable<Object[]> testCases() {
        Object[] fromDateAfterEpoch = {DEFAULT_PAYE_EPOCH.plusDays(1), DEFAULT_PAYE_EPOCH, DEFAULT_PAYE_EPOCH.plusDays(1)};
        Object[] fromDateOnEpoch = {DEFAULT_PAYE_EPOCH, DEFAULT_PAYE_EPOCH, DEFAULT_PAYE_EPOCH};
        Object[] fromDateBeforeEpoch = {DEFAULT_PAYE_EPOCH.minusDays(1), DEFAULT_PAYE_EPOCH, DEFAULT_PAYE_EPOCH};

        // fromDate not restricted if LocalDate.MIN used for Epoch
        Object[] epochLocalDateMin = {DEFAULT_PAYE_EPOCH.minusDays(1), LocalDate.MIN, DEFAULT_PAYE_EPOCH.minusDays(1)};

        return asList(fromDateAfterEpoch, fromDateOnEpoch, fromDateBeforeEpoch, epochLocalDateMin);
    }

    public HmrcClientPayeEpochTest(LocalDate requestedFromDate, LocalDate epoch, LocalDate actualDateUsed) {
        this.requestedFromDate = requestedFromDate;
        this.epoch = epoch;
        this.actualDateUsed = actualDateUsed;
    }

    @Test
    public void getPaye_fromDate_useEpochIfTooEarly() {
        hmrcClient.populateIncomeSummary("any access token", anyIndividual, requestedFromDate, SOME_TO_DATE, mockIncomeSummaryContext);

        then(mockHmrcHateoasClient)
                .should()
                .getPayeIncome(eq(actualDateUsed), eq(SOME_TO_DATE), anyString(), any(Link.class));
        then(mockHmrcHateoasClient)
                .should()
                .getEmployments(eq(actualDateUsed), eq(SOME_TO_DATE), anyString(), any(Link.class));

    }
}
