package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.pttg.application.NinoUtils;
import uk.gov.digital.ho.pttg.dto.IncomeSummary;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.time.LocalDate;

@Slf4j
@RestController
public class HmrcResource {

    private final IncomeSummaryService incomeSummaryService;
    private final NinoUtils ninoUtils;

    public HmrcResource(final IncomeSummaryService incomeSummaryService, final NinoUtils ninoUtils) {
        this.incomeSummaryService = incomeSummaryService;
        this.ninoUtils = ninoUtils;
    }

    @RequestMapping(value = "/income", method = RequestMethod.GET, produces = "application/json")
    public IncomeSummary getHmrcData(
            @RequestParam(value = "firstName") String firstName,
            @RequestParam(value = "lastName") String lastName,
            @RequestParam(value = "nino") String nino,
            @RequestParam(value = "dateOfBirth") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
            @RequestParam(value = "fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        final String redactedNino = ninoUtils.redactedNino(nino);
        log.info("Hmrc service invoked for nino {} with date range {} to {}", redactedNino, fromDate, toDate);

        final Individual individual = new Individual(firstName, lastName, nino, dob);
        return incomeSummaryService.getIncomeSummary(individual, fromDate, toDate);
    }
}
