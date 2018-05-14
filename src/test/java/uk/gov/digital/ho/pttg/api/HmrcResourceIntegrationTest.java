package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.ServiceRunner;
import uk.gov.digital.ho.pttg.dto.AuthToken;
import uk.gov.digital.ho.pttg.dto.IncomeSummary;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.MockRestServiceServer.MockRestServiceServerBuilder;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = ServiceRunner.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HmrcResourceIntegrationTest {

    private static final String MATCH_ID = "87654321";
    private static final String ACCESS_ID = "987987987";

    private MockRestServiceServer mockService;

    @Autowired
    private RestTemplate mockRestTemplate;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Before
    public void setup() throws JsonProcessingException {
        MockRestServiceServerBuilder builder = bindTo(mockRestTemplate);
        builder.ignoreExpectOrder(true);
        mockService = builder.build();

        mockService
                .expect(requestTo(containsString("/audit")))
                .andExpect(method(POST))
                .andRespond(withSuccess());

        mockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildOauthResponse(), APPLICATION_JSON));
    }

    @Test
    public void shouldMakeAllServiceCalls() throws IOException {

        expectSuccessfulTraversal();

        ResponseEntity<IncomeSummary> responseEntity = restTemplate.getForEntity("/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01", IncomeSummary.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        //verifies all services were called.
        mockService.verify();

        assertThat(responseEntity.getBody().getPaye().size()).isEqualTo(7);
        assertThat(responseEntity.getBody().getSelfAssessment().size()).isEqualTo(2);
        assertThat(responseEntity.getBody().getSelfAssessment().size()).isEqualTo(2);
        assertThat(responseEntity.getBody().getIndividual().getFirstName()).isEqualTo("Laurie");
        assertThat(responseEntity.getBody().getIndividual().getLastName()).isEqualTo("Halford");
        assertThat(responseEntity.getBody().getIndividual().getNino()).isEqualTo("GH576240A");
        assertThat(responseEntity.getBody().getIndividual().getDateOfBirth()).isEqualTo(LocalDate.of(1992, 3, 1));
    }

    @Test
    public void shouldHandleBadHMRCServiceRequest() {

        mockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withBadRequest());

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                String.class);

        mockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void anyHMRCErrorShouldBePercolatedThrough() {

        mockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withStatus(I_AM_A_TEAPOT));

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                String.class);

        mockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(I_AM_A_TEAPOT);
    }

    @Test
    public void accessCodeServiceThrowsClientError() {

        mockService.reset();

        mockService
                .expect(requestTo(containsString("/audit")))
                .andExpect(method(POST))
                .andRespond(withSuccess());
        mockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withBadRequest());

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                String.class);

        mockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void accessCodeServiceWithRetriesThrowsServerError() {

        mockService.reset();

        mockService
                .expect(requestTo(containsString("/audit")))
                .andExpect(method(POST))
                .andRespond(withSuccess());
        mockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withServerError());
        mockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withServerError());
        mockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withServerError());

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                String.class);

        mockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void accessCodeServiceGetsResponseAfterRetry() {

        mockService.reset();

        mockService
                .expect(requestTo(containsString("/audit")))
                .andExpect(method(POST))
                .andRespond(withSuccess());
        mockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withServerError());
        mockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withBadRequest());

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                String.class);

        mockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void shouldAllowAnOptionalToDate() throws IOException {

        mockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withSuccess(buildMatchResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString("/individuals/matching/" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildMatchedIndividualResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString("/individuals/income/?matchId=" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildIncomeResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString("/individuals/employments/?matchId=" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildEmploymentsResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString(
                        "/individuals/employments/paye?matchId=" + MATCH_ID + "&fromDate=2017-01-01")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildEmploymentsPayeResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString(
                        "/individuals/income/paye?matchId=" + MATCH_ID + "&fromDate=2017-01-01")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildPayeIncomeResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString(
                        "/individuals/income/sa?matchId=" + MATCH_ID + "&fromTaxYear=2016-17")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildSaResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString(
                        "/individuals/income/sa/self-employments?matchId=" + MATCH_ID + "&fromTaxYear=2016-17")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildSaSelfEmploymentResponse(), APPLICATION_JSON));

        ResponseEntity<IncomeSummary> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&dateOfBirth=1992-03-01", IncomeSummary.class);

        mockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody().getEmployments().get(0).getEmployer().getName()).isEqualTo("Acme Inc");
        assertThat(responseEntity.getBody().getPaye().get(0).getWeekPayNumber()).isEqualTo(49);
    }

    @Test
    public void HMRCreturnsErrorDuringLinkTraversal() throws IOException {

        mockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withSuccess(buildMatchResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString("/individuals/matching/" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildMatchedIndividualResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString("/individuals/income/?matchId=" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildIncomeResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString(
                        "/individuals/income/paye?matchId=" + MATCH_ID + "&fromDate=2017-01-01")))
                .andExpect(method(GET))
                .andRespond(withStatus(INTERNAL_SERVER_ERROR));

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01", String.class);

        mockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldRetryAccessCallIfUnauthorizedResponseFromHmrcCalls() throws IOException {
        // given
        mockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withUnauthorizedRequest());

        mockService
                .expect(requestTo(containsString("/audit")))
                .andExpect(method(POST))
                .andRespond(withSuccess());

        mockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildOauthResponse(), APPLICATION_JSON));


        expectSuccessfulTraversal();

        // when
        ResponseEntity<IncomeSummary> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                IncomeSummary.class);

        // then
        // verify all expected calls were made
        mockService.verify();

        // verify success response code
        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

        // verify response body is correct
        assertThat(responseEntity.getBody().getPaye().size()).isEqualTo(7);
        assertThat(responseEntity.getBody().getSelfAssessment().size()).isEqualTo(2);
        assertThat(responseEntity.getBody().getSelfAssessment().size()).isEqualTo(2);
        assertThat(responseEntity.getBody().getIndividual().getFirstName()).isEqualTo("Laurie");
        assertThat(responseEntity.getBody().getIndividual().getLastName()).isEqualTo("Halford");
        assertThat(responseEntity.getBody().getIndividual().getNino()).isEqualTo("GH576240A");
        assertThat(responseEntity.getBody().getIndividual().getDateOfBirth()).isEqualTo(LocalDate.of(1992, 3, 1));
    }

    @Test
    public void shouldGiveUnauthorizedWhenFailedUnauthorizedRetriesWithHmrc() throws IOException {
        // given
        mockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withUnauthorizedRequest());

        mockService
                .expect(requestTo(containsString("/audit")))
                .andExpect(method(POST))
                .andRespond(withSuccess());

        mockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildOauthResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withUnauthorizedRequest());

        // when
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                String.class);

        // then
        // verify all expected calls were made
        mockService.verify();

        // verify success response code
        assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
    }

    private void expectSuccessfulTraversal() throws IOException {
        mockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withSuccess(buildMatchResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString("/individuals/matching/" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildMatchedIndividualResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString("/individuals/income/?matchId=" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildIncomeResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString("/individuals/employments/?matchId=" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildEmploymentsResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString("/individuals/employments/paye?matchId=" + MATCH_ID + "&fromDate=2017-01-01&toDate=2017-06-01")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildEmploymentsPayeResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString("/individuals/income/paye?matchId=" + MATCH_ID + "&fromDate=2017-01-01&toDate=2017-06-01")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildPayeIncomeResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString("/individuals/income/sa?matchId=" + MATCH_ID + "&fromTaxYear=2016-17&toTaxYear=2017-18")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildSaResponse(), APPLICATION_JSON));

        mockService
                .expect(requestTo(containsString("/individuals/income/sa/self-employments?matchId=" + MATCH_ID + "&fromTaxYear=2016-17&toTaxYear=2017-18")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildSaSelfEmploymentResponse(), APPLICATION_JSON));
    }


    private String loadJsonFile(String filename) throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream(String.format("/template/%s.json", filename)));
    }

    private String buildOauthResponse() throws JsonProcessingException {
        return mapper.writeValueAsString(new AuthToken(ACCESS_ID, null));
    }

    private String buildMatchResponse() throws IOException {
        return loadJsonFile("matchResponse")
                .replace("${matchId}", MATCH_ID);
    }

    private String buildMatchedIndividualResponse() throws IOException {
        return loadJsonFile("individualMatchResponse")
                .replace("${matchId}", MATCH_ID);
    }

    private String buildIncomeResponse() throws IOException {
        return loadJsonFile("incomeResponse")
                .replace("${matchId}", MATCH_ID);
    }

    private String buildEmploymentsResponse() throws IOException {
        return loadJsonFile("employmentsResponse")
                .replace("${matchId}", MATCH_ID);
    }

    private String buildEmploymentsPayeResponse() throws IOException {
        return loadJsonFile("employmentsPayeResponse")
                .replace("${matchId}", MATCH_ID);
    }

    private String buildPayeIncomeResponse() throws IOException {
        return loadJsonFile("incomePayeResponse")
                .replace("${matchId}", MATCH_ID);
    }

    private String buildSaResponse() throws IOException {
        return loadJsonFile("incomeSAResponse")
                .replace("${matchId}", MATCH_ID);
    }

    private String buildEmptySaResponse() throws IOException {
        return loadJsonFile("incomeSAResponseEmpty")
                .replace("${matchId}", MATCH_ID);
    }

    private String buildSaSelfEmploymentResponse() throws IOException {
        return loadJsonFile("incomeSASelfEmploymentsResponse")
                .replace("${matchId}", MATCH_ID);
    }

}

