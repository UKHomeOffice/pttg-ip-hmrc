package uk.gov.digital.ho.pttg.audit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.digital.ho.pttg.application.domain.Individual;

import java.time.LocalDate;

@Getter
public class AuditIndividualData {
    public static final String GET_HMRC_DATA_METHOD = "get-hmrc-data";

    @JsonProperty(value = "method", required = true)
    private String method;

    @JsonProperty(value = "nino", required = true)
    private String nino;

    @JsonProperty(value = "forename", required = true)
    private String forename;

    @JsonProperty(value = "surname", required = true)
    private String surname;

    @JsonProperty(value = "dateOfBirth", required = true)
    private LocalDate dateOfBirth;

    public AuditIndividualData(final String method, final Individual individual) {
        this.method = method;
        this.nino = individual.getNino();
        this.forename = individual.getFirstName();
        this.surname = individual.getLastName();
        this.dateOfBirth = individual.getDateOfBirth();
    }
}
