package uk.gov.digital.ho.pttg.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.pttg.application.NinoUtils;
import uk.gov.digital.ho.pttg.application.domain.IncomeSummary;
import uk.gov.digital.ho.pttg.application.domain.Individual;

import java.time.LocalDate;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.digital.ho.pttg.api.RequestHeaderData.POOL_SIZE;
import static uk.gov.digital.ho.pttg.api.RequestHeaderData.REQUEST_DURATION_MS;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@Slf4j
@RestController
class HmrcResource {

    private final IncomeSummaryService incomeSummaryService;
    private final NinoUtils ninoUtils;
    private final RequestHeaderData requestHeaderData;

    HmrcResource(IncomeSummaryService incomeSummaryService, NinoUtils ninoUtils, RequestHeaderData requestHeaderData) {
        this.incomeSummaryService = incomeSummaryService;
        this.ninoUtils = ninoUtils;
        this.requestHeaderData = requestHeaderData;
    }

    @PostMapping(value="/income", produces = APPLICATION_JSON_VALUE)
    IncomeSummary getHmrcData(@RequestBody IncomeDataRequest incomeDataRequest) {

        IncomeSummary incomeSummary = produceIncomeSummary(
                individual(
                        incomeDataRequest.firstName(),
                        incomeDataRequest.lastName(),
                        incomeDataRequest.nino(),
                        incomeDataRequest.dateOfBirth(),
                        incomeDataRequest.aliasSurnames()),
                incomeDataRequest.fromDate(),
                incomeDataRequest.toDate());

        return incomeSummary;
    }

    private IncomeSummary produceIncomeSummary(Individual individual, LocalDate fromDate, LocalDate toDate) {

        log.info("Hmrc service invoked for nino {} with date range {} to {}", individual.getNino(), fromDate, toDate, value(EVENT, HMRC_SERVICE_REQUEST_RECEIVED));

        IncomeSummary incomeSummary = incomeSummaryService.getIncomeSummary(individual, fromDate, toDate);

        log.info("Income summary successfully retrieved from HMRC",
                value(EVENT, HMRC_SERVICE_RESPONSE_SUCCESS),
                value(REQUEST_DURATION_MS, requestHeaderData.calculateRequestDuration()),
                value(POOL_SIZE, requestHeaderData.poolSize())
                );

        return incomeSummary;
    }

    private Individual individual(String firstName, String lastName, String nino, LocalDate dob, String aliasSurnames) {
        String sanitisedNino = ninoUtils.sanitise(nino);
        if (!requestHeaderData.isASmokeTest()) {
            ninoUtils.validate(sanitisedNino);
        }
        return new Individual(firstName, lastName, sanitisedNino, dob, aliasSurnames);
    }
}
