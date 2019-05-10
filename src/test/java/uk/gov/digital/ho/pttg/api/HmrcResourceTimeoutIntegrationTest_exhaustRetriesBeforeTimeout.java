package uk.gov.digital.ho.pttg.api;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.ServiceRunner;
import uk.gov.digital.ho.pttg.application.HmrcAccessCodeClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static uk.gov.digital.ho.pttg.api.HmrcResourceTimeoutIntegrationTestUtils.*;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_SERVICE_RESPONSE_ERROR;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = ServiceRunner.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "hmrc.retry.attempts=1",
        "hmrc.retry.delay=0"
})
public class HmrcResourceTimeoutIntegrationTest_exhaustRetriesBeforeTimeout {

    private static final String MATCH_ID = "87654321";
    private static final String ACCESS_ID = "987987987";

    private MockRestServiceServer hmrcApiMockService;
    private Appender<ILoggingEvent> mockAppender;

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
        MockRestServiceServer auditMockService = buildMockService(auditRestTemplate);
        MockRestServiceServer hmrcAccessCodeMockService = buildMockService(hmrcAccessCodeRestTemplate);
        hmrcApiMockService = buildMockService(hmrcApiRestTemplate);

        auditMockService
                .expect(requestTo(containsString("/audit")))
                .andExpect(method(POST))
                .andRespond(withSuccess());

        hmrcAccessCodeMockService
                .expect(requestTo(containsString("/access")))
                .andExpect(method(GET))
                .andRespond(withSuccess(buildOauthResponse(mapper, ACCESS_ID), APPLICATION_JSON));

        ReflectionTestUtils.setField(hmrcAccessCodeClient, "accessCode", Optional.empty());

