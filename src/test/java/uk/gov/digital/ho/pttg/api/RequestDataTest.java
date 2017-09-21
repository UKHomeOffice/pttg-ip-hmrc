package uk.gov.digital.ho.pttg.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestDataTest {

    @Mock private HttpServletRequest mockHttpServletRequest;
    @Mock private HttpServletResponse mockHttpServletResponse;
    @Mock private Object mockHandler;

    private RequestData requestData;

    @Before
    public void setup() {
        requestData = new RequestData();
    }

    @Test
    public void shouldDefaultRequestData() throws Exception {
        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.deploymentName()).isNull();
        assertThat(requestData.deploymentNamespace()).isNull();
        assertThat(requestData.sessionId()).isEqualTo("unknown");
        assertThat(requestData.correlationId()).isNotNull();
        assertThat(requestData.userId()).isEqualTo("anonymous");
    }

    @Test
    public void shouldUseSessionIdFromRequest() throws Exception {
        HttpSession mockSessison = mock(HttpSession.class);
        when(mockSessison.getId()).thenReturn("some session id");
        when(mockHttpServletRequest.getSession(false)).thenReturn(mockSessison);

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.sessionId()).isEqualTo("some session id");
    }

    @Test
    public void shouldUseCorrelationIdFromRequest() throws Exception {
        when(mockHttpServletRequest.getHeader("x-correlation-id")).thenReturn("some correlation id");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.correlationId()).isEqualTo("some correlation id");
    }

    @Test
    public void shouldUseUserIdFromRequest() throws Exception {
        when(mockHttpServletRequest.getHeader("x-auth-userid")).thenReturn("some user id");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.userId()).isEqualTo("some user id");
    }
}