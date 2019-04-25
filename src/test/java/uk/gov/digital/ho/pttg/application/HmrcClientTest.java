package uk.gov.digital.ho.pttg.application;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.hateoas.Link;
import uk.gov.digital.ho.pttg.application.domain.Individual;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HmrcClientTest {

    @Mock private HmrcHateoasClient mockHmrcHateoasClient;


    @Test
    @SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void shouldGetSelfEmployment() {
        HmrcClient hmrcClient = new HmrcClient(mockHmrcHateoasClient);

        IncomeSummaryContext mockIncomeSummaryContext = mock(IncomeSummaryContext.class);
        when(mockIncomeSummaryContext.needsSelfAssessmentSelfEmploymentIncome()).thenReturn(true);
        when(mockIncomeSummaryContext.getSelfAssessmentLink(any(String.class))).thenReturn(mock(Link.class));

        hmrcClient.populateIncomeSummary("some access token", mock(Individual.class), LocalDate.now(), LocalDate.now(), mockIncomeSummaryContext);

        verify(mockIncomeSummaryContext).needsSelfAssessmentSelfEmploymentIncome();
        verify(mockHmrcHateoasClient).getSelfAssessmentSelfEmploymentIncome(anyString(), any(Link.class));
    }
}
