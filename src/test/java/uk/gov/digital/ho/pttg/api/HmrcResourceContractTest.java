package uk.gov.digital.ho.pttg.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.digital.ho.pttg.application.domain.Individual;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.digital.ho.pttg.api.JsonRequestUtilities.*;
import static uk.gov.digital.ho.pttg.api.RequestHeaderData.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class HmrcResourceContractTest {

    @MockBean
    private IncomeSummaryService incomeSummaryService;

    @Autowired
    private MockMvc mockMvc;

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
    public void getRequestWithoutAliasesShouldRespondOk() throws Exception {
        String someFirstName = "some first name";
        String someLastName = "some last name";
        String someNino = "AA123456A";
        LocalDate someDateOfBirth = LocalDate.of(1991, 2, 3);
        LocalDate someFromDate = LocalDate.of(2013, 4, 5);
        LocalDate someToDate = LocalDate.of(2018, 6, 7);

        mockMvc.perform(get("/income")
                .contentType(MediaType.APPLICATION_JSON)
                .param("firstName", someFirstName)
                .param("lastName", someLastName)
                .param("nino", someNino)
                .param("dateOfBirth", someDateOfBirth.toString())
                .param("fromDate", someFromDate.toString())
                .param("toDate", someToDate.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(incomeSummaryService).getIncomeSummary(new Individual(someFirstName, someLastName, someNino, someDateOfBirth, ""), someFromDate, someToDate);
    }

    @Test
    public void getRequestWithAliasesShouldRespondOk() throws Exception {
        String someFirstName = "some first name";
        String someLastName = "some last name";
        String someNino = "AA123456A";
        LocalDate someDateOfBirth = LocalDate.of(1991, 2, 3);
        LocalDate someFromDate = LocalDate.of(2013, 4, 5);
        LocalDate someToDate = LocalDate.of(2018, 6, 7);
        String someAliasSurnames = "some alias surnames";

        mockMvc.perform(get("/income")
                .contentType(MediaType.APPLICATION_JSON)
                .param("firstName", someFirstName)
                .param("lastName", someLastName)
                .param("nino", someNino)
                .param("dateOfBirth", someDateOfBirth.toString())
                .param("fromDate", someFromDate.toString())
                .param("toDate", someToDate.toString())
                .param("aliasSurnames", someAliasSurnames)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(incomeSummaryService).getIncomeSummary(new Individual(someFirstName, someLastName, someNino, someDateOfBirth, someAliasSurnames), someFromDate, someToDate);
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

}
