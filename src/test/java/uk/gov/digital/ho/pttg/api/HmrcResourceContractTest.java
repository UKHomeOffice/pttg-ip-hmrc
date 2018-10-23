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
import uk.gov.digital.ho.pttg.dto.Individual;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.digital.ho.pttg.api.JsonRequestUtilities.*;

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

        verify(incomeSummaryService).getIncomeSummary(new Individual("some first name", "some last name", "AA123456A", LocalDate.of(1991, 2, 3)), LocalDate.of(2013, 4, 5), LocalDate.of(2018, 6, 7));
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

        verify(incomeSummaryService).getIncomeSummary(new Individual("some first name", "some last name", "AA123456A", LocalDate.of(1991, 2, 3)), LocalDate.of(2013, 4, 5), LocalDate.of(2018, 6, 7));
    }

    @Test
    public void requestWithNullAliasShouldRespondOk() throws Exception {
        mockMvc.perform(post("/income")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestWithNullAlias())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(incomeSummaryService).getIncomeSummary(new Individual("some first name", "some last name", "AA123456A", LocalDate.of(1991, 2, 3)), LocalDate.of(2013, 4, 5), LocalDate.of(2018, 6, 7));
    }

}
