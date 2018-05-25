package uk.gov.digital.ho.pttg.application.retry;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryContext;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NameMatchingRetryPolicyTest {

    private NameMatchingRetryPolicy nameMatchingRetryPolicy;

    @Mock
    private RetryContext mockRetryContext;

    private Individual individual;
    private List<String> candidateNames;

    @Before
    public void setUp() {
        candidateNames = ImmutableList.of("Arthur Bobbins");
        individual = new Individual("Arthur", "Bobbins", "nino", LocalDate.now());
        nameMatchingRetryPolicy = new NameMatchingRetryPolicy(candidateNames, individual);
    }

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

    @Test
    public void retriesForEachCandidateName() {
        when(mockRetryContext.getLastThrowable()).thenReturn(new ApplicationExceptions.HmrcForbiddenException("test"));
        when(mockRetryContext.getRetryCount()).thenReturn(0).thenReturn(1).thenReturn(2);

        candidateNames = ImmutableList.of("Arthur Bobbins", "Bobbins Arthur");
        individual = new Individual("Arthur", "Bobbins", "nino", LocalDate.now());
        nameMatchingRetryPolicy = new NameMatchingRetryPolicy(candidateNames, individual);

        boolean retry = nameMatchingRetryPolicy.canRetry(mockRetryContext);
        assertThat("The first attempt can retry", retry, is(true));
        assertThat("The individual first name is correct", individual.getFirstName(), is("Arthur"));
        assertThat("The individual last name is correct", individual.getLastName(), is("Bobbins"));

        retry = nameMatchingRetryPolicy.canRetry(mockRetryContext);
        assertThat("The second attempt can retry", retry, is(true));
        assertThat("The individual first name is correct", individual.getFirstName(), is("Bobbins"));
        assertThat("The individual last name is correct", individual.getLastName(), is("Arthur"));

        retry = nameMatchingRetryPolicy.canRetry(mockRetryContext);
        assertThat("The third attempt cannot retry", retry, is(false));

    }

}
