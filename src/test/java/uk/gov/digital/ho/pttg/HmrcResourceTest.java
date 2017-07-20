package uk.gov.digital.ho.pttg;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.dto.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HmrcResourceTest {


    public static final String FIRST_NAME = "Den";
    public static final String LAST_NAME = "Chimes";
    public static final String NINO = "AA654321AA";
    public static final LocalDate DATE_OF_BIRTH = LocalDate.of(1975, 6, 21);
    public static final LocalDate FROM_DATE = LocalDate.of(2016, 6, 21);
    public static final LocalDate TO_DATE = LocalDate.of(2016, 6, 21);
    @Mock
    private HmrcClient mockClient;
    private HmrcResource resource;

    @Before
    public void setUp() {
        resource = new HmrcResource(mockClient);
    }


    @Test
    public void testCollaboratorsWhenCreatingCase() {

        when(mockClient.getIncome(new Individual(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH), FROM_DATE, TO_DATE)).thenReturn(buildIncomeSummary());

        resource.getHmrcData(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH, FROM_DATE, TO_DATE);

        verify(mockClient).getIncome(new Individual(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH), FROM_DATE, TO_DATE);
    }

    @Test
    public void shouldAllowOptionalToDate() {

        when(mockClient.getIncome(new Individual(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH), FROM_DATE, null)).thenReturn(buildIncomeSummary());

        resource.getHmrcData(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH, FROM_DATE, null);

        verify(mockClient).getIncome(new Individual(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH), FROM_DATE, null);
    }

    private IncomeSummary buildIncomeSummary() {
        final ImmutableList<Income> incomes = ImmutableList.of(new Income("payref", new BigDecimal(4.5), new BigDecimal(6.5), "2017-01-01", 1, null));
        final Employer employer = new Employer("payref", "Cadburys", new Address("line1", "line2", "line3", "line4", "line5", "S102BB"));
        final ImmutableList<Employment> employment = ImmutableList.of(new Employment("WEEKLY", "2016-6-21", "2016-6-21", employer));
        return new IncomeSummary(incomes, employment);
    }

}