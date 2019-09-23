package uk.gov.digital.ho.pttg.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.api.ComponentTraceHeaderData.COMPONENT_TRACE_HEADER;

@RunWith(MockitoJUnitRunner.class)
public class ComponentTraceHeaderDataTest {

    @Mock private HttpServletRequest mockHttpServletRequest;
    @Mock private HttpServletResponse mockHttpServletResponse;
    @Mock private Object mockHandler;
    @Mock private HttpHeaders mockHeaders;

    private ComponentTraceHeaderData componentTraceHeaderData;

    @Before
    public void setUp() {
        componentTraceHeaderData = new ComponentTraceHeaderData();
    }

    @Test
    public void preHandle_noComponentTraceHeader_create() {
        when(mockHttpServletRequest.getHeader("x-component-trace")).thenReturn(null);

        componentTraceHeaderData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(componentTraceHeaderData.componentTrace()).isEqualTo("pttg-ip-hmrc");
    }

    @Test
    public void preHandle_componentTraceHeader_append() {
        when(mockHttpServletRequest.getHeader("x-component-trace")).thenReturn("pttg-ip-api");

        componentTraceHeaderData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(componentTraceHeaderData.componentTrace()).isEqualTo("pttg-ip-api,pttg-ip-hmrc");
    }

    @Test
    public void preHandle_componentTraceHeaderMultipleComponents_append() {
        when(mockHttpServletRequest.getHeader("x-component-trace")).thenReturn("pttg-ip-api,pttg-ip-audit");

        componentTraceHeaderData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(componentTraceHeaderData.componentTrace()).isEqualTo("pttg-ip-api,pttg-ip-audit,pttg-ip-hmrc");
    }

    @Test
    public void addComponentTraceHeader_anyResponse_addsHeader() {
        String expectedComponentTrace = "some-component,some-other-component";
        MDC.put(COMPONENT_TRACE_HEADER, expectedComponentTrace);

        componentTraceHeaderData.addComponentTraceHeader(mockHeaders);

        then(mockHeaders).should().add(COMPONENT_TRACE_HEADER, expectedComponentTrace);
    }

    @Test
    public void updateComponentTrace_responseEntityNoTraceHeader_doNotUpdate() {
        String expectedComponentTrace = "pttg-ip-hmrc";
        MDC.put(COMPONENT_TRACE_HEADER, expectedComponentTrace);

        ResponseEntity responseWithoutTraceHeader = ResponseEntity.ok("");
        componentTraceHeaderData.updateComponentTrace(responseWithoutTraceHeader);

        assertThat(componentTraceHeaderData.componentTrace()).isEqualTo(expectedComponentTrace);
    }

    @Test
    public void updateComponentTrace_responseEntityNullTraceHeader_doNotUpdate() {
        String expectedComponentTrace = "pttg-ip-hmrc";
        MDC.put(COMPONENT_TRACE_HEADER, expectedComponentTrace);

        ResponseEntity responseWithoutNullHeader = new ResponseEntity(componentTraceHeader(null), HttpStatus.OK);
        componentTraceHeaderData.updateComponentTrace(responseWithoutNullHeader);

        assertThat(componentTraceHeaderData.componentTrace()).isEqualTo(expectedComponentTrace);
    }

    @Test
    public void updateComponentTrace_responseEntityEmptyTraceHeader_doNotUpdate() {
        String expectedComponentTrace = "pttg-ip-hmrc";
        MDC.put(COMPONENT_TRACE_HEADER, expectedComponentTrace);

        ResponseEntity responseWithoutNullHeader = new ResponseEntity(componentTraceHeader(""), HttpStatus.OK);
        componentTraceHeaderData.updateComponentTrace(responseWithoutNullHeader);

        assertThat(componentTraceHeaderData.componentTrace()).isEqualTo(expectedComponentTrace);
    }

    @Test
    public void updateComponentTrace_responseEntityTraceHeader_update() {
        String initialTrace = "pttg-ip-hmrc";
        MDC.put(COMPONENT_TRACE_HEADER, initialTrace);

        String expectedTrace = "pttg-ip-hmrc,pttg-ip-audit";
        ResponseEntity responseWithTraceHeader = new ResponseEntity(componentTraceHeader(expectedTrace), HttpStatus.OK);
        componentTraceHeaderData.updateComponentTrace(responseWithTraceHeader);

        assertThat(componentTraceHeaderData.componentTrace()).isEqualTo(expectedTrace);
    }

