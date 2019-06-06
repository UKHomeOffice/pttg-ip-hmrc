package uk.gov.digital.ho.pttg.application;

import org.aspectj.lang.JoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ResponseDurationAspectTest {

    private ResponseDurationAspect responseDurationAspect;

    @Mock private RequestHeaderData mockRequestHeaderData;

    @Before
    public void setup() {
        responseDurationAspect = new ResponseDurationAspect(mockRequestHeaderData);
    }

    @Test
    public void before_anyJoinPoint_shouldCheckIfTakingTooLong() {

        JoinPoint anyJoinPoint = mock(JoinPoint.class);

        responseDurationAspect.before(anyJoinPoint);

        then(mockRequestHeaderData)
                .should()
                .abortIfTakingTooLong();
    }
}
