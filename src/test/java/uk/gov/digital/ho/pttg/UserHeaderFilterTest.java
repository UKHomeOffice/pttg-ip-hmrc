package uk.gov.digital.ho.pttg;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MDC;
import uk.gov.digital.ho.pttg.api.UserHeaderFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.pttg.api.UserHeaderFilter.USER_ID_HEADER;


@RunWith(MockitoJUnitRunner.class)
public class UserHeaderFilterTest {

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private ServletResponse mockServletResponse;

    @InjectMocks
    private UserHeaderFilter userHeaderFilter;

    @Before
    public void setup() {
        MDC.clear();
    }


    @Test
    public void shouldAddToSuppliedDataToMdcBeforeProceeding() throws IOException, ServletException {
        when(mockHttpServletRequest.getHeader(USER_ID_HEADER)).thenReturn("supplied data should go in the MDC");

        assertThat(MDC.get(USER_ID_HEADER))
                .as("MDC needs to be empty before the test")
                .isEqualTo(null);

        FilterChain stubFilterChain = (request, response) -> assertThat(MDC.get(USER_ID_HEADER)).isEqualTo("supplied data should go in the MDC");

        userHeaderFilter.doFilter(mockHttpServletRequest, mockServletResponse, stubFilterChain);

        assertThat(MDC.get(USER_ID_HEADER))
                .as("MDC should be empty after the test")
                .isEqualTo(null);
    }

}