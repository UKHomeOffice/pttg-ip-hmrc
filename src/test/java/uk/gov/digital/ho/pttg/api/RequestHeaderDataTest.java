package uk.gov.digital.ho.pttg.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.InsufficientTimeException;
import uk.gov.digital.ho.pttg.application.LogEvent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static uk.gov.digital.ho.pttg.Failable.when_ExceptionThrownBy;
import static uk.gov.digital.ho.pttg.api.RequestHeaderData.EXPECTED_REMAINING_TIME_TO_COMPLETE;
import static uk.gov.digital.ho.pttg.api.RequestHeaderData.MAX_DURATION_MS_HEADER;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_INSUFFICIENT_TIME_TO_COMPLETE;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_SERVICE_GENERATED_CORRELATION_ID;

@RunWith(MockitoJUnitRunner.class)
public class RequestHeaderDataTest {

    @Mock private HttpServletRequest mockHttpServletRequest;
    @Mock private HttpServletResponse mockHttpServletResponse;
    @Mock private Object mockHandler;
    @Mock private Appender<ILoggingEvent> mockAppender;

    private RequestHeaderData requestData;

    @Before
    public void setup() {
        Clock testClock =  Clock.fixed(Instant.ofEpochMilli(2222), ZoneId.of("Z"));
        requestData = new RequestHeaderData(testClock);
        ReflectionTestUtils.setField(requestData, "hmrcAccessBasicAuth", "user:password");

        Logger rootLogger = (Logger) LoggerFactory.getLogger(RequestHeaderData.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
    }

    @Test
    public void shouldUseSystemClockByDefault() {
        Clock systemClock = Clock.systemUTC();

        RequestHeaderData requestHeaderData = new RequestHeaderData();

        Clock clock = (Clock) ReflectionTestUtils.getField(requestHeaderData, "clock");

        assertThat(clock).isEqualTo(systemClock);
    }

    @Test
    public void shouldProduceBasicAuthHeaderValue() {
        assertThat(requestData.hmrcBasicAuth()).isEqualTo("Basic dXNlcjpwYXNzd29yZA==");
    }

    @Test
    public void shouldDefaultRequestData() {
        given_requestDataPrehandleCalled();

        assertThat(requestData.deploymentName()).isNull();
        assertThat(requestData.deploymentNamespace()).isNull();
        assertThat(requestData.sessionId()).isEqualTo("unknown");
        assertThat(requestData.correlationId()).isNotNull();
        assertThat(requestData.userId()).isEqualTo("unknown");
    }

    @Test
    public void shouldUseSessionIdFromRequest() {
        given(mockHttpServletRequest.getHeader("x-session-id"))
                .willReturn("some session id");

        given_requestDataPrehandleCalled();

        assertThat(requestData.sessionId()).isEqualTo("some session id");
    }

    @Test
    public void shouldUseCorrelationIdFromRequest() {
        given(mockHttpServletRequest.getHeader("x-correlation-id"))
                .willReturn("some correlation id");

        given_requestDataPrehandleCalled();

        assertThat(requestData.correlationId()).isEqualTo("some correlation id");
    }

    @Test
    public void shouldUseUserIdFromRequest() {
        given(mockHttpServletRequest.getHeader("x-auth-userid"))
                .willReturn("some user id");

        given_requestDataPrehandleCalled();

        assertThat(requestData.userId()).isEqualTo("some user id");
    }

    @Test
    public void shouldDefaultMaxDurationWhenNotSuppliedInRequest() {
        given(mockHttpServletRequest.getHeader("x-max-duration"))
                .willReturn(null);

        given_requestDataPrehandleCalled();

        // This is not a Spring test, so the default value is zero
        assertThat(MDC.get("max_duration")).isEqualTo("0");
    }

    @Test
    public void shouldThrowExceptionWhenNonIntegerMaxDuration() {
        given(mockHttpServletRequest.getHeader("x-max-duration"))
                .willReturn("a76543");

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));

