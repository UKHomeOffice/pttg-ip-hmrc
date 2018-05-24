package uk.gov.digital.ho.pttg.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryContext;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NameMatchingRetryPolicyTest {

    @Mock
    private RetryContext mockRetryContext;

    @InjectMocks
    private NameMatchingRetryPolicy nameMatchingRetryPolicy;

    @Test
    public void retriesWithHandledForbiddenException() {
        when(mockRetryContext.getLastThrowable()).thenReturn(new ApplicationExceptions.HmrcForbiddenException("test"));

        boolean retry = nameMatchingRetryPolicy.canRetry(mockRetryContext);

        verify(mockRetryContext).getLastThrowable();
        assertTrue(retry);

    }

    @Test
    public void noRetriesWithUnhandledForbiddenStatus() {
        when(mockRetryContext.getLastThrowable()).thenReturn(new HttpClientErrorException(HttpStatus.FORBIDDEN, "test"));

        boolean retry = nameMatchingRetryPolicy.canRetry(mockRetryContext);

        verify(mockRetryContext).getLastThrowable();
        assertFalse(retry);
    }

}
