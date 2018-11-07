package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.pttg.application.HmrcAccessCodeClient;
import uk.gov.digital.ho.pttg.application.HmrcClient;
import uk.gov.digital.ho.pttg.application.IncomeSummaryContext;
import uk.gov.digital.ho.pttg.application.domain.IncomeSummary;
import uk.gov.digital.ho.pttg.application.domain.Individual;
import uk.gov.digital.ho.pttg.audit.AuditClient;
import uk.gov.digital.ho.pttg.audit.AuditIndividualData;

import java.time.LocalDate;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_API_CALL_ATTEMPT;
import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_INCOME_REQUEST;
import static uk.gov.digital.ho.pttg.audit.AuditIndividualData.GET_HMRC_DATA_METHOD;

@Slf4j
@Service
public class IncomeSummaryService {

    private final HmrcClient hmrcClient;
    private final HmrcAccessCodeClient accessCodeClient;
    private final AuditClient auditClient;
    private final RetryTemplate reauthorisingRetryTemplate;
    private final RetryTemplate apiFailureRetryTemplate;
    private final int hmrcApiFailureRetryAttempts;

    @Autowired
    public IncomeSummaryService(
            HmrcClient hmrcClient,
            HmrcAccessCodeClient accessCodeClient,
            AuditClient auditClient,
            RetryTemplate reauthorisingRetryTemplate,
            RetryTemplate apiFailureRetryTemplate,
            @Value("${hmrc.retry.attempts}") int hmrcApiFailureRetryAttempts) {

        this.hmrcClient = hmrcClient;
        this.accessCodeClient = accessCodeClient;
        this.auditClient = auditClient;

        this.reauthorisingRetryTemplate = reauthorisingRetryTemplate;
        this.apiFailureRetryTemplate = apiFailureRetryTemplate;
        this.hmrcApiFailureRetryAttempts = hmrcApiFailureRetryAttempts;
    }

    IncomeSummary getIncomeSummary(Individual individual, LocalDate fromDate, LocalDate toDate) {
        return requestIncomeSummaryWithRetries(individual, fromDate, toDate);
    }

    private IncomeSummary requestIncomeSummaryWithRetries(Individual individual, LocalDate fromDate, LocalDate toDate) {

        return reauthorisingRetryTemplate.execute(context -> {

            if (context.getRetryCount() > 0) {
                log.debug("Access Code refresh required");
                accessCodeClient.reportBadAccessCode();
                accessCodeClient.loadLatestAccessCode();
            }

            log.debug("Request Income Summary from HMRC. Attempt number {} of {}", context.getRetryCount() + 1, hmrcApiFailureRetryAttempts);

            return requestIncomeSummary(individual, fromDate, toDate);
        });
    }

    private IncomeSummary requestIncomeSummary(Individual individual, LocalDate fromDate, LocalDate toDate) {

        auditRequestToHmrc(individual);

        String accessCode = requestAccessCode();

        IncomeSummaryContext incomeSummaryContext = new IncomeSummaryContext();

        return apiFailureRetryTemplate.execute(context -> {
            log.info("HMRC call attempt {} of {}", context.getRetryCount() + 1, hmrcApiFailureRetryAttempts, value(EVENT, HMRC_API_CALL_ATTEMPT));
            return hmrcClient.getIncomeSummary(accessCode, individual, fromDate, toDate, incomeSummaryContext);
        });
    }

    private String requestAccessCode() {
        return accessCodeClient.getAccessCode();
    }

    private void auditRequestToHmrc(Individual individual) {
        UUID eventId = UUID.randomUUID();
        AuditIndividualData auditIndividualData = new AuditIndividualData(GET_HMRC_DATA_METHOD, individual);

        auditClient.add(HMRC_INCOME_REQUEST, eventId, auditIndividualData);
    }
}
