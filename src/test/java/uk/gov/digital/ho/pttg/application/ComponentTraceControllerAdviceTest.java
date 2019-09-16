package uk.gov.digital.ho.pttg.application;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import uk.gov.digital.ho.pttg.api.ComponentTraceHeaderData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ComponentTraceControllerAdviceTest {

    private static final MethodParameter ANY_RETURN_TYPE = mock(MethodParameter.class);
    private static final Class ANY_CONVERTER_TYPE = ComponentTraceControllerAdvice.class;
    private static final MediaType ANY_MEDIA_TYPE = MediaType.APPLICATION_JSON;

    @Mock
    private ServerHttpRequest mockRequest;
    @Mock
    private ServerHttpResponse mockResponse;
    @Mock
    private ComponentTraceHeaderData mockComponentTraceHeaderData;

    private ComponentTraceControllerAdvice controllerAdvice;

    @Before
    public void setUp() {
        controllerAdvice = new ComponentTraceControllerAdvice(mockComponentTraceHeaderData);
    }

    @Test
    public void supports_anyInput_true() {
        assertThat(controllerAdvice.supports(ANY_RETURN_TYPE, ANY_CONVERTER_TYPE)).isTrue();
    }

    @Test
    public void beforeBodyWrite_someBody_returnBody() {
        Object expectedBody = "some body";
        Object actualBody = controllerAdvice.beforeBodyWrite(expectedBody, ANY_RETURN_TYPE, ANY_MEDIA_TYPE, ANY_CONVERTER_TYPE, mockRequest, mockResponse);

        assertThat(actualBody).isEqualTo(expectedBody);
    }

    @Test
    public void beforeBodyWrite_anyResponse_addHeader() {
        HttpHeaders someHeaders = new HttpHeaders();
        someHeaders.add("any key", "any value");
        given(mockResponse.getHeaders()).willReturn(someHeaders);

        controllerAdvice.beforeBodyWrite("any body", ANY_RETURN_TYPE, ANY_MEDIA_TYPE, ANY_CONVERTER_TYPE, mockRequest, mockResponse);

        then(mockComponentTraceHeaderData).should().addComponentTraceHeader(someHeaders);
    }
}
