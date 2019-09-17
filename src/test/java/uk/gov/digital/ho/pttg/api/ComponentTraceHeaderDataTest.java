package uk.gov.digital.ho.pttg.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
}