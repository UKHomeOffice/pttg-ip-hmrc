package uk.gov.digital.ho.pttg;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import uk.gov.digital.ho.pttg.api.HmrcResource;
import uk.gov.digital.ho.pttg.api.IncomeSummaryService;
import uk.gov.digital.ho.pttg.application.NinoUtils;
import uk.gov.digital.ho.pttg.dto.IncomeSummary;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HmrcResourceTest {
    private static final String NINO = "QQ123456C";
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
    @Mock
    private Appender<ILoggingEvent> mockAppender;
    @InjectMocks
    private HmrcResource hmrcResource;

    @Before
    public void setup() {
        ;
    }

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

    @Test
    public void shouldOnlyLogRedactedNino() {
        // given
        final String redactedNino = "QQ1***56C";
        when(mockNinoUtils.redact(NINO)).thenReturn(redactedNino);

        final Logger root = (Logger) LoggerFactory.getLogger(HmrcResource.class);
        root.addAppender(mockAppender);

        // when
        hmrcResource.getHmrcData(FIRST_NAME, LAST_NAME, NINO, DATE_OF_BIRTH, FROM_DATE, TO_DATE);

        // then
        final ArgumentCaptor<ILoggingEvent> logCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(mockAppender).doAppend(logCaptor.capture());

        // verify formatted log message only contains redacted nino
        final ILoggingEvent logEvent = logCaptor.getValue();
        final String formattedLogMessage = logEvent.getFormattedMessage();
        assertThat(formattedLogMessage).contains(redactedNino);
        assertThat(formattedLogMessage).doesNotContain(NINO);
    }
}