        mockAppender = mock(Appender.class);
        Logger rootLogger = (Logger) LoggerFactory.getLogger(ResourceExceptionHandler.class);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(mockAppender);
    }

    @Test
    public void shouldExhaustRetriesWhenTimeoutIsLarge_matchResponse() {

        resourceRequest(hmrcApiMockService, "/individuals/matching/", MATCH_ID, POST, 0, "matchResponse", INTERNAL_SERVER_ERROR);
        resourceRequest(hmrcApiMockService, "/individuals/matching/" + MATCH_ID, MATCH_ID, GET, 0, "individualMatchResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/employments/?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "employmentsResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/employments/paye?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "employmentsPayeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/paye?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomePayeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/sa?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeSAResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/sa/self-employments?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeSASelfEmploymentsResponse", OK);

        ResponseEntity<String> responseEntity = performHmrcRequest(restTemplate, 9000);

        validateResponse(responseEntity);
        validateLogging();
    }

    @Test
    public void shouldExhaustRetriesWhenTimeoutIsLarge_individualMatchResponse() {

        resourceRequest(hmrcApiMockService, "/individuals/matching/", MATCH_ID, POST, 0, "matchResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/matching/" + MATCH_ID, MATCH_ID, GET, 0, "individualMatchResponse", INTERNAL_SERVER_ERROR);
        resourceRequest(hmrcApiMockService, "/individuals/income/?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/employments/?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "employmentsResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/employments/paye?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "employmentsPayeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/paye?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomePayeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/sa?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeSAResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/sa/self-employments?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeSASelfEmploymentsResponse", OK);

        ResponseEntity<String> responseEntity = performHmrcRequest(restTemplate, 9000);

        validateResponse(responseEntity);
        validateLogging();
    }

    @Test
    public void shouldExhaustRetriesWhenTimeoutIsLarge_incomeResponse() {

        resourceRequest(hmrcApiMockService, "/individuals/matching/", MATCH_ID, POST, 0, "matchResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/matching/" + MATCH_ID, MATCH_ID, GET, 0, "individualMatchResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeResponse", INTERNAL_SERVER_ERROR);
        resourceRequest(hmrcApiMockService, "/individuals/employments/?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "employmentsResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/employments/paye?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "employmentsPayeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/paye?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomePayeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/sa?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeSAResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/sa/self-employments?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeSASelfEmploymentsResponse", OK);

        ResponseEntity<String> responseEntity = performHmrcRequest(restTemplate, 9000);

        validateResponse(responseEntity);
        validateLogging();
    }

    @Test
    public void shouldExhaustRetriesWhenTimeoutIsLarge_employmentsResponse() {

        resourceRequest(hmrcApiMockService, "/individuals/matching/", MATCH_ID, POST, 0, "matchResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/matching/" + MATCH_ID, MATCH_ID, GET, 0, "individualMatchResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/employments/?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "employmentsResponse", INTERNAL_SERVER_ERROR);
        resourceRequest(hmrcApiMockService, "/individuals/employments/paye?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "employmentsPayeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/paye?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomePayeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/sa?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeSAResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/sa/self-employments?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeSASelfEmploymentsResponse", OK);

        ResponseEntity<String> responseEntity = performHmrcRequest(restTemplate, 9000);

        validateResponse(responseEntity);
        validateLogging();
    }

    @Test
    public void shouldExhaustRetriesWhenTimeoutIsLarge_employmentsPayeResponse() {

        resourceRequest(hmrcApiMockService, "/individuals/matching/", MATCH_ID, POST, 0, "matchResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/matching/" + MATCH_ID, MATCH_ID, GET, 0, "individualMatchResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/employments/?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "employmentsResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/employments/paye?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "employmentsPayeResponse", INTERNAL_SERVER_ERROR);
        resourceRequest(hmrcApiMockService, "/individuals/income/paye?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomePayeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/sa?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeSAResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/sa/self-employments?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeSASelfEmploymentsResponse", OK);

        ResponseEntity<String> responseEntity = performHmrcRequest(restTemplate, 9000);

        validateResponse(responseEntity);
        validateLogging();
    }

    @Test
    public void shouldExhaustRetriesWhenTimeoutIsLarge_incomePayeResponse() {

        resourceRequest(hmrcApiMockService, "/individuals/matching/", MATCH_ID, POST, 0, "matchResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/matching/" + MATCH_ID, MATCH_ID, GET, 0, "individualMatchResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/employments/?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "employmentsResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/employments/paye?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "employmentsPayeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/paye?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomePayeResponse", INTERNAL_SERVER_ERROR);
        resourceRequest(hmrcApiMockService, "/individuals/income/sa?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeSAResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/sa/self-employments?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeSASelfEmploymentsResponse", OK);

        ResponseEntity<String> responseEntity = performHmrcRequest(restTemplate, 9000);

        validateResponse(responseEntity);
        validateLogging();
    }

    @Test
    public void shouldExhaustRetriesWhenTimeoutIsLarge_incomeSAResponse() {

        resourceRequest(hmrcApiMockService, "/individuals/matching/", MATCH_ID, POST, 0, "matchResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/matching/" + MATCH_ID, MATCH_ID, GET, 0, "individualMatchResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/employments/?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "employmentsResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/employments/paye?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "employmentsPayeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/paye?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomePayeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/sa?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeSAResponse", INTERNAL_SERVER_ERROR);
        resourceRequest(hmrcApiMockService, "/individuals/income/sa/self-employments?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeSASelfEmploymentsResponse", OK);

        ResponseEntity<String> responseEntity = performHmrcRequest(restTemplate, 9000);

        validateResponse(responseEntity);
        validateLogging();
    }

    @Test
    public void shouldExhaustRetriesWhenTimeoutIsLarge_incomeSASelfEmploymentsResponse() {

        resourceRequest(hmrcApiMockService, "/individuals/matching/", MATCH_ID, POST, 0, "matchResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/matching/" + MATCH_ID, MATCH_ID, GET, 0, "individualMatchResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/employments/?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "employmentsResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/employments/paye?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "employmentsPayeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/paye?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomePayeResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/sa?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeSAResponse", OK);
        resourceRequest(hmrcApiMockService, "/individuals/income/sa/self-employments?matchId=" + MATCH_ID, MATCH_ID, GET, 0, "incomeSASelfEmploymentsResponse", INTERNAL_SERVER_ERROR);

        ResponseEntity<String> responseEntity = performHmrcRequest(restTemplate, 9000);

        validateResponse(responseEntity);
        validateLogging();
    }

    private void validateResponse(ResponseEntity<String> responseEntity) {
        assertThat(responseEntity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(responseEntity.getBody()).isEqualTo(responseMessage(INTERNAL_SERVER_ERROR));
    }

    private void validateLogging() {
        then(mockAppender)
                .should()
                .doAppend(argThat(isALogMessageWith(
                        "HttpServerErrorException: 500 Internal Server Error",
                        1,
                        HMRC_SERVICE_RESPONSE_ERROR)));
    }

}

