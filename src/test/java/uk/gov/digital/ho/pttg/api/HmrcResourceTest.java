package uk.gov.digital.ho.pttg.api;

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
import uk.gov.digital.ho.pttg.application.NinoUtils;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HmrcResourceTest {

    @Mock
    private IncomeSummaryService incomeSummaryService;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    private HmrcResource hmrcResource;

    @Before
    public void setup() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(HmrcResource.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);

        hmrcResource = new HmrcResource(incomeSummaryService, new NinoUtils());
        LocalDate date = LocalDate.of(2018, 7, 30);
        hmrcResource.getHmrcData("John", "Smith", "PP300000A", date, date, date);
    }

    @Test
    public void shouldLogWhenRequestReceived() {
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Hmrc service invoked for nino PP300**** with date range 2018-07-30 to 2018-07-30") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[3]).getFieldName().equals("event_id");
        }));
    }

    @Test
    public void shouldLogResponseSuccess() {
        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Income summary successfully retrieved from HMRC") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[0]).getFieldName().equals("event_id");
        }));
    }

}
