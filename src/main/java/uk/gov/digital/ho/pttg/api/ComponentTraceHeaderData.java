package uk.gov.digital.ho.pttg.api;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

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

    public void updateComponentTrace(ResponseEntity responseEntity) {
        List<String> componentTraceHeaders = responseEntity.getHeaders().get(COMPONENT_TRACE_HEADER);
        if (componentTraceHeaders == null || componentTraceHeaders.isEmpty()) {
            return;
        }

        setComponentTrace(componentTraceHeaders);
    }

    public void updateComponentTrace(HttpStatusCodeException httpException) {
        HttpHeaders headers = httpException.getResponseHeaders();
        if (headers == null) {
            return;
        }

        List<String> componentTraceHeaders = headers.get(COMPONENT_TRACE_HEADER);
        setComponentTrace(componentTraceHeaders);
    }

    public void addComponentTraceHeader(HttpHeaders headers) {
        headers.add(COMPONENT_TRACE_HEADER, componentTrace());
    }

    String componentTrace() {
        return MDC.get(COMPONENT_TRACE_HEADER);
    }

    private void setComponentTrace(List<String> componentTraceHeaders) {
        if (componentTraceHeaders == null) {
            return;
        }

        List<String> components = removeEmptyHeaders(componentTraceHeaders);
        if (!components.isEmpty()) {
            MDC.put(COMPONENT_TRACE_HEADER, String.join(",", components));
        }
    }

    private List<String> removeEmptyHeaders(List<String> componentTraceHeaders) {
        return componentTraceHeaders.stream()
                                    .filter(StringUtils::isNotEmpty)
                                    .collect(Collectors.toList());
    }
}
