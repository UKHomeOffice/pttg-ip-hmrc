package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.pttg.application.HmrcAccessCodeClient;
import uk.gov.digital.ho.pttg.application.HmrcClient;
import uk.gov.digital.ho.pttg.audit.AuditClient;
import uk.gov.digital.ho.pttg.audit.AuditIndividualData;
import uk.gov.digital.ho.pttg.dto.IncomeSummary;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.time.LocalDate;
import java.util.UUID;

import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_INCOME_REQUEST;

@Slf4j
@RestController
public class HmrcResource {

    private final HmrcClient hmrcClient;
    private final HmrcAccessCodeClient accessCodeClient;
    private final AuditClient auditClient;

    public HmrcResource(HmrcClient hmrcClient, HmrcAccessCodeClient accessCodeClient, AuditClient auditClient) {
        this.hmrcClient = hmrcClient;
        this.accessCodeClient = accessCodeClient;
        this.auditClient = auditClient;
    }

    @RequestMapping(value = "/income", method = RequestMethod.GET, produces = "application/json")
    public IncomeSummary getHmrcData(
            @RequestParam(value = "firstName") String firstName,
            @RequestParam(value = "lastName") String lastName,
            @RequestParam(value = "nino") String nino,
            @RequestParam(value = "dateOfBirth") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
            @RequestParam(value = "fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        final Individual individual = new Individual(firstName, lastName, nino, dob);

        log.info("Hmrc service invoked for nino {} with date range {} to {}", individual.getNino(), fromDate, toDate);

        UUID eventId = UUID.randomUUID();

        auditClient.add(HMRC_INCOME_REQUEST, eventId, auditData(individual));

        log.info("Get the Access Token");

        String accessToken = accessCodeClient.getAccessCode();

        return hmrcClient.getIncome(accessToken, individual, fromDate, toDate);
    }

    private AuditIndividualData auditData(Individual individual) {

        return new AuditIndividualData(
                "get-hmrc-data",
                individual.getNino(),
                individual.getFirstName(),
                individual.getLastName(),
                individual.getDateOfBirth());
    }

}