    @Test
    public void updateComponentTrace_multipleResponseEntityCalls_lastWins() {
        String firstTrace = "pttg-ip-hmrc";
        ResponseEntity firstResponse = new ResponseEntity(componentTraceHeader(firstTrace), HttpStatus.OK);
        componentTraceHeaderData.updateComponentTrace(firstResponse);

        String expectedTrace = "pttg-ip-hmrc,pttg-ip-audit";
        ResponseEntity winningResponse = new ResponseEntity(componentTraceHeader(expectedTrace), HttpStatus.OK);
        componentTraceHeaderData.updateComponentTrace(winningResponse);

        assertThat(componentTraceHeaderData.componentTrace()).isEqualTo(expectedTrace);
    }

    @Test
    public void updateComponentTrace_exceptionNoHeaders_doNotUpdate() {
        String expectedTrace = "pttg-ip-hmrc";
        MDC.put(COMPONENT_TRACE_HEADER, expectedTrace);

        HttpStatusCodeException httpException = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        componentTraceHeaderData.updateComponentTrace(httpException);

        assertThat(componentTraceHeaderData.componentTrace()).isEqualTo(expectedTrace);
    }

    @Test
    public void updateComponentTrace_exceptionNullTraceHeader_doNotUpdate() {
        String expectedTrace = "pttg-ip-hmrc";
        MDC.put(COMPONENT_TRACE_HEADER, expectedTrace);

        HttpStatusCodeException httpExceptionWithNullTrace = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "any text", componentTraceHeader(null), null, null);
        componentTraceHeaderData.updateComponentTrace(httpExceptionWithNullTrace);

        assertThat(componentTraceHeaderData.componentTrace()).isEqualTo(expectedTrace);
    }

    @Test
    public void updateComponentTrace_exceptionEmptyTraceHeader_doNotUpdate() {
        String expectedTrace = "pttg-ip-hmrc";
        MDC.put(COMPONENT_TRACE_HEADER, expectedTrace);

        HttpStatusCodeException httpExceptionWithEmptyTrace = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "any text", componentTraceHeader(""), null, null);
        componentTraceHeaderData.updateComponentTrace(httpExceptionWithEmptyTrace);

        assertThat(componentTraceHeaderData.componentTrace()).isEqualTo(expectedTrace);
    }

    @Test
    public void updateComponentTrace_exceptionOtherHeaders_doNotUpdate() {
        String expectedTrace = "pttg-ip-hmrc";
        MDC.put(COMPONENT_TRACE_HEADER, expectedTrace);

        HttpHeaders otherHeaders = new HttpHeaders();
        otherHeaders.put("some other header", Collections.singletonList("some other value"));
        HttpStatusCodeException httpExceptionWithEmptyTrace = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "any text", otherHeaders, null, null);
        componentTraceHeaderData.updateComponentTrace(httpExceptionWithEmptyTrace);

        assertThat(componentTraceHeaderData.componentTrace()).isEqualTo(expectedTrace);
    }

    @Test
    public void updateComponentTrace_exceptionWithTrace_update() {
        String initialTrace = "pttg-ip-hmrc";
        MDC.put(COMPONENT_TRACE_HEADER, initialTrace);

        String expectedTrace = "pttg-ip-hmrc,pttg-ip-api";
        HttpStatusCodeException httpExceptionWithTrace = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "any text", componentTraceHeader(expectedTrace), null, null);
        componentTraceHeaderData.updateComponentTrace(httpExceptionWithTrace);

        assertThat(componentTraceHeaderData.componentTrace()).isEqualTo(expectedTrace);
    }

    @Test
    public void updateComponentTrace_multipleExceptionsWithTrace_lastWins() {
        String initialTrace = "pttg-ip-hmrc";
        HttpStatusCodeException initialException = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "any text", componentTraceHeader(initialTrace), null, null);
        componentTraceHeaderData.updateComponentTrace(initialException);


        String expectedTrace = "pttg-ip-hmrc,pttg-ip-api";
        HttpStatusCodeException winningException = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "any text", componentTraceHeader(expectedTrace), null, null);
        componentTraceHeaderData.updateComponentTrace(winningException);

        assertThat(componentTraceHeaderData.componentTrace()).isEqualTo(expectedTrace);
    }

    @Test
    public void appendComponentToTrace_someComponent_append() {
        MDC.put(COMPONENT_TRACE_HEADER, "pttg-ip-api,pttg-ip-hmrc");

        componentTraceHeaderData.appendComponentToTrace("HMRC");

        assertThat(componentTraceHeaderData.componentTrace()).isEqualTo("pttg-ip-api,pttg-ip-hmrc,HMRC");
    }

    private HttpHeaders componentTraceHeader(String components) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(COMPONENT_TRACE_HEADER, components);
        return headers;
    }
}