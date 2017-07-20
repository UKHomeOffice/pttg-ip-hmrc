package uk.gov.digital.ho.pttg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.pttg.dto.IncomeSummary;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.time.LocalDate;

@Slf4j
@RestController
public class HmrcResource {

    private final HmrcClient client;

    @Autowired
    public HmrcResource(HmrcClient client) {
        this.client = client;
    }

    @RequestMapping(value = "/income", method = RequestMethod.GET, produces = "application/json")
    public IncomeSummary getHmrcData(
            @RequestParam(value = "firstName") String firstName,
            @RequestParam(value = "lastName")  String lastName,
            @RequestParam(value = "nino")  String nino,
            @RequestParam(value = "dateOfBirth")  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
            @RequestParam(value = "fromDate")  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false)  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate)
    {


        final Individual individual = new Individual(firstName, lastName, nino, dob);
        log.info(String.format("Hmrc service invoked for Individual %s with date range %s to %s", individual, fromDate, toDate));
        return client.getIncome(individual, fromDate, toDate);
    }
}
