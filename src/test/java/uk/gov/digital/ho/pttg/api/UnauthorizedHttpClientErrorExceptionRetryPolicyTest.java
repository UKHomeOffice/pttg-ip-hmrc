package uk.gov.digital.ho.pttg.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.retry.RetryContext;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.pttg.application.retry.UnauthorizedHttpClientErrorExceptionRetryPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcUnauthorisedException;

@RunWith(MockitoJUnitRunner.class)
public class UnauthorizedHttpClientErrorExceptionRetryPolicyTest {

    private static final int MAX_ATTEMPTS = 2;

    @Mock private RetryContext mockRetryContext;

    private UnauthorizedHttpClientErrorExceptionRetryPolicy unauthorizedHttpClientErrorExceptionRetryPolicy;

    @Before
    public void setUp() {
        unauthorizedHttpClientErrorExceptionRetryPolicy = new UnauthorizedHttpClientErrorExceptionRetryPolicy(MAX_ATTEMPTS);
    }

    @Test
    public void canRetryWhenAttemptsRemaining() {
        when(mockRetryContext.getRetryCount()).thenReturn(0);
        when(mockRetryContext.getLastThrowable()).thenReturn(null);

        boolean canRetry = unauthorizedHttpClientErrorExceptionRetryPolicy.canRetry(mockRetryContext);

        assertThat(canRetry).isTrue();
    }

    @Test
    public void canRetryWhenUnauthorizedHttpClientErrorExceptionThrown() {
        when(mockRetryContext.getRetryCount()).thenReturn(0);
        when(mockRetryContext.getLastThrowable()).thenReturn(new HmrcUnauthorisedException("test"));

        boolean canRetry = unauthorizedHttpClientErrorExceptionRetryPolicy.canRetry(mockRetryContext);

        assertThat(canRetry).isTrue();
    }

    @Test
    public void cannotRetryOtherHttpClientErrorExceptionThrown() {
        when(mockRetryContext.getLastThrowable()).thenReturn(new HttpClientErrorException(BAD_REQUEST));

        boolean canRetry = unauthorizedHttpClientErrorExceptionRetryPolicy.canRetry(mockRetryContext);

        assertThat(canRetry).isFalse();
    }

    @Test
    public void cannotRetryWhenNoAttemptsRemaining() {
        when(mockRetryContext.getRetryCount()).thenReturn(MAX_ATTEMPTS + 1);
        when(mockRetryContext.getLastThrowable()).thenReturn(null);

        boolean canRetry = unauthorizedHttpClientErrorExceptionRetryPolicy.canRetry(mockRetryContext);

        assertThat(canRetry).isFalse();
    }

    @Test
    public void cannotRetryWhenOtherExceptionThrown() {
        when(mockRetryContext.getLastThrowable()).thenReturn(new IllegalArgumentException());

        boolean canRetry = unauthorizedHttpClientErrorExceptionRetryPolicy.canRetry(mockRetryContext);

        assertThat(canRetry).isFalse();
    }
}