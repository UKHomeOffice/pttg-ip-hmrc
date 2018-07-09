package acceptance.rows

import uk.gov.digital.ho.pttg.dto.Individual

import java.time.LocalDate

import static java.time.format.DateTimeFormatter.ISO_DATE

class IndividualRow {
    String firstName
    String lastName
    String dateOfBirth
    String nino

    IndividualRow(String firstName, String lastName, String dateOfBirth, String nino) {
        this.firstName = firstName
        this.lastName = lastName
        this.dateOfBirth = dateOfBirth
        this.nino = nino
    }

    Individual toIndividual() {
        new Individual(firstName, lastName, nino, LocalDate.parse(dateOfBirth, ISO_DATE))
    }

    static IndividualRow fromMap(Map<String, String> individualMap) {
        def firstName = individualMap.get("First name")
        def lastName = individualMap.get("Last name")
        def dateOfBirth = individualMap.get("Date of Birth")
        def nino = individualMap.get("nino")

        new IndividualRow(firstName, lastName, dateOfBirth, nino)
    }
}
