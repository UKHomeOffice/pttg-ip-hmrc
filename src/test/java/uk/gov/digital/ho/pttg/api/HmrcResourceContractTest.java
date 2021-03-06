package uk.gov.digital.ho.pttg.api;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.digital.ho.pttg.application.domain.IncomeSummary;
import uk.gov.digital.ho.pttg.application.domain.Individual;

import java.time.LocalDate;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.digital.ho.pttg.api.JsonRequestUtilities.*;
import static uk.gov.digital.ho.pttg.api.RequestHeaderData.*;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_RETRY_EVENT;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class HmrcResourceContractTest {

    private static final String SMOKE_TEST_NINO = "QQ123456C";

    @MockBean
    private IncomeSummaryService incomeSummaryService;

    @Autowired
    private MockMvc mockMvc;

    @Rule
    public OutputCapture outputCapture = new OutputCapture();

    @Test
    public void validRequestShouldRespondOk() throws Exception {
        mockMvc.perform(post("/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getDefaultRequest())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(incomeSummaryService).getIncomeSummary(new Individual("some first name", "some last name", "AA123456A", LocalDate.of(1991, 2, 3), "some alias surnames"), LocalDate.of(2013, 4, 5), LocalDate.of(2018, 6, 7));
    }

    @Test
    public void missingRequestParameterShouldRespondWithError() throws Exception {
        mockMvc.perform(post("/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestAndRemoveLineWithKey("firstName"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("firstName")));

        verifyZeroInteractions(incomeSummaryService);
    }

    @Test
    public void nullRequestParameterShouldRespondWithError() throws Exception {
        mockMvc.perform(post("/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestAndReplaceValueWithNull("lastName"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("lastName")));

        verifyZeroInteractions(incomeSummaryService);
    }

    @Test
    public void malformedRequestParameterShouldRespondWithError() throws Exception {
        mockMvc.perform(post("/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestAndReplaceValue("dateOfBirth", "not a date"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(containsString("dateOfBirth")));

        verifyZeroInteractions(incomeSummaryService);
    }

    @Test
    public void requestWithoutAliasesShouldRespondOk() throws Exception {
        mockMvc.perform(post("/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestWithoutAlias())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(incomeSummaryService).getIncomeSummary(new Individual("some first name", "some last name", "AA123456A", LocalDate.of(1991, 2, 3), ""), LocalDate.of(2013, 4, 5), LocalDate.of(2018, 6, 7));
    }

    @Test
    public void requestWithNullAliasShouldRespondOk() throws Exception {
        mockMvc.perform(post("/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestWithNullAlias())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(incomeSummaryService).getIncomeSummary(new Individual("some first name", "some last name", "AA123456A", LocalDate.of(1991, 2, 3), ""), LocalDate.of(2013, 4, 5), LocalDate.of(2018, 6, 7));
    }

    @Test
    public void correlationIdShouldBeReturnedIfPassed() throws Exception {
        mockMvc.perform(post("/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getDefaultRequest())
                .accept(MediaType.APPLICATION_JSON)
                .header(CORRELATION_ID_HEADER, "some correlation id"))
                .andExpect(status().isOk())
                .andExpect(header().string(CORRELATION_ID_HEADER, "some correlation id"));
    }

    @Test
    public void correlationIdShouldBeGeneratedIfNotPassed() throws Exception {
        mockMvc.perform(post("/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getDefaultRequest())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists(CORRELATION_ID_HEADER))
                .andExpect(header().string(CORRELATION_ID_HEADER, not(isEmptyString())));
    }

    @Test
    public void sessionIdShouldBeReturnedIfPassed() throws Exception {
        mockMvc.perform(post("/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getDefaultRequest())
                .accept(MediaType.APPLICATION_JSON)
                .header(SESSION_ID_HEADER, "some session id"))
                .andExpect(status().isOk())
                .andExpect(header().string(SESSION_ID_HEADER, "some session id"));
    }

    @Test
    public void sessionIdShouldBeGeneratedIfNotPassed() throws Exception {
        mockMvc.perform(post("/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getDefaultRequest())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(SESSION_ID_HEADER, "unknown"));
    }

    @Test
    public void userIdShouldBeReturnedIfPassed() throws Exception {
        mockMvc.perform(post("/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getDefaultRequest())
                .accept(MediaType.APPLICATION_JSON)
                .header(USER_ID_HEADER, "some user id"))
                .andExpect(status().isOk())
                .andExpect(header().string(USER_ID_HEADER, "some user id"));
    }

    @Test
    public void userIdShouldBeGeneratedIfNotPassed() throws Exception {
        mockMvc.perform(post("/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getDefaultRequest())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(USER_ID_HEADER, "unknown"));
    }

    @Test
    public void getHmrcData_smokeTestNino_smokeTest_returnOk() throws Exception {
        LocalDate anyDateOfBirth = LocalDate.now();
        Individual anyIndividual = new Individual("any firstName", "any lastName", "any nino", anyDateOfBirth, "any alias surnames");
        IncomeSummary anyIncomeSummary = new IncomeSummary(emptyList(), emptyList(), emptyList(), anyIndividual);

        when(incomeSummaryService.getIncomeSummary(any(Individual.class), any(LocalDate.class), any(LocalDate.class))).thenReturn(anyIncomeSummary);

        mockMvc.perform(post("/income")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(getRequestAndReplaceValue("nino", SMOKE_TEST_NINO))
                                .header(USER_ID_HEADER, SMOKE_TESTS_USER_ID)
                                .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
    }

    @Test
    public void getHmrcData_smokeTestNino_notASmokeTest_returnUnprocessableEntity() throws Exception {
        mockMvc.perform(post("/income")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(getRequestAndReplaceValue("nino", SMOKE_TEST_NINO))
                                .header(USER_ID_HEADER, "not a smoke-test user")
                                .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnprocessableEntity())
               .andExpect(content().string(containsString("NINO")));
    }
    @Test
    public void retryCountShouldBeLogged() throws Exception {
        mockMvc.perform(post("/income")
            .contentType(MediaType.APPLICATION_JSON)
            .content(getDefaultRequest())
            .header(RETRY_COUNT_HEADER, "2"));

        String expectedLogOutput = String.format("\"%s\":\"%s\"", EVENT, HMRC_RETRY_EVENT);
        outputCapture.expect(containsString(expectedLogOutput));
    }

    @Test
    public void retryCountShouldNotBeLogged() throws Exception {
        mockMvc.perform(post("/income")
            .contentType(MediaType.APPLICATION_JSON)
            .content(getDefaultRequest()));

        String unexpectedLogOutput = String.format("\"%s\":\"%s\"", EVENT, HMRC_RETRY_EVENT);
        outputCapture.expect(not(containsString(unexpectedLogOutput)));
    }

}
