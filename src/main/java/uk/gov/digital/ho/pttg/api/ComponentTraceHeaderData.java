package uk.gov.digital.ho.pttg.api;

import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static uk.gov.digital.ho.pttg.api.RequestHeaderData.COMPONENT_NAME;

@Component
public class ComponentTraceHeaderData {

    static final String COMPONENT_TRACE_HEADER = "x-component-trace";

    public void initialiseComponentTrace(HttpServletRequest request) {
        String initialComponentTraceHeader = request.getHeader(COMPONENT_TRACE_HEADER);
        String newComponentTraceHeader = initialComponentTraceHeader == null ? COMPONENT_NAME : initialComponentTraceHeader + "," + COMPONENT_NAME;
        MDC.put(COMPONENT_TRACE_HEADER, newComponentTraceHeader);
    }

    public String componentTrace() {
        return MDC.get(COMPONENT_TRACE_HEADER);
    }

    public void addComponentTraceHeader(HttpHeaders headers) {
        headers.add(COMPONENT_TRACE_HEADER, componentTrace());
    }
}