        assertThat(MDC.get("max_duration")).isEqualTo(null);
    }

    @Test
    public void shouldUseMaxDurationFromRequest() {
        given(mockHttpServletRequest.getHeader("x-max-duration"))
                .willReturn("76543");

        given_requestDataPrehandleCalled();

        assertThat(MDC.get("max_duration")).isEqualTo("76543");
    }

    @Test
    public void shouldLogWhenCorrelationIdGenerated() {
        given(mockHttpServletRequest.getHeader("x-correlation-id"))
                .willReturn(null);

        given_requestDataPrehandleCalled();

        then(mockAppender)
                .should()
                .doAppend(argThat(isALogMessageWith(
                        "Generated new correlation id as not passed in request header",
                        0,
                        HMRC_SERVICE_GENERATED_CORRELATION_ID)));
    }

    @Test
    public void shouldAddRequestTimeStampToMDC() {
        given_requestDataPrehandleCalled();
        assertThat(MDC.get("request-timestamp")).isNotNull();
    }

    @Test
    public void shouldReturnRequestDuration() {
        given_requestDataPrehandleCalled();
        assertThat(requestData.calculateRequestDuration()).isNotNegative();
    }

    @Test
    public void shouldReturnPoolSize() {
        given_requestDataPrehandleCalled();
        assertThat(requestData.poolSize()).isNotNegative();
    }

    @Test
    public void shouldCalculateTimeOfResponseRequiredBy() {

        given(mockHttpServletRequest.getHeader(MAX_DURATION_MS_HEADER))
                .willReturn("777");

        given_requestDataPrehandleCalled();

        long responseRequiredBy = requestData.responseRequiredBy();

        assertThat(responseRequiredBy).isEqualTo(2999);
    }

    @Test
    public void proceed_whenRequestDurationAtThreshold_noException() {

        given(mockHttpServletRequest.getHeader("x-max-duration"))
                .willReturn(Long.toString(EXPECTED_REMAINING_TIME_TO_COMPLETE));

        given_requestDataPrehandleCalled();

        assertThatCode(() -> requestData.abortIfTakingTooLong())
                .doesNotThrowAnyException();
    }

    @Test
    public void proceed_whenRequestDurationBeyondThreshold_exceptionThrown() {

        given(mockHttpServletRequest.getHeader("x-max-duration"))
                .willReturn(Long.toString(EXPECTED_REMAINING_TIME_TO_COMPLETE - 1));

        given_requestDataPrehandleCalled();

        assertThatExceptionOfType(InsufficientTimeException.class)
            .isThrownBy(() -> requestData.abortIfTakingTooLong());
    }

    @Test
    public void proceed_whenRequestDurationBeyondThreshold_log() {

        given(mockHttpServletRequest.getHeader("x-max-duration"))
                .willReturn(Long.toString(EXPECTED_REMAINING_TIME_TO_COMPLETE - 1));

        given_requestDataPrehandleCalled();

        when_ExceptionThrownBy(() -> requestData.abortIfTakingTooLong());

        then(mockAppender)
                .should()
                .doAppend(argThat(isALogMessageWith(
                        "Insufficient time to complete the Response - -1 ms remaining and expected duration is 0",
                        2,
                        HMRC_INSUFFICIENT_TIME_TO_COMPLETE)));
    }

    private void given_requestDataPrehandleCalled() {
        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);
    }

    @NotNull
    private ArgumentMatcher<ILoggingEvent> isALogMessageWith(String message, int eventArgIndex, LogEvent event) {
        return (argument) -> {
            LoggingEvent loggingEvent = (LoggingEvent) argument;

            return loggingEvent.getFormattedMessage().equals(message) &&
                           loggingEvent.getArgumentArray()[eventArgIndex].equals(new ObjectAppendingMarker("event_id", event));
        };
    }

}
