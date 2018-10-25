package bdd.steps;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import cucumber.api.DataTable;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.core.SessionMap;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.digital.ho.pttg.ServiceRunner;
import uk.gov.digital.ho.pttg.application.HmrcClient;
import uk.gov.digital.ho.pttg.application.domain.Individual;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.jayway.restassured.RestAssured.given;
import static java.time.format.DateTimeFormatter.*;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

@SuppressFBWarnings({
        "SE_NO_SERIALVERSIONID",
        "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"})
@SpringBootTest(classes = ServiceRunner.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
        "pttg.audit.url=http://localhost:1111",
        "base.hmrc.url=http://localhost:2222",
        "base.hmrc.access.code.url=http://localhost:3333",
        "hmrc.sa.self-employment-only=false"
        })
public class NameMatchingSteps {
    private static final String INDIVIDUAL_MATCHING_RESPONSE_SESSION_KEY = "IndividualMatchingResponse";
    private static final boolean IGNORE_JSON_ARRAY_ORDER = true;
    private static final boolean IGNORE_EXTRA_ELEMENTS = true;
    private static final String INDIVIDUAL_MATCHING_ENDPOINT = "/individuals/matching/";
    private static final String MATCH_ID = "s87654321";

    private static final WireMockServer AUDIT_MOCK_SERVICE = new WireMockServer(options().port(1111));
    private static final WireMockServer HMRC_MOCK_SERVICE = new WireMockServer(options().port(2222));
    private static final WireMockServer ACCESS_KEY_MOCK_SERVICE = new WireMockServer(options().port(3333));

    private static boolean isSetup = false;

    @Autowired
    private HmrcClient hmrcClient;

    @LocalServerPort
    private int port;

    @Before
    public void setup() throws Exception {
        if (!isSetup) {

            HMRC_MOCK_SERVICE.start();
            AUDIT_MOCK_SERVICE.start();
            ACCESS_KEY_MOCK_SERVICE.start();

            setupAuditStub();
            setupAccessCodeStub();

            RestAssured.port = port;
            isSetup = true;
        }

        setupStubsForOtherHmrcCalls();
    }

    @After
    public static void teardown() {
        Serenity.clearCurrentSession();
        HMRC_MOCK_SERVICE.resetAll();
    }

    private static void setupAuditStub() {
        AUDIT_MOCK_SERVICE
                .stubFor(post(urlMatching("/audit.*"))
                                 .willReturn(aResponse().withStatus(200)));
    }

    private static void setupAccessCodeStub() {
        LocalDateTime now = LocalDateTime.now();
        String expiryTime = ISO_LOCAL_DATE_TIME.format(now.plusHours(5));
        String refreshTime = ISO_LOCAL_DATE_TIME.format(now.plusHours(5).minusMinutes(1));

        String accessCodeJsonResponse =
                new JSONObject()
                        .put("code", "TestCode")
                        .put("expiry", expiryTime)
                        .put("refreshTime", refreshTime)
                        .toString();

        ACCESS_KEY_MOCK_SERVICE.stubFor(get(urlMatching("/access"))
                                                .willReturn(aResponse()
                                                                    .withHeader("Content-Type", "application/json")
                                                                    .withBody(accessCodeJsonResponse)
                                                                    .withStatus(200)));
    }

    @Given("^HMRC has the following individual records$")
    public void hmrcHasTheFollowingIndividualRecords(DataTable dataTable) throws Throwable {

        List<Individual> individuals = dataTable.asList(IndividualRow.class)
                                           .stream()
                                           .map(IndividualRow::toIndividual)
                                           .collect(toList());

        for (Individual individual : individuals) {

            String firstname = individual.getFirstName();
            String lastname = individual.getLastName();

            // Check if lastname contains spaces
            Pattern pattern = Pattern.compile("\\s");
            Matcher matcher = pattern.matcher(lastname);
            int extraCharacter = 0;

            // If surname contains spaces and the first part of the surname is shorter than 3 characters,
            // add an extra character to the string that the mock will match
            if (matcher.find() && lastname.split("\\s+")[0].length() < 3) {
                extraCharacter = 1;
            }

            // Get at least the first letter of the firstname and at least the first three letters of the lastname
            int nameIndex = Math.min(1, firstname.length());
            int surnameIndex = Math.min(3 + extraCharacter, lastname.length());

            // Match json on NINO, DOB and with initials of name and surname only
            HMRC_MOCK_SERVICE.stubFor(post(urlEqualTo(INDIVIDUAL_MATCHING_ENDPOINT))
                                              .atPriority(1)
                                              .withRequestBody(matchingJsonPath("$.nino", equalTo(individual.getNino())))
                                              .withRequestBody(matchingJsonPath("$.dateOfBirth", equalTo(ISO_LOCAL_DATE.format(individual.getDateOfBirth()))))
                                              .withRequestBody(matchingJsonPath("$.firstName", matching(firstname.substring(0, nameIndex) + ".*")))
                                              .withRequestBody(matchingJsonPath("$.lastName", matching(lastname.substring(0, surnameIndex) + ".*")))
                                              .willReturn(aResponse()
                                                                  .withBody(buildMatchResponse())
                                                                  .withStatus(HttpStatus.OK.value())
                                                                  .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));
        }

        HMRC_MOCK_SERVICE.stubFor(post(urlEqualTo(INDIVIDUAL_MATCHING_ENDPOINT))
                                          .atPriority(5)
                                          .willReturn(aFailedMatchResponse()));
    }

