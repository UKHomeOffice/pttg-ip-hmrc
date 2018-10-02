package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.pttg.application.NinoUtils;
import uk.gov.digital.ho.pttg.dto.IncomeSummary;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.time.LocalDate;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@Slf4j
@RestController
public class HmrcResource {

    private final IncomeSummaryService incomeSummaryService;
    private final NinoUtils ninoUtils;

    public HmrcResource(IncomeSummaryService incomeSummaryService, NinoUtils ninoUtils) {
        this.incomeSummaryService = incomeSummaryService;
        this.ninoUtils = ninoUtils;
    }

    @GetMapping(value = "/income", produces = APPLICATION_JSON_VALUE)
    public IncomeSummary getHmrcData(
            @RequestParam(value = "firstName") String firstName,
            @RequestParam(value = "lastName") String lastName,
            @RequestParam(value = "nino") String nino,
            @RequestParam(value = "dateOfBirth") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
            @RequestParam(value = "fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        return produceIncomeSummary(
                individual(
                        firstName,
                        lastName,
                        nino,
                        dob),
                fromDate,
                toDate);
    }

    @PostMapping(value="/income", produces = APPLICATION_JSON_VALUE)
    public IncomeSummary getHmrcData(@RequestBody IncomeDataRequest incomeDataRequest) {

        return produceIncomeSummary(
                individual(
                        incomeDataRequest.firstName(),
                        incomeDataRequest.lastName(),
                        incomeDataRequest.nino(),
                        incomeDataRequest.dateOfBirth()),
                incomeDataRequest.fromDate(),
                incomeDataRequest.toDate());
    }

    private IncomeSummary produceIncomeSummary(Individual individual, LocalDate fromDate, LocalDate toDate) {

        log.info("Hmrc service invoked for nino {} with date range {} to {}", individual.getNino(), fromDate, toDate, value(EVENT, HMRC_SERVICE_REQUEST_RECEIVED));

        IncomeSummary incomeSummary = incomeSummaryService.getIncomeSummary(individual, fromDate, toDate);

        log.info("Income summary successfully retrieved from HMRC", value(EVENT, HMRC_SERVICE_RESPONSE_SUCCESS));

        return incomeSummary;
    }

    private Individual individual(String firstName, String lastName, String nino, LocalDate dob) {
        String sanitisedNino = ninoUtils.sanitise(nino);
        ninoUtils.validate(sanitisedNino);
        return new Individual(firstName, lastName, sanitisedNino, dob);
    }
}
