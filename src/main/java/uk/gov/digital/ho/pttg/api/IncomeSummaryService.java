package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.pttg.application.HmrcAccessCodeClient;
import uk.gov.digital.ho.pttg.application.HmrcClient;
import uk.gov.digital.ho.pttg.application.HmrcRetryTemplateFactory;
import uk.gov.digital.ho.pttg.application.IncomeSummaryContext;
import uk.gov.digital.ho.pttg.application.domain.IncomeSummary;
import uk.gov.digital.ho.pttg.application.domain.Individual;
import uk.gov.digital.ho.pttg.audit.AuditClient;

import java.time.LocalDate;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_API_CALL_ATTEMPT;
import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_INCOME_REQUEST;

@Slf4j
@Service
public class IncomeSummaryService {

    private final HmrcClient hmrcClient;
    private final HmrcAccessCodeClient accessCodeClient;
    private final AuditClient auditClient;
    private final RetryTemplate reauthorisingRetryTemplate;
    private final int unauthorisedRetryAttempts;
    private final RequestHeaderData requestHeaderData;
    private HmrcRetryTemplateFactory hmrcRetryTemplateFactory;

    @Autowired
    public IncomeSummaryService(HmrcClient hmrcClient,
                                HmrcAccessCodeClient accessCodeClient,
                                AuditClient auditClient,
                                RetryTemplate reauthorisingRetryTemplate,
                                @Value("${hmrc.retry.unauthorized-attempts}") int unauthorisedRetryAttempts,
                                RequestHeaderData requestHeaderData,
                                HmrcRetryTemplateFactory hmrcRetryTemplateFactory) {

        this.hmrcClient = hmrcClient;
        this.accessCodeClient = accessCodeClient;
        this.auditClient = auditClient;

        this.reauthorisingRetryTemplate = reauthorisingRetryTemplate;
        this.unauthorisedRetryAttempts = unauthorisedRetryAttempts;
        this.requestHeaderData = requestHeaderData;
        this.hmrcRetryTemplateFactory = hmrcRetryTemplateFactory;
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

            log.debug("Request Income Summary from HMRC. Attempt number {} of {}", context.getRetryCount() + 1, unauthorisedRetryAttempts);

            return requestIncomeSummary(individual, fromDate, toDate);
        });
    }

    private IncomeSummary requestIncomeSummary(Individual individual, LocalDate fromDate, LocalDate toDate) {

        auditRequestToHmrc(individual);

        String accessCode = requestAccessCode();

        return generateIncomeSummary(individual, fromDate, toDate, accessCode);
    }

    private IncomeSummary generateIncomeSummary(Individual individual, LocalDate fromDate, LocalDate toDate, String accessCode) {

        IncomeSummaryContext incomeSummaryContext = new IncomeSummaryContext();

        RetryTemplate retryTemplate = hmrcRetryTemplateFactory.createInstance();

        return retryTemplate.execute(retryContext -> {
            log.info("HMRC call attempt {}",
                    retryContext.getRetryCount() + 1,
                    value(EVENT, HMRC_API_CALL_ATTEMPT));
            return hmrcClient.populateIncomeSummary(accessCode, individual, fromDate, toDate, incomeSummaryContext);
        });

    }

    private String requestAccessCode() {
        return accessCodeClient.getAccessCode();
    }

    private void auditRequestToHmrc(Individual individual) {
        UUID eventId = UUID.randomUUID();
        auditClient.add(HMRC_INCOME_REQUEST, eventId, null);
    }
}
