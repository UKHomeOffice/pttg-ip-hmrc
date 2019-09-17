package uk.gov.digital.ho.pttg.api;

import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ComponentTraceHeaderData implements HandlerInterceptor {

    static final String COMPONENT_TRACE_HEADER = "x-component-trace";
    private static final String COMPONENT_NAME = "pttg-ip-hmrc";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String initialComponentTraceHeader = request.getHeader(COMPONENT_TRACE_HEADER);
        String newComponentTraceHeader = initialComponentTraceHeader == null ? COMPONENT_NAME : initialComponentTraceHeader + "," + COMPONENT_NAME;
        MDC.put(COMPONENT_TRACE_HEADER, newComponentTraceHeader);
        return true;
    }

    public void addComponentTraceHeader(HttpHeaders headers) {
        headers.add(COMPONENT_TRACE_HEADER, componentTrace());
    }

    String componentTrace() {
        return MDC.get(COMPONENT_TRACE_HEADER);
    }
}
