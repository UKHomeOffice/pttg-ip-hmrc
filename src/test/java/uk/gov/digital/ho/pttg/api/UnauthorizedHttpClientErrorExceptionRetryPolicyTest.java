package uk.gov.digital.ho.pttg.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryContext;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UnauthorizedHttpClientErrorExceptionRetryPolicyTest {

    private static final int MAX_ATTEMPTS = 2;

    @Mock
    private RetryContext mockRetryContext;

    private UnauthorizedHttpClientErrorExceptionRetryPolicy unauthorizedHttpClientErrorExceptionRetryPolicy;

    @Before
    public void setUp() {
        unauthorizedHttpClientErrorExceptionRetryPolicy = new UnauthorizedHttpClientErrorExceptionRetryPolicy(MAX_ATTEMPTS);
    }

    @Test
    public void canRetryWhenAttemptsRemaining() {
        // given
        when(mockRetryContext.getRetryCount()).thenReturn(0);
        when(mockRetryContext.getLastThrowable()).thenReturn(null);

        // when
        final boolean canRetry = unauthorizedHttpClientErrorExceptionRetryPolicy.canRetry(mockRetryContext);

        // then
        assertThat(canRetry).isTrue();
    }

    @Test
    public void canRetryWhenUnauthorizedHttpClientErrorExceptionThrown() {
        // given
        when(mockRetryContext.getRetryCount()).thenReturn(0);
        when(mockRetryContext.getLastThrowable()).thenReturn(new ApplicationExceptions.HmrcUnauthorisedException("test"));

        // when
        final boolean canRetry = unauthorizedHttpClientErrorExceptionRetryPolicy.canRetry(mockRetryContext);

        // then
        assertThat(canRetry).isTrue();
    }

    @Test
    public void cannotRetryOtherHttpClientErrorExceptionThrown() {
        // given
        when(mockRetryContext.getRetryCount()).thenReturn(0);
        when(mockRetryContext.getLastThrowable()).thenReturn(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // when
        final boolean canRetry = unauthorizedHttpClientErrorExceptionRetryPolicy.canRetry(mockRetryContext);

        // then
        assertThat(canRetry).isFalse();
    }

    @Test
    public void cannotRetryWhenNoAttemptsRemaining() {
        // given
        when(mockRetryContext.getRetryCount()).thenReturn(MAX_ATTEMPTS + 1);
        when(mockRetryContext.getLastThrowable()).thenReturn(null);

        // when
        final boolean canRetry = unauthorizedHttpClientErrorExceptionRetryPolicy.canRetry(mockRetryContext);

        // then
        assertThat(canRetry).isFalse();
    }

    @Test
    public void cannotRetryWhenOtherExceptionThrown() {
        // given
        when(mockRetryContext.getRetryCount()).thenReturn(0);
        when(mockRetryContext.getLastThrowable()).thenReturn(new IllegalArgumentException());

        // when
        final boolean canRetry = unauthorizedHttpClientErrorExceptionRetryPolicy.canRetry(mockRetryContext);

        // then
        assertThat(canRetry).isFalse();
    }
}