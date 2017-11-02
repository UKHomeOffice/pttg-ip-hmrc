package uk.gov.digital.ho.pttg.audit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class AuditIndividualData {

    @JsonProperty(value="method", required = true)
    private String method;

    @JsonProperty(value="nino", required = true)
    private String nino;

    @JsonProperty(value="forename", required = true)
    private String forename;

    @JsonProperty(value="surname", required = true)
    private String surname;

    @JsonProperty(value="dateOfBirth", required = true)
    private LocalDate dateOfBirth;
}
