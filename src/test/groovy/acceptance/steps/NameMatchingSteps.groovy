package acceptance.steps

import acceptance.rows.IndividualRow
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import com.jayway.restassured.RestAssured
import com.jayway.restassured.response.Response
import cucumber.api.DataTable
import cucumber.api.java.After
import cucumber.api.java.Before
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import groovy.json.JsonOutput
import net.serenitybdd.core.Serenity
import org.apache.commons.io.IOUtils
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import uk.gov.digital.ho.pttg.ServiceRunner
import uk.gov.digital.ho.pttg.dto.Individual

import java.nio.charset.Charset
import java.time.LocalDate
import java.time.LocalDateTime

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import static com.jayway.restassured.RestAssured.given
import static java.time.format.DateTimeFormatter.ISO_DATE
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import static org.junit.Assert.*

@SuppressFBWarnings([
        "SE_NO_SERIALVERSIONID",
        "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"])
@ContextConfiguration
@SpringBootTest(classes = ServiceRunner.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = [
                "pttg.audit.url=http://localhost:1111",
                "base.hmrc.url=http://localhost:2222",
                "base.hmrc.access.code.url=http://localhost:3333"
        ])
class NameMatchingSteps {
    private static final RESPONSE_SESSION_KEY = "IndividualMatchingResponse"
    private static final IGNORE_JSON_ARRAY_ORDER = true
    private static final IGNORE_EXTRA_ELEMENTS = true
    private static final INDIVIDUAL_MATCHING_ENDPOINT = "/individuals/matching/"
    private static final MATCH_ID = "s87654321"

    private static final AUDIT_MOCK_SERVICE = new WireMockServer(options().port(1111))
    private static final HMRC_MOCK_SERVICE = new WireMockServer(options().port(2222))
    private static final ACCESS_KEY_MOCK_SERVICE = new WireMockServer(options().port(3333))

    private static isSetup = false

    @LocalServerPort
    private int port

    @Before
    void before() throws Exception {
        if (!isSetup) {
            HMRC_MOCK_SERVICE.start()

            AUDIT_MOCK_SERVICE.start()
            ACCESS_KEY_MOCK_SERVICE.start()

            setupAuditStub()
            setupAccessCodeStub()

            RestAssured.port = port
            isSetup = true
        }

        setupStubsForOtherHmrcCalls()
    }

    private static StubMapping setupAuditStub() {
        AUDIT_MOCK_SERVICE
                .stubFor(post(urlMatching("/audit.*"))
                .willReturn(aResponse().withStatus(200)))
    }

