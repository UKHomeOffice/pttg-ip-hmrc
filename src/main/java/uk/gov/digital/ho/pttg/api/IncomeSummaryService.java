package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.pttg.application.HmrcAccessCodeClient;
import uk.gov.digital.ho.pttg.application.HmrcClient;
import uk.gov.digital.ho.pttg.application.retry.HmrcClientErrorRetryPolicy;
import uk.gov.digital.ho.pttg.audit.AuditClient;
import uk.gov.digital.ho.pttg.audit.AuditIndividualData;
import uk.gov.digital.ho.pttg.dto.IncomeSummary;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.time.LocalDate;
import java.util.UUID;

import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_INCOME_REQUEST;
import static uk.gov.digital.ho.pttg.audit.AuditIndividualData.GET_HMRC_DATA_METHOD;

@Slf4j
@Service
public class IncomeSummaryService {

    private final HmrcClient hmrcClient;
    private final HmrcAccessCodeClient accessCodeClient;
    private final AuditClient auditClient;
    private final RetryTemplate retryTemplate;
    private final int hmrcUnauthorizedRetryAttempts;

    @Autowired
    public IncomeSummaryService(
            final HmrcClient hmrcClient,
            final HmrcAccessCodeClient accessCodeClient,
            final AuditClient auditClient,
            @Value("${hmrc.retry.unauthorized.attempts}") final int hmrcUnauthorizedRetryAttempts) {
        this.hmrcClient = hmrcClient;
        this.accessCodeClient = accessCodeClient;
        this.auditClient = auditClient;

        this.retryTemplate = new RetryTemplate();
        this.hmrcUnauthorizedRetryAttempts = hmrcUnauthorizedRetryAttempts;
    }

    public IncomeSummary getIncomeSummary(final Individual individual, final LocalDate fromDate, final LocalDate toDate) {
        return requestIncomeSummaryWithRetries(individual, fromDate, toDate);
    }

    private IncomeSummary requestIncomeSummaryWithRetries(final Individual individual, final LocalDate fromDate, final LocalDate toDate) {
        retryTemplate.setRetryPolicy(new HmrcClientErrorRetryPolicy(hmrcUnauthorizedRetryAttempts));
        return retryTemplate.execute(context -> {
            log.info("Attempting to request Income Summary from HMRC. Attempt number #{}", context.getRetryCount() + 1);
            return requestIncomeSummary(individual, fromDate, toDate);
        });
    }

    private IncomeSummary requestIncomeSummary(final Individual individual, final LocalDate fromDate, final LocalDate toDate) {
        auditRequestToHmrc(individual);

        final String accessCode = requestAccessCode();
        return hmrcClient.getIncome(accessCode, individual, fromDate, toDate);
    }

    private String requestAccessCode() {
        return accessCodeClient.getAccessCode();
    }

    private void auditRequestToHmrc(final Individual individual) {
        final UUID eventId = UUID.randomUUID();
        final AuditIndividualData auditIndividualData = new AuditIndividualData(GET_HMRC_DATA_METHOD, individual);

        auditClient.add(HMRC_INCOME_REQUEST, eventId, auditIndividualData);
    }
}
