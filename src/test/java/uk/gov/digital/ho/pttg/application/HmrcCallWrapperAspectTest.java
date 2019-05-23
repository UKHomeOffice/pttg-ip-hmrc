package uk.gov.digital.ho.pttg.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;
import uk.gov.digital.ho.pttg.application.util.TraversonFollower;

import java.net.URI;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HmrcCallWrapperAspectTest {

    @MockBean RequestHeaderData mockRequestHeaderData;
    @MockBean(name = "hmrcApiRestTemplate") RestTemplate mockRestTemplate;
    @MockBean TraversonFollower mockTraversonFollower;

    @Autowired HmrcCallWrapper hmrcCallWrapper;

    private ParameterizedTypeReference anyReference = ParameterizedTypeReference.forType(null);

    @Test
    public void exchange_aspectApplied() {

        URI anyUri = URI.create("anyUrl");
        HttpMethod anyHttpMethod = HttpMethod.GET;
        HttpEntity anyHttpEntity = HttpEntity.EMPTY;

        given(mockRestTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), any(ParameterizedTypeReference.class)))
                .willThrow(UnsupportedOperationException.class);

        try {
            hmrcCallWrapper.exchange(anyUri, anyHttpMethod, anyHttpEntity, anyReference);
            fail("expected action to throw, but it did not!");
        } catch (UnsupportedOperationException e) {
            // This method is supposed to swallow this exception.
        }

        then(mockRequestHeaderData)
                .should()
                .abortIfTakingTooLong();
    }

    @Test
    public void followTraverson_aspectApplied() {

        String anyLink = "any Link";
        String anyAccessToken = "any access token";

        given(mockTraversonFollower.followTraverson(anyString(), anyString(), eq(mockRestTemplate), any(ParameterizedTypeReference.class)))
                .willThrow(UnsupportedOperationException.class);

        try {
            hmrcCallWrapper.followTraverson(anyLink, anyAccessToken, anyReference);
            fail("expected action to throw, but it did not!");
        } catch (UnsupportedOperationException e) {
            // This method is supposed to swallow this exception.
        }

        then(mockRequestHeaderData)
                .should()
                .abortIfTakingTooLong();
    }

}