    @When("^the applicant submits the following data to the RPS service$")
    public void theApplicantSubmitsTheFollowingDataToTheRPSService(DataTable dataTable) {

        Map<String, String> individualMap = dataTable.asMap(String.class, String.class);
        IndividualRow individualRow = IndividualRow.fromMap(individualMap);

        LocalDate now = LocalDate.now();
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("nino", individualRow.getNino());
        requestParameters.put("firstName", individualRow.getFirstName());
        requestParameters.put("lastName", individualRow.getLastName());
        requestParameters.put("dateOfBirth", individualRow.getDateOfBirth());
        requestParameters.put("fromDate", now.format(ISO_DATE));

        if (!Objects.isNull(individualRow.getAliasSurname())) {
            requestParameters.put("aliasSurnames", individualRow.getAliasSurname());
        }
        ImmutableMap<String, String> requestBody = ImmutableMap.copyOf(requestParameters);

        Response response = given()
                                .basePath("/income")
                                .queryParameters(requestBody)
                                .get();

        SessionMap<Object, Object> currentSession = Serenity.getCurrentSession();
        currentSession.put(INDIVIDUAL_MATCHING_RESPONSE_SESSION_KEY, response);
    }

    @When("^the service configuration is changed to self assessment summary$")
    public void theServiceConfigurationIsChangedToSelfAssessmentSummary() {
        ReflectionTestUtils.setField(hmrcClient, "selfEmploymentOnly", true);
    }

    @When("^the service configuration is changed to self assessment self employment only")
    public void theServiceConfigurationIsChangedToSelfAssessmentSelfEmploymentOnly() {
        ReflectionTestUtils.setField(hmrcClient, "selfEmploymentOnly", false);
    }

    @Then("^the footprint will try the following combination of names in order$")
    public void theFootprintWillTryTheFollowingCombinationOfNamesInOrder(DataTable dataTable) {

        List<Individual> individuals = dataTable.asList(IndividualRow.class)
                                               .stream()
                                               .map(IndividualRow::toIndividual)
                                               .collect(toList());

        List<LoggedRequest> matchingRequestsInOrder = getIndividualMatchingRequestsInOrder();

        int numberOfExpectedRequests = individuals.size();
        int numberOfActualRequests = matchingRequestsInOrder.size();

        assertEquals("Unexpected number of requests to HMRC Individual Matching", numberOfExpectedRequests, numberOfActualRequests);

        AtomicInteger index = new AtomicInteger(0);

        matchingRequestsInOrder
            .forEach(request -> {
                    Individual expectedIndividual = individuals.get(index.getAndIncrement());
                    verifyRequestContainsExpectedNames(request, expectedIndividual);
                });
    }

    private static void verifyRequestContainsExpectedNames(LoggedRequest loggedRequest, Individual expectedIndividual) {

        String expectedJson = getIndividualMatchRequestExpectedJson(expectedIndividual);
        StringValuePattern stringValuePattern = equalToJson(expectedJson, IGNORE_JSON_ARRAY_ORDER, IGNORE_EXTRA_ELEMENTS);

        String requestBody = loggedRequest.getBodyAsString();
        boolean exactMatch = stringValuePattern.match(requestBody).isExactMatch();

        assertTrue(
                String.format("Expected request with names: {First name:%s,Last name: %s}, Actual request: %s",
                        expectedIndividual.getFirstName(),
                        expectedIndividual.getLastName(),
                        requestBody),
                exactMatch);
    }

    private static List<LoggedRequest> getIndividualMatchingRequestsInOrder() {
        return HMRC_MOCK_SERVICE.findAll(postRequestedFor(urlEqualTo(INDIVIDUAL_MATCHING_ENDPOINT)));
    }

