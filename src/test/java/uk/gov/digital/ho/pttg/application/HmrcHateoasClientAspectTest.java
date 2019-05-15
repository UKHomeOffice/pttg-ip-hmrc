package uk.gov.digital.ho.pttg.application;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.Link;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;
import uk.gov.digital.ho.pttg.application.domain.Individual;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.NameMatchingCandidatesService;
import uk.gov.digital.ho.pttg.application.util.namenormalizer.NameNormalizer;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class HmrcHateoasClientAspectTest {

    @MockBean RequestHeaderData mockRequestHeaderData;
    @MockBean HmrcCallWrapper mockHmrcCallWrapper;
    @MockBean NameMatchingCandidatesService mockNameMatchingCandidatesService;
    @MockBean NameNormalizer mockNameNormalizer;

    @Autowired HmrcHateoasClient hmrcHateoasClient;

    private LocalDate anyFromDate;
    private LocalDate anyToDate;
    private String anyAccessToken;
    private String anyString;
    private Link anyLink;

    @Before
    public void setup() {
        anyFromDate = LocalDate.MAX;
        anyToDate = LocalDate.MAX;
        anyAccessToken = "any access token";
        anyLink = new Link("/anyLink");
        anyString = "any string";

        given(mockHmrcCallWrapper.followTraverson(anyString(), eq(anyAccessToken), any()))
                .willThrow(UnsupportedOperationException.class);
        given(mockNameMatchingCandidatesService.generateCandidateNames(anyString(), anyString(), anyString()))
                .willThrow(UnsupportedOperationException.class);
        given(mockNameNormalizer.normalizeNames(any()))
                .willThrow(UnsupportedOperationException.class);
        given(mockHmrcCallWrapper.exchange(any(), any(), any(), any()))
                .willThrow(UnsupportedOperationException.class);
    }

    @Test
    public void getPayIncome_aspectApplied() {

        try {
            hmrcHateoasClient.getPayeIncome(anyFromDate, anyToDate, anyAccessToken, anyLink);
            fail("expected action to throw, but it did not!");
        } catch (UnsupportedOperationException e) {
            // This method is supposed to swallow this exception.
        }

        then(mockRequestHeaderData)
                .should()
                .abortIfTakingTooLong();
    }

    @Test
    public void getEmployments_aspectApplied() {

        try {
            hmrcHateoasClient.getEmployments(anyFromDate, anyToDate, anyAccessToken, anyLink);
            fail("expected action to throw, but it did not!");
        } catch (UnsupportedOperationException e) {
            // This method is supposed to swallow this exception.
        }

        then(mockRequestHeaderData)
                .should()
                .abortIfTakingTooLong();
    }

    @Test
    public void getSelfAssessmentSelfEmploymentIncome_aspectApplied() {

        try {
            hmrcHateoasClient.getSelfAssessmentSelfEmploymentIncome(anyAccessToken, anyLink);
            fail("expected action to throw, but it did not!");
        } catch (UnsupportedOperationException e) {
            // This method is supposed to swallow this exception.
        }

        then(mockRequestHeaderData)
                .should()
                .abortIfTakingTooLong();
    }

    @Test
    public void getMatchResource_aspectApplied() {

        try {
            hmrcHateoasClient.getMatchResource(new Individual(anyString, anyString, anyString, anyFromDate, anyString), anyAccessToken);
            fail("expected action to throw, but it did not!");
        } catch (UnsupportedOperationException e) {
            // This method is supposed to swallow this exception.
        }

        then(mockRequestHeaderData)
                .should()
                .abortIfTakingTooLong();
    }

    @Test
    public void performMatchedIndividualRequest_aspectApplied() {

        try {
            CandidateName anyCandidateName = new CandidateName("any first", "any last");
            hmrcHateoasClient.performMatchedIndividualRequest(anyString, anyAccessToken, anyCandidateName, anyString, null);
            fail("expected action to throw, but it did not!");
        } catch (UnsupportedOperationException e) {
            // This method is supposed to swallow this exception.
        }

        then(mockRequestHeaderData)
                .should()
                .abortIfTakingTooLong();
    }

    @Test
    public void getIndividualResource_aspectApplied() {

        try {
            hmrcHateoasClient.getIndividualResource(anyAccessToken, anyLink);
            fail("expected action to throw, but it did not!");
        } catch (UnsupportedOperationException e) {
            // This method is supposed to swallow this exception.
        }

        then(mockRequestHeaderData)
                .should()
                .abortIfTakingTooLong();
    }

    @Test
    public void getIncomeResource_aspectApplied() {

        try {
            hmrcHateoasClient.getIncomeResource(anyAccessToken, anyLink);
            fail("expected action to throw, but it did not!");
        } catch (UnsupportedOperationException e) {
            // This method is supposed to swallow this exception.
        }

        then(mockRequestHeaderData)
                .should()
                .abortIfTakingTooLong();
    }

    @Test
    public void getEmploymentResource_aspectApplied() {

        try {
            hmrcHateoasClient.getEmploymentResource(anyAccessToken, anyLink);
            fail("expected action to throw, but it did not!");
        } catch (UnsupportedOperationException e) {
            // This method is supposed to swallow this exception.
        }

        then(mockRequestHeaderData)
                .should()
                .abortIfTakingTooLong();
    }

    @Test
    public void getSelfAssessmentResource_aspectApplied() {

        try {
            hmrcHateoasClient.getSelfAssessmentResource(anyAccessToken, "2018", "2019", anyLink);
            fail("expected action to throw, but it did not!");
        } catch (UnsupportedOperationException e) {
            // This method is supposed to swallow this exception.
        }

        then(mockRequestHeaderData)
                .should()
                .abortIfTakingTooLong();
    }

}
