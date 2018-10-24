package bdd.steps;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.pttg.application.domain.Individual;

import java.time.LocalDate;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_DATE;

@AllArgsConstructor
@Getter
class IndividualRow {

    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String nino;

    Individual toIndividual() {
        return new Individual(firstName, lastName, nino, LocalDate.parse(dateOfBirth, ISO_DATE), "");
    }

    static IndividualRow fromMap(Map<String, String> individualMap) {
        String firstName = individualMap.get("First name");
        String lastName = individualMap.get("Last name");
        String dateOfBirth = individualMap.get("Date of Birth");
        String nino = individualMap.get("nino");

        return new IndividualRow(firstName, lastName, dateOfBirth, nino);
    }
}
