package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
class IncomeDataRequest {

    @JsonProperty(value = "firstName", required = true)
    private String firstName;

    @JsonProperty(value = "lastName", required = true)
    private String lastName;

    @JsonProperty(value = "nino", required = true)
    private String nino;

    @JsonProperty(value = "dateOfBirth", required = true)
    private LocalDate dateOfBirth;

    @JsonProperty(value = "fromDate", required = true)
    private LocalDate fromDate;

    @JsonProperty(value = "toDate", required = true)
    private LocalDate toDate;
}