    private static void setupAccessCodeStub() {
        def accessCodeJsonResponse = JsonOutput.toJson([
                code  : 'TestCode',
                expiry: ISO_LOCAL_DATE_TIME.format(LocalDateTime.now().plusHours(5))
        ])
        ACCESS_KEY_MOCK_SERVICE.stubFor(get(urlMatching("/access"))
                .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(accessCodeJsonResponse)
                .withStatus(200)))
    }

    @Given('^HMRC has the following individual records$')
    void hmrc_has_the_following_individual_records(DataTable dataTable) {

        def individuals = dataTable.asList(IndividualRow.class)
                .collect { row -> row.toIndividual() }

        for (individual in individuals) {
            def expectedJson = getIndividualMatchRequestExpectedJson(individual)

            // Match full json response
            HMRC_MOCK_SERVICE.stubFor(post(urlEqualTo(INDIVIDUAL_MATCHING_ENDPOINT))
                    .atPriority(1)
                    .withRequestBody(equalToJson(expectedJson, IGNORE_JSON_ARRAY_ORDER, IGNORE_EXTRA_ELEMENTS))
                    .willReturn(aResponse().withBody(buildMatchResponse()).withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))

            // Get at least the first letter of the firstname and at least the first three letters of the lastname
            int nameIndex = Math.min(1, (int) individual.getFirstName().length())
            int surnameIndex = Math.min(3, (int) individual.getLastName().length())

            // Match json with initials of name and surname only
            HMRC_MOCK_SERVICE.stubFor(post(urlEqualTo(INDIVIDUAL_MATCHING_ENDPOINT))
                    .atPriority(1)
                    .withRequestBody(matchingJsonPath("\$.firstName", matching(individual.getFirstName().substring(0, nameIndex)  + ".*")))
                    .withRequestBody(matchingJsonPath("\$.lastName", matching(individual.getLastName().substring(0, surnameIndex) + ".*")))
                    .withRequestBody(matchingJsonPath("\$.nino"))
                    .withRequestBody(matchingJsonPath("\$.dateOfBirth"))
                    .willReturn(aResponse().withBody(buildMatchResponse()).withStatus(HttpStatus.OK.value())
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)))
        }

        HMRC_MOCK_SERVICE.stubFor(post(urlEqualTo(INDIVIDUAL_MATCHING_ENDPOINT))
                .atPriority(5)
                .willReturn(aFailedMatchResponse()))
    }

    @When('^the applicant submits the following data to the RPS service$')
    static void theApplicantSubmitsTheFollowingDataToTheRpsService(DataTable dataTable) {

        def individualMap = dataTable.asMap(String.class, String.class)
        def individual = IndividualRow.fromMap(individualMap)

        def now = LocalDate.now()
        def requestBody = [
                nino       : individual.nino,
                firstName  : individual.firstName,
                lastName   : individual.lastName,
                dateOfBirth: individual.dateOfBirth,
                fromDate   : now.format(ISO_DATE)
        ]

        def response = given()
                .basePath("/income")
                .queryParameters(requestBody)
                .get()

        def session = Serenity.getCurrentSession()
        session.put(RESPONSE_SESSION_KEY, response)
    }

    @Then('^the footprint will try the following combination of names in order$')
    static void theFootprintWillTryTheFollowingCombinationOfNamesInOrder(DataTable dataTable) {
        def individuals = dataTable.asList(IndividualRow.class)
                .collect { row -> row.toIndividual() }

        def matchingRequestsInOrder = getIndividualMatchingRequestsInOrder()

        def numberOfExpectedRequests = individuals.size()
        def numberOfActualRequests = matchingRequestsInOrder.size()
        assertEquals("Unexpected number of requests to HMRC Individual Matching", numberOfExpectedRequests, numberOfActualRequests)

        matchingRequestsInOrder.eachWithIndex { request, index ->
            def expectedIndividual = individuals.get(index)
            verifyRequestContainsExpectedNames(request, expectedIndividual)
        }
    }

    private static void verifyRequestContainsExpectedNames(LoggedRequest loggedRequest, Individual expectedIndividual) {
        def expectedJson = getIndividualMatchRequestExpectedJson(expectedIndividual)
        def stringValuePattern = equalToJson(expectedJson, IGNORE_JSON_ARRAY_ORDER, IGNORE_EXTRA_ELEMENTS)

        def requestBody = loggedRequest.getBodyAsString()
        def exactMatch = stringValuePattern.match(requestBody).isExactMatch()

        assertTrue("Expected request with names: {First name:`${expectedIndividual.getFirstName()}`,Last name: `${expectedIndividual.getLastName()}`}, Actual request: $requestBody", exactMatch)
    }

    private static List<LoggedRequest> getIndividualMatchingRequestsInOrder() {
        return HMRC_MOCK_SERVICE.findAll(postRequestedFor(urlEqualTo(INDIVIDUAL_MATCHING_ENDPOINT)))
    }

    @And('^a Matched response will be returned from the service$')
    static void aMatchedResponseWillBeReturnedFromTheService() throws Throwable {
        def session = Serenity.getCurrentSession()
        Response response = session.get(RESPONSE_SESSION_KEY) as Response

        assertNotNull(response)
        assertEquals(200, response.getStatusCode())
    }

    private static ResponseDefinitionBuilder aFailedMatchResponse() {
        aResponse()
                .withStatus(HttpStatus.FORBIDDEN.value())
                .withBody("{ \"code\":\"MATCHING_FAILED\",\"message\":\"There is no match for the information provided\"}")
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    }

    private static String getIndividualMatchRequestExpectedJson(Individual individual) {
        "{\"firstName\":\"${individual.getFirstName()}\", \"lastName\":\"${individual.getLastName()}\"}".toString()
    }

    @And('^HMRC was called (\\d+) times$')
    static void hmrcWasCalledTimes(int numberOfCalls) throws Throwable {
        def matchingRequests = getIndividualMatchingRequestsInOrder()

        assertEquals(numberOfCalls, matchingRequests.size())
    }

    @Then('^a not matched response is returned$')
    static void aNotMatchedResponseIsReturned() throws Throwable {
        def session = Serenity.getCurrentSession()
        Response response = session.get(RESPONSE_SESSION_KEY) as Response

        assertNotNull(response)
        assertEquals(404, response.getStatusCode())
    }

    @After
    static void cleanUp() {
        Serenity.clearCurrentSession()
        HMRC_MOCK_SERVICE.resetAll()
    }

    private void setupStubsForOtherHmrcCalls() {
        HMRC_MOCK_SERVICE.stubFor(get(urlMatching("${INDIVIDUAL_MATCHING_ENDPOINT}.*"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(buildMatchedIndividualResponse())
        ))

        HMRC_MOCK_SERVICE.stubFor(get(urlMatching("/individuals/income/.*"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(buildIncomeResponse())
        ))

        HMRC_MOCK_SERVICE.stubFor(get(urlMatching("/individuals/employments/.*"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(buildEmploymentsResponse())
        ))

        HMRC_MOCK_SERVICE.stubFor(get(urlMatching("/individuals/employments/paye.*"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(buildEmploymentsPayeResponse())
        ))

        HMRC_MOCK_SERVICE.stubFor(get(urlMatching("/individuals/income/paye.*"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(buildPayeIncomeResponse())
        ))

        HMRC_MOCK_SERVICE.stubFor(get(urlMatching("/individuals/income/sa.*"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(buildSaResponse())
        ))

        HMRC_MOCK_SERVICE.stubFor(get(urlMatching("/individuals/income/sa/self-employments.*"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(buildSaSelfEmploymentResponse())
        ))
    }

    private String loadJsonFile(String filename) throws IOException {
        def resourceAsStream = this.getClass().getResourceAsStream("/template/${filename}.json")
        return IOUtils.toString(resourceAsStream, Charset.defaultCharset())
    }

    private String buildMatchResponse() throws IOException {
        return loadJsonFile("matchResponse")
                .replace('${matchId}', MATCH_ID)
    }

    private String buildMatchedIndividualResponse() throws IOException {
        return loadJsonFile("individualMatchResponse")
                .replace('${matchId}', MATCH_ID)
    }

    private String buildIncomeResponse() throws IOException {
        return loadJsonFile("incomeResponse")
                .replace('${matchId}', MATCH_ID)
    }

    private String buildEmploymentsResponse() throws IOException {
        return loadJsonFile("employmentsResponse")
                .replace('${matchId}', MATCH_ID)
    }

    private String buildEmploymentsPayeResponse() throws IOException {
        return loadJsonFile("employmentsPayeResponse")
                .replace('${matchId}', MATCH_ID)
    }

    private String buildPayeIncomeResponse() throws IOException {
        return loadJsonFile("incomePayeResponse")
                .replace('${matchId}', MATCH_ID)
    }

    private String buildSaResponse() throws IOException {
        return loadJsonFile("incomeSAResponse")
                .replace('${matchId}', MATCH_ID)
    }

    private String buildSaSelfEmploymentResponse() throws IOException {
        return loadJsonFile("incomeSASelfEmploymentsResponse")
                .replace('${matchId}', MATCH_ID)
    }
}
