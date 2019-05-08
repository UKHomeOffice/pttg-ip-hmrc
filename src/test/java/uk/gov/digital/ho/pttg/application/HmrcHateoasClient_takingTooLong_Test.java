package uk.gov.digital.ho.pttg.application;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.hateoas.Link;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.InsuffienctTimeException;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.willThrow;

@RunWith(MockitoJUnitRunner.class)
public class HmrcHateoasClient_takingTooLong_Test {

    @Mock private RequestHeaderData mockRequestHeaderData;

    private HmrcHateoasClient client;

    private LocalDate anyFromDate = LocalDate.MAX;
    private LocalDate anyToDate = LocalDate.MAX;
    private String anyAccessToken = "any access token";
    private Link anyLink = new Link("any link");
    private String anyMatchUrl = "any match url";
    private CandidateName anyCandidateNames = new CandidateName("any first name", "any last name");
    private String anyNino = "any nino";
    private LocalDate anyDob = LocalDate.MAX;
    private String anyTaxYear = "any tax year";

    @Before
    public void setup() {
        client = new HmrcHateoasClient(mockRequestHeaderData, null, null, null, null);

        willThrow(new InsuffienctTimeException("Some reason"))
            .given(mockRequestHeaderData).abortIfTakingTooLong();
    }

    @Test
    public void getPayeIncome_whenTakingTooLong_aborts() {
        assertThatExceptionOfType(InsuffienctTimeException.class)
                .isThrownBy(() -> client.getPayeIncome(anyFromDate, anyToDate, anyAccessToken, anyLink));
    }

    @Test
    public void getEmployments_whenTakingTooLong_aborts() {
        assertThatExceptionOfType(InsuffienctTimeException.class)
                .isThrownBy(() -> client.getEmployments(anyFromDate, anyToDate, anyAccessToken, anyLink));
    }

    @Test
    public void getSelfAssessmentSelfEmploymentIncome_whenTakingTooLong_aborts() {
        assertThatExceptionOfType(InsuffienctTimeException.class)
                .isThrownBy(() -> client.getSelfAssessmentSelfEmploymentIncome(anyAccessToken, anyLink));
    }

    @Test
    public void performMatchedIndividualRequest_whenTakingTooLong_aborts() {
        assertThatExceptionOfType(InsuffienctTimeException.class)
                .isThrownBy(() -> ReflectionTestUtils.invokeMethod(client, "performMatchedIndividualRequest", anyMatchUrl, anyAccessToken, anyCandidateNames, anyNino, anyDob));
    }

    @Test
    public void getIndividualResource_whenTakingTooLong_aborts() {
        assertThatExceptionOfType(InsuffienctTimeException.class)
                .isThrownBy(() -> ReflectionTestUtils.invokeMethod(client, "getIndividualResource", anyAccessToken, anyLink));
    }

    @Test
    public void getIncomeResource_whenTakingTooLong_aborts() {
        assertThatExceptionOfType(InsuffienctTimeException.class)
                .isThrownBy(() -> ReflectionTestUtils.invokeMethod(client, "getIncomeResource", anyAccessToken, anyLink));
    }

    @Test
    public void getEmploymentResource_whenTakingTooLong_aborts() {
        assertThatExceptionOfType(InsuffienctTimeException.class)
                .isThrownBy(() -> ReflectionTestUtils.invokeMethod(client, "getEmploymentResource", anyAccessToken, anyLink));
    }

    @Test
    public void getSelfAssessmentResource_whenTakingTooLong_aborts() {
        assertThatExceptionOfType(InsuffienctTimeException.class)
                .isThrownBy(() -> ReflectionTestUtils.invokeMethod(client, "getSelfAssessmentResource", anyAccessToken, anyTaxYear, anyTaxYear, anyLink));
    }
}