    @And("^a Matched response will be returned from the service$")
        public void aMatchedResponseWillBeReturnedFromTheService() {

        Response response = getIndividualMatchingResponse();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @And("^the self employment profit will be returned from the service$")
    public void theSelfEmploymentProfitWillBeReturnedFromTheService() {

        Response response = getIndividualMatchingResponse();

        assertNotNull(response);
        JsonPath jsonPath = new JsonPath(response.asString());
        assertEquals(10500, jsonPath.getInt("selfAssessment[1].selfEmploymentProfit"));
        assertEquals(200, response.getStatusCode());
    }

    @And("^the summary income will be returned from the service$")
    public void theSummaryIncomeWillBeReturnedFromTheService() {

        Response response = getIndividualMatchingResponse();

        assertNotNull(response);
        JsonPath jsonPath = new JsonPath(response.asString());
        assertEquals(30000, jsonPath.getInt("selfAssessment[1].summaryIncome"));
        assertEquals(200, response.getStatusCode());
    }

    private static ResponseDefinitionBuilder aFailedMatchResponse() {
        return aResponse()
                .withStatus(HttpStatus.FORBIDDEN.value())
                .withBody("{ \"code\":\"MATCHING_FAILED\",\"message\":\"There is no match for the information provided\"}")
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
    }

    @And("^HMRC was called (\\d+) times$")
    public void hmrcWasCalledTimes(int numberOfCalls) {
        List<LoggedRequest> matchingRequests = getIndividualMatchingRequestsInOrder();
        assertEquals(numberOfCalls, matchingRequests.size());
    }

    @Then("^a not matched response is returned$")
    public void aNotMatchedResponseIsReturned() {
        Response response = getIndividualMatchingResponse();

        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
    }

    private Response getIndividualMatchingResponse() {
        SessionMap<Object, Object> sessionMap = Serenity.getCurrentSession();
        return (Response) sessionMap.get(INDIVIDUAL_MATCHING_RESPONSE_SESSION_KEY);
    }

    private static String getIndividualMatchRequestExpectedJson(Individual individual) {
        return String.format("{\"firstName\":\"%s\", \"lastName\":\"%s\"}",
                individual.getFirstName(),
                individual.getLastName());
    }

    private void setupStubsForOtherHmrcCalls() throws IOException {
        HMRC_MOCK_SERVICE.stubFor(get(urlMatching(String.format("%s.*", INDIVIDUAL_MATCHING_ENDPOINT)))
                                          .willReturn(aResponse()
                                                              .withStatus(200)
                                                              .withHeader("Content-Type", "application/json")
                                                              .withBody(buildMatchedIndividualResponse())
                                          ));

        HMRC_MOCK_SERVICE.stubFor(get(urlMatching("/individuals/income/.*"))
                                          .willReturn(aResponse()
                                                              .withStatus(200)
                                                              .withHeader("Content-Type", "application/json")
                                                              .withBody(buildIncomeResponse())
                                          ));

        HMRC_MOCK_SERVICE.stubFor(get(urlMatching("/individuals/employments/.*"))
                                          .willReturn(aResponse()
                                                              .withStatus(200)
                                                              .withHeader("Content-Type", "application/json")
                                                              .withBody(buildEmploymentsResponse())
                                          ));

        HMRC_MOCK_SERVICE.stubFor(get(urlMatching("/individuals/employments/paye.*"))
                                          .willReturn(aResponse()
                                                              .withStatus(200)
                                                              .withHeader("Content-Type", "application/json")
                                                              .withBody(buildEmploymentsPayeResponse())
                                          ));

        HMRC_MOCK_SERVICE.stubFor(get(urlMatching("/individuals/income/paye.*"))
                                          .willReturn(aResponse()
                                                              .withStatus(200)
                                                              .withHeader("Content-Type", "application/json")
                                                              .withBody(buildPayeIncomeResponse())
                                          ));

        HMRC_MOCK_SERVICE.stubFor(get(urlMatching("/individuals/income/sa\\?.*"))
                                          .willReturn(aResponse()
                                                              .withStatus(200)
                                                              .withHeader("Content-Type", "application/json")
                                                              .withBody(buildSaResponse())
                                          ));

        HMRC_MOCK_SERVICE.stubFor(get(urlMatching("/individuals/income/sa/self-employments.*"))
                                          .willReturn(aResponse()
                                                              .withStatus(200)
                                                              .withHeader("Content-Type", "application/json")
                                                              .withBody(buildSaSelfEmploymentResponse())
                                          ));

        HMRC_MOCK_SERVICE.stubFor(get(urlMatching("/individuals/income/sa/summary.*"))
                                          .willReturn(aResponse()
                                                              .withStatus(200)
                                                              .withHeader("Content-Type", "application/json")
                                                              .withBody(buildSaSummaryResponse())
                                          ));
    }

    private String loadJsonFile(String filename) throws IOException {
        InputStream resourceAsStream = this.getClass().getResourceAsStream(String.format("/template/%s.json", filename));
        return IOUtils.toString(resourceAsStream, Charset.defaultCharset());
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

    private String buildSaSelfEmploymentResponse() throws IOException {
        return loadJsonFile("incomeSASelfEmploymentsResponse")
                       .replace("${matchId}", MATCH_ID);
    }

    private String buildSaSummaryResponse() throws IOException {
        return loadJsonFile("incomeSASummaryResponse")
                       .replace("${matchId}", MATCH_ID);
    }

}
