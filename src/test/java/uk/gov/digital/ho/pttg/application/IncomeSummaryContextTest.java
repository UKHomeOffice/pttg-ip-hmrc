package uk.gov.digital.ho.pttg.application;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.digital.ho.pttg.application.domain.Individual;
import uk.gov.digital.ho.pttg.dto.AnnualSelfAssessmentTaxReturn;
import uk.gov.digital.ho.pttg.dto.EmbeddedIndividual;
import uk.gov.digital.ho.pttg.dto.Employment;
import uk.gov.digital.ho.pttg.dto.Income;

import java.util.List;

import static java.util.Collections.EMPTY_LIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static uk.gov.digital.ho.pttg.application.IncomeDataTypes.PAYE;
import static uk.gov.digital.ho.pttg.application.IncomeDataTypes.SELF_ASSESSMENT;

public class IncomeSummaryContextTest {

    private IncomeSummaryContext incomeSummaryContext;
    private Link mockLink;

    @Before
    public void setup() {
        incomeSummaryContext = new IncomeSummaryContext();
        mockLink = mock(Link.class);
    }

    @Test
    public void shouldRequireAllData() {
        assertThat(incomeSummaryContext.needsMatchResource()).isTrue();
        assertThat(incomeSummaryContext.needsIndividualResource()).isTrue();
        assertThat(incomeSummaryContext.needsIncomeResource()).isTrue();
        assertThat(incomeSummaryContext.needsEmploymentResource()).isTrue();
        assertThat(incomeSummaryContext.needsSelfAssessmentResource()).isTrue();
        assertThat(incomeSummaryContext.needsPayeIncome()).isTrue();
        assertThat(incomeSummaryContext.needsEmployments()).isTrue();
        assertThat(incomeSummaryContext.needsSelfAssessmentSelfEmploymentIncome()).isTrue();
    }

    @Test
    public void shouldSetMatchResource() {
        Resource<String> mockResource = mock(Resource.class);

        given(mockResource.getLink(anyString())).willReturn(mockLink);

        incomeSummaryContext.matchResource(mockResource);

        assertThat(incomeSummaryContext.needsMatchResource()).isFalse();
        assertThat(incomeSummaryContext.getMatchLink("some link")).isEqualTo(mockLink);
    }

    @Test
    public void shouldSetIndividualResource() {
        Resource<EmbeddedIndividual> mockResource = mock(Resource.class);

        given(mockResource.getLink(anyString())).willReturn(mockLink);

        incomeSummaryContext.individualResource(mockResource);

        assertThat(incomeSummaryContext.needsIndividualResource()).isFalse();
        assertThat(incomeSummaryContext.getIndividualLink("some link")).isEqualTo(mockLink);
    }

    @Test
    public void shouldSetIndividual() {
        Resource<EmbeddedIndividual> mockResource = mock(Resource.class);
        EmbeddedIndividual mockContent = mock(EmbeddedIndividual.class);
        Individual mockIndividual = mock(Individual.class);

        given(mockResource.getContent()).willReturn(mockContent);
        given(mockContent.getIndividual()).willReturn(mockIndividual);

        incomeSummaryContext.individualResource(mockResource);

        assertThat(incomeSummaryContext.getIndividual()).isEqualTo(mockIndividual);
    }

    @Test
    public void shouldSetIncomeResource() {
        Resource<String> mockResource = mock(Resource.class);

        given(mockResource.getLink(anyString())).willReturn(mockLink);

        incomeSummaryContext.incomeResource(mockResource);

        assertThat(incomeSummaryContext.needsIncomeResource()).isFalse();
        assertThat(incomeSummaryContext.getIncomeLink("some link")).isEqualTo(mockLink);
    }

    @Test
    public void shouldSetEmploymentResource() {
        Resource<String> mockResource = mock(Resource.class);

        given(mockResource.getLink(anyString())).willReturn(mockLink);

        incomeSummaryContext.employmentResource(mockResource);

        assertThat(incomeSummaryContext.needsEmploymentResource()).isFalse();
        assertThat(incomeSummaryContext.getEmploymentLink("some link")).isEqualTo(mockLink);
    }

    @Test
    public void shouldSetSelfAssessmentResource() {
        Resource<String> mockResource = mock(Resource.class);

        given(mockResource.getLink(anyString())).willReturn(mockLink);

        incomeSummaryContext.selfAssessmentResource(mockResource);

        assertThat(incomeSummaryContext.needsSelfAssessmentResource()).isFalse();
        assertThat(incomeSummaryContext.getSelfAssessmentLink("some link")).isEqualTo(mockLink);
    }

    @Test
    public void shouldSetPayeIncome() {
        List<Income> mockIncome = mock(List.class);

        incomeSummaryContext.payeIncome(mockIncome);

        assertThat(incomeSummaryContext.needsPayeIncome()).isFalse();
        assertThat(incomeSummaryContext.payeIncome()).isEqualTo(mockIncome);
    }

    @Test
    public void shouldSetEmployments() {
        List<Employment> mockEmployments = mock(List.class);

        incomeSummaryContext.employments(mockEmployments);

        assertThat(incomeSummaryContext.needsEmployments()).isFalse();
        assertThat(incomeSummaryContext.employments()).isEqualTo(mockEmployments);
    }

    @Test
    public void shouldSetSelfAssessmentSelfEmploymentIncome() {
        List<AnnualSelfAssessmentTaxReturn> mockAnnualSelfAssessmentTaxReturns = mock(List.class);

        incomeSummaryContext.selfAssessmentSelfEmploymentIncome(mockAnnualSelfAssessmentTaxReturns);

        assertThat(incomeSummaryContext.needsSelfAssessmentSelfEmploymentIncome()).isFalse();
        assertThat(incomeSummaryContext.selfAssessmentSelfEmploymentIncome()).isEqualTo(mockAnnualSelfAssessmentTaxReturns);
    }

    @Test
    public void incomeDataAvailable_whenNone_shouldReturnEmptyList() {
        assertThat(incomeSummaryContext.incomeDataAvailable()).isEmpty();
    }

    @Test
    public void incomeDataAvailable_whenPAYE_shouldReturnPAYE() {
        ReflectionTestUtils.setField(incomeSummaryContext, "payeIncome", EMPTY_LIST);
        ReflectionTestUtils.setField(incomeSummaryContext, "selfAssessmentSelfEmploymentIncome", null);
        assertThat(incomeSummaryContext.incomeDataAvailable()).containsExactly(PAYE);
    }

    @Test
    public void incomeDataAvailable_whenSAE_shouldReturnSA() {
        ReflectionTestUtils.setField(incomeSummaryContext, "payeIncome", null);
        ReflectionTestUtils.setField(incomeSummaryContext, "selfAssessmentSelfEmploymentIncome", EMPTY_LIST);
        assertThat(incomeSummaryContext.incomeDataAvailable()).containsExactly(SELF_ASSESSMENT);
    }

    @Test
    public void incomeDataAvailable_whenBOTH_shouldReturnBOTH() {
        ReflectionTestUtils.setField(incomeSummaryContext, "payeIncome", EMPTY_LIST);
        ReflectionTestUtils.setField(incomeSummaryContext, "selfAssessmentSelfEmploymentIncome", EMPTY_LIST);
        assertThat(incomeSummaryContext.incomeDataAvailable()).containsExactly(PAYE, SELF_ASSESSMENT);
    }
}