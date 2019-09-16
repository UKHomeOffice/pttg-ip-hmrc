package uk.gov.digital.ho.pttg.application;

import lombok.AllArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import uk.gov.digital.ho.pttg.api.ComponentTraceHeaderData;

@ControllerAdvice
@AllArgsConstructor
public class ComponentTraceControllerAdvice implements ResponseBodyAdvice {

    private final ComponentTraceHeaderData componentTraceHeaderData;

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        componentTraceHeaderData.addComponentTraceHeader(response.getHeaders());
        return body;
    }
}
