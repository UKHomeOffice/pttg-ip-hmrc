package uk.gov.digital.ho.pttg.application;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ProxyCustomizerTest {

    private static final String LOG_TEST_APPENDER = "tester";

    @Mock RestTemplate template;

    @Mock
    private Appender<ILoggingEvent> mockAppender;
    private ProxyCustomizer customizer;

    @Before
    public void setup() {

        mockAppender.setName(LOG_TEST_APPENDER);

        Logger logger = (Logger) LoggerFactory.getLogger(ProxyCustomizer.class);
        logger.setLevel(Level.INFO);
        logger.addAppender(mockAppender);

        customizer = new ProxyCustomizer(
                "http://test.hmrc.gov.uk",
                "a.proxy.server",
                1234);
    }

    @After
    public void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(ProxyCustomizer.class);
        logger.detachAppender(LOG_TEST_APPENDER);
    }

    @Test
    public void shouldSetRequestFactory() {
        customizer.customize(template);
        verify(template).setRequestFactory(any(HttpComponentsClientHttpRequestFactory.class));
    }

    @Test
    public void shouldLogMessageWhenCustomizeCalled() {
        customizer.customize(template);

        verify(mockAppender).doAppend(argThat(argument -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals("Using proxy [a.proxy.server]:[1234] for [test.hmrc.gov.uk]") &&
                    ((ObjectAppendingMarker) loggingEvent.getArgumentArray()[3]).getFieldName().equals("event_id");
        }));
    }
}
