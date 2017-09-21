package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.pttg.application.HmrcClient;
import uk.gov.digital.ho.pttg.audit.AuditService;
import uk.gov.digital.ho.pttg.dto.IncomeSummary;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_INCOME_REQUEST;
import static uk.gov.digital.ho.pttg.audit.AuditEventType.HMRC_INCOME_RESPONSE;

@Slf4j
@RestController
public class HmrcResource {

    private final HmrcClient client;
    private final AuditService auditService;

    public HmrcResource(HmrcClient client, AuditService auditService) {
        this.client = client;
        this.auditService = auditService;
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

        auditService.add(HMRC_INCOME_REQUEST, eventId, auditData(individual));

        final IncomeSummary incomeSummary = client.getIncome(individual, fromDate, toDate);

        auditService.add(HMRC_INCOME_RESPONSE, eventId, auditData(incomeSummary));

        return incomeSummary;
    }

    private Map<String, Object> auditData(Individual individual) {

        Map<String, Object> auditData = new HashMap<>();

        auditData.put("method", "get-hmrc-data");
        auditData.put("nino", individual.getNino());
        auditData.put("forename", individual.getFirstName());
        auditData.put("surname", individual.getLastName());
        auditData.put("dateOfBirth", individual.getDateOfBirth());

        return auditData;
    }

    private Map<String, Object> auditData(IncomeSummary response) {

        Map<String, Object> auditData = new HashMap<>();

        auditData.put("method", "get-hmrc-data");
        auditData.put("response", response);

        return auditData;
    }

}
