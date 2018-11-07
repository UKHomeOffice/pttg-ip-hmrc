package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.ServiceRunner;
import uk.gov.digital.ho.pttg.application.HmrcAccessCodeClient;
import uk.gov.digital.ho.pttg.application.domain.IncomeSummary;
import uk.gov.digital.ho.pttg.dto.AccessCode;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.MockRestServiceServer.MockRestServiceServerBuilder;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = ServiceRunner.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "hmrc.retry.unauthorized-attempts=2",
        "hmrc.access.service.retry.attempts=3",
        "hmrc.sa.self-employment-only=true"
})
public class HmrcResourceIntegrationTest {

    private static final String MATCH_ID = "87654321";
    private static final String ACCESS_ID = "987987987";

    private MockRestServiceServer auditMockService;
    private MockRestServiceServer hmrcAccecssCodeMockService;
    private MockRestServiceServer hmrcApiMockService;

    @Autowired
    private RestTemplate auditRestTemplate;

    @Autowired
    private RestTemplate hmrcAccessCodeRestTemplate;

    @Autowired
    private RestTemplate hmrcApiRestTemplate;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private HmrcAccessCodeClient hmrcAccessCodeClient;

    @Before
    public void setup() throws JsonProcessingException {
        auditMockService = buildMockService(auditRestTemplate);
        hmrcAccecssCodeMockService = buildMockService(hmrcAccessCodeRestTemplate);
        hmrcApiMockService = buildMockService(hmrcApiRestTemplate);

        auditMockService
                .expect(requestTo(containsString("/audit")))
                .andExpect(method(POST))
                .andRespond(withSuccess());

        hmrcAccecssCodeMockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildOauthResponse(), APPLICATION_JSON));

        ReflectionTestUtils.setField(hmrcAccessCodeClient, "accessCode", Optional.ofNullable(null));
    }

    private MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServerBuilder auditBuilder = bindTo(restTemplate);
        auditBuilder.ignoreExpectOrder(true);
        return auditBuilder.build();
    }

    @Test
    public void shouldMakeAllServiceCalls() throws IOException {

        buildAndExpectSuccessfulTraversal();

        ResponseEntity<IncomeSummary> responseEntity = restTemplate.getForEntity("/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01", IncomeSummary.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        //verifies all services were called.
        auditMockService.verify();
        hmrcAccecssCodeMockService.verify();
        hmrcApiMockService.verify();

        assertThat(responseEntity.getBody().getPaye().size()).isEqualTo(7);
        assertThat(responseEntity.getBody().getPaye().get(0).getWeekPayNumber()).isEqualTo(49);
        assertThat(responseEntity.getBody().getEmployments().size()).isEqualTo(2);
        assertThat(responseEntity.getBody().getEmployments().get(0).getEmployer().getName()).isEqualTo("Acme Inc");
        assertThat(responseEntity.getBody().getEmployments().get(1).getEmployer().getName()).isEqualTo("Disney Inc");
        assertThat(responseEntity.getBody().getSelfAssessment().size()).isEqualTo(2);
        assertThat(responseEntity.getBody().getSelfAssessment().get(0).getSelfEmploymentProfit()).isEqualTo(BigDecimal.ZERO);
        assertThat(responseEntity.getBody().getSelfAssessment().get(0).getTaxYear()).isEqualTo("2014-15");
        assertThat(responseEntity.getBody().getSelfAssessment().get(1).getSelfEmploymentProfit()).isEqualTo(new BigDecimal("10500"));
        assertThat(responseEntity.getBody().getSelfAssessment().get(1).getTaxYear()).isEqualTo("2013-14");
        assertThat(responseEntity.getBody().getIndividual().getFirstName()).isEqualTo("Laurie");
        assertThat(responseEntity.getBody().getIndividual().getLastName()).isEqualTo("Halford");
        assertThat(responseEntity.getBody().getIndividual().getNino()).isEqualTo("GH576240A");
        assertThat(responseEntity.getBody().getIndividual().getDateOfBirth()).isEqualTo(LocalDate.of(1992, 3, 1));
    }

    @Test
    public void shouldMakeAllServiceCallsWhenNoSelfAssessmentData() throws IOException {

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withSuccess(buildMatchResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/matching/" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildMatchedIndividualResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/income/?matchId=" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildIncomeWithoutSaResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/employments/?matchId=" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildEmploymentsResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/employments/paye?matchId=" + MATCH_ID + "&fromDate=2017-01-01&toDate=2017-06-01")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildEmploymentsPayeResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/income/paye?matchId=" + MATCH_ID + "&fromDate=2017-01-01&toDate=2017-06-01")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildPayeIncomeResponse(), APPLICATION_JSON));


        ResponseEntity<IncomeSummary> responseEntity = restTemplate.getForEntity("/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01", IncomeSummary.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

        hmrcApiMockService.verify();

        assertThat(responseEntity.getBody().getSelfAssessment().size()).isEqualTo(0);
    }

    @Test
    public void shouldHandleBadHMRCServiceRequest() {

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withBadRequest());

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                String.class);

        hmrcApiMockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void anyHmrcErrorShouldBePercolatedThrough() {

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withStatus(I_AM_A_TEAPOT));

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                String.class);

        hmrcApiMockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(I_AM_A_TEAPOT);
    }

    @Test
    public void accessCodeServiceUnavailable() {

        auditMockService.reset();
        hmrcAccecssCodeMockService.reset();
        hmrcApiMockService.reset();

        auditMockService
                .expect(requestTo(containsString("/audit")))
                .andExpect(method(POST))
                .andRespond(withSuccess());

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                String.class);

        auditMockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void accessCodeServiceThrowsClientError() {

        auditMockService.reset();
        hmrcAccecssCodeMockService.reset();
        hmrcApiMockService.reset();

        auditMockService
                .expect(requestTo(containsString("/audit")))
                .andExpect(method(POST))
                .andRespond(withSuccess());
        hmrcAccecssCodeMockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withBadRequest());

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                String.class);

        auditMockService.verify();
        hmrcAccecssCodeMockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void accessCodeServiceWithRetriesThrowsServerError() {

        auditMockService.reset();
        hmrcAccecssCodeMockService.reset();
        hmrcApiMockService.reset();

        auditMockService
                .expect(requestTo(containsString("/audit")))
                .andExpect(method(POST))
                .andRespond(withSuccess());
        hmrcAccecssCodeMockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withServerError());
        hmrcAccecssCodeMockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withServerError());
        hmrcAccecssCodeMockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withServerError());

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                String.class);

        auditMockService.verify();
        hmrcAccecssCodeMockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void accessCodeServiceGetsResponseAfterRetry() {

        auditMockService.reset();
        hmrcAccecssCodeMockService.reset();
        hmrcApiMockService.reset();

        auditMockService
                .expect(requestTo(containsString("/audit")))
                .andExpect(method(POST))
                .andRespond(withSuccess());
        hmrcAccecssCodeMockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withServerError());
        hmrcAccecssCodeMockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withBadRequest());

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                String.class);

        auditMockService.verify();
        hmrcAccecssCodeMockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void shouldAllowAnOptionalToDate() throws IOException {

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withSuccess(buildMatchResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/matching/" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildMatchedIndividualResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/income/?matchId=" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildIncomeResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/employments/?matchId=" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildEmploymentsResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString(
                        "/individuals/employments/paye?matchId=" + MATCH_ID + "&fromDate=2017-01-01")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildEmploymentsPayeResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString(
                        "/individuals/income/paye?matchId=" + MATCH_ID + "&fromDate=2017-01-01")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildPayeIncomeResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString(
                        "/individuals/income/sa?matchId=" + MATCH_ID + "&fromTaxYear=2016-17")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildSaResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString(
                        "/individuals/income/sa/self-employments?matchId=" + MATCH_ID + "&fromTaxYear=2016-17")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildSaSelfEmploymentResponse(), APPLICATION_JSON));

        ResponseEntity<IncomeSummary> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&dateOfBirth=1992-03-01", IncomeSummary.class);

        auditMockService.verify();
        hmrcAccecssCodeMockService.verify();
        hmrcApiMockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody().getEmployments().get(0).getEmployer().getName()).isEqualTo("Acme Inc");
        assertThat(responseEntity.getBody().getPaye().get(0).getWeekPayNumber()).isEqualTo(49);
    }

    @Test
    public void hmrcReturnsErrorDuringLinkTraversal() throws IOException {

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withSuccess(buildMatchResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/matching/" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildMatchedIndividualResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/income/?matchId=" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildIncomeResponse(), APPLICATION_JSON));

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01", String.class);

        auditMockService.verify();
        hmrcAccecssCodeMockService.verify();
        hmrcApiMockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
    }

    @Test
    public void shouldRetryAccessCallIfUnauthorizedResponseFromHmrcCalls() throws IOException {
        // given
        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withUnauthorizedRequest());

        auditMockService
                .expect(requestTo(containsString("/audit")))
                .andExpect(method(POST))
                .andRespond(withSuccess());

        hmrcAccecssCodeMockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildOauthResponse(), APPLICATION_JSON));

        hmrcAccecssCodeMockService
                .expect(requestTo(containsString("/access/" + ACCESS_ID + "/report")))
                .andExpect(method(POST))
                .andRespond(withSuccess());


        buildAndExpectSuccessfulTraversal();

        // when
        ResponseEntity<IncomeSummary> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                IncomeSummary.class);

        // then
        auditMockService.verify();
        hmrcAccecssCodeMockService.verify();
        hmrcApiMockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

        // verify response body is correct
        assertThat(responseEntity.getBody().getPaye().size()).isEqualTo(7);
        assertThat(responseEntity.getBody().getSelfAssessment().size()).isEqualTo(2);
        assertThat(responseEntity.getBody().getIndividual().getFirstName()).isEqualTo("Laurie");
        assertThat(responseEntity.getBody().getIndividual().getLastName()).isEqualTo("Halford");
        assertThat(responseEntity.getBody().getIndividual().getNino()).isEqualTo("GH576240A");
        assertThat(responseEntity.getBody().getIndividual().getDateOfBirth()).isEqualTo(LocalDate.of(1992, 3, 1));
    }

    @Test
    public void shouldGiveUnauthorizedWhenFailedUnauthorizedRetriesWithHmrc() throws IOException {
        // given
        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withUnauthorizedRequest());

        hmrcAccecssCodeMockService
                .expect(requestTo(containsString("/access/" + ACCESS_ID + "/report")))
                .andExpect(method(POST))
                .andRespond(withSuccess());

        hmrcAccecssCodeMockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildOauthResponse(), APPLICATION_JSON));

        auditMockService
                .expect(requestTo(containsString("/audit")))
                .andExpect(method(POST))
                .andRespond(withSuccess());

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withUnauthorizedRequest());

        // when
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                String.class);

        // then
        auditMockService.verify();
        hmrcAccecssCodeMockService.verify();
        hmrcApiMockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
    }

    @Test
    public void shouldRetryAccessCallIfConnectionRefused() {
        // given
        auditMockService.reset();
        hmrcAccecssCodeMockService.reset();
        hmrcApiMockService.reset();

        auditMockService
                .expect(requestTo(containsString("/audit")))
                .andExpect(method(POST))
                .andRespond(withSuccess());

        hmrcAccecssCodeMockService
                .expect(times(3), requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(request -> {
                    final HttpHostConnectException httpHostConnectException = new HttpHostConnectException(new ConnectException(), HttpHost.create("/access"));
                    throw new ResourceAccessException("ExceptionMessage", httpHostConnectException);
                });

        // when
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Laurie&nino=GH576240A&lastName=Halford&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                String.class);

        // then
        auditMockService.verify();
        hmrcAccecssCodeMockService.verify();
        hmrcApiMockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(responseEntity.getBody()).contains("HttpHostConnectException: Connect to /access refused");
    }

    @Test
    public void shouldRetryUntilMatchingNameFound() throws IOException {
        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).body("{\"code\" : \"MATCHING_FAILED\", \"message\" : \"There is no match for the information provided\"}"));

        buildAndExpectSuccessfulTraversal();

        ResponseEntity<IncomeSummary> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Halford&nino=GH576240A&lastName=Laurie&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                IncomeSummary.class);

        auditMockService.verify();
        hmrcAccecssCodeMockService.verify();
        hmrcApiMockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
        assertThat(responseEntity.getBody().getIndividual().getFirstName()).isEqualTo("Laurie");
        assertThat(responseEntity.getBody().getIndividual().getLastName()).isEqualTo("Halford");

    }

    @Test
    public void shouldReturnInternalServerErrorIfNonHmrcForbiddenReceived() {
        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                "/income?firstName=Halford&nino=GH576240A&lastName=Laurie&fromDate=2017-01-01&toDate=2017-06-01&dateOfBirth=1992-03-01",
                String.class);

        auditMockService.verify();
        hmrcAccecssCodeMockService.verify();
        hmrcApiMockService.verify();

        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(responseEntity.getBody()).isEqualTo("Received a 403 Forbidden response from proxy");
    }

    private void buildAndExpectSuccessfulTraversal() throws IOException {
        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/matching/")))
                .andExpect(method(POST))
                .andRespond(withSuccess(buildMatchResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/matching/" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildMatchedIndividualResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/income/?matchId=" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildIncomeResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/employments/?matchId=" + MATCH_ID)))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildEmploymentsResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/employments/paye?matchId=" + MATCH_ID + "&fromDate=2017-01-01&toDate=2017-06-01")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildEmploymentsPayeResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/income/paye?matchId=" + MATCH_ID + "&fromDate=2017-01-01&toDate=2017-06-01")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildPayeIncomeResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/income/sa?matchId=" + MATCH_ID + "&fromTaxYear=2016-17&toTaxYear=2017-18")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildSaResponse(), APPLICATION_JSON));

        hmrcApiMockService
                .expect(requestTo(containsString("/individuals/income/sa/self-employments?matchId=" + MATCH_ID + "&fromTaxYear=2016-17&toTaxYear=2017-18")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildSaSelfEmploymentResponse(), APPLICATION_JSON));
    }

    private String loadJsonFile(String filename) throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream(String.format("/template/%s.json", filename)), StandardCharsets.UTF_8);
    }

    private String buildOauthResponse() throws JsonProcessingException {
        return mapper.writeValueAsString(new AccessCode(ACCESS_ID, LocalDateTime.MAX, LocalDateTime.MAX));
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

    private String buildIncomeWithoutSaResponse() throws IOException {
        return loadJsonFile("incomeResponseNoSa")
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

    private String buildSaSelfEmploymentResponse() throws IOException {
        return loadJsonFile("incomeSASelfEmploymentsResponse")
                .replace("${matchId}", MATCH_ID);
    }
}

