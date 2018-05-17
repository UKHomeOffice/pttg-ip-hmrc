package uk.gov.digital.ho.pttg;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.api.HmrcResource;
import uk.gov.digital.ho.pttg.api.IncomeSummaryService;
import uk.gov.digital.ho.pttg.application.NinoUtils;
import uk.gov.digital.ho.pttg.dto.IncomeSummary;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HmrcResourceTest {
    private static final String NINO = "Nino";
    private static final String LAST_NAME = "LastName";
    private static final String FIRST_NAME = "FirstName";
    private static final LocalDate TO_DATE = LocalDate.of(2018, Month.MAY, 1);
    private static final LocalDate FROM_DATE = LocalDate.of(2018, Month.JANUARY, 1);
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, Month.DECEMBER, 25);

    @Mock
    private IncomeSummaryService mockIncomeSummaryService;

    @Mock
    private NinoUtils mockNinoUtils;

    @Mock
    private IncomeSummary mockIncomeSummary;

    @InjectMocks
    private HmrcResource hmrcResource;

    @Test
    public void shouldCallIncomeSummaryServiceCorrectlyOnInvocation() {
        // given
        when(mockIncomeSummaryService.getIncomeSummary(isA(Individual.class), eq(FROM_DATE), eq(TO_DATE))).thenReturn(mockIncomeSummary);

        // when
        final IncomeSummary actualIncomeSummary = hmrcResource.getHmrcData(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH, FROM_DATE, TO_DATE);

        // then
        // verify expected `IncomeSummary` is returned
        assertThat(actualIncomeSummary).isEqualTo(mockIncomeSummary);
        verifyZeroInteractions(mockIncomeSummary);
    }

    @Test
    public void shouldHandleOptionalToDate() {
        // given
        when(mockIncomeSummaryService.getIncomeSummary(isA(Individual.class), eq(FROM_DATE), isNull(LocalDate.class))).thenReturn(mockIncomeSummary);

        // when
        final IncomeSummary actualIncomeSummary = hmrcResource.getHmrcData(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH, FROM_DATE, null);

        // then
        // verify expected `IncomeSummary` is returned
        assertThat(actualIncomeSummary).isEqualTo(mockIncomeSummary);
        verifyZeroInteractions(mockIncomeSummary);
    }
}