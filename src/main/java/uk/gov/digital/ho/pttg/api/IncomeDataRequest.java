package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.time.LocalDate;

import static java.util.Objects.isNull;

@Getter
@Accessors(fluent = true)
class IncomeDataRequest {

    private static final String FIRSTNAME_KEY = "firstName";
    private static final String LASTNAME_KEY = "lastName";
    private static final String NINO_KEY = "nino";
    private static final String DATEOFBIRTH_KEY = "dateOfBirth";
    private static final String FROMDATE_KEY = "fromDate";
    private static final String TODATE_KEY = "toDate";
    private static final String ALIASSURNAMES_KEY = "aliasSurnames";

    @JsonProperty(value = FIRSTNAME_KEY)
    private String firstName;

    @JsonProperty(value = LASTNAME_KEY)
    private String lastName;

    @JsonProperty(value = NINO_KEY)
    private String nino;

    @JsonProperty(value = DATEOFBIRTH_KEY)
    private LocalDate dateOfBirth;

    @JsonProperty(value = FROMDATE_KEY)
    private LocalDate fromDate;

    @JsonProperty(value = TODATE_KEY)
    private LocalDate toDate;

    @JsonProperty(value = ALIASSURNAMES_KEY)
    private String aliasSurnames;

    @JsonCreator
    IncomeDataRequest(
            @JsonProperty(value = FIRSTNAME_KEY, required = true) @NonNull String firstName,
            @JsonProperty(value = LASTNAME_KEY, required = true) @NonNull String lastName,
            @JsonProperty(value = NINO_KEY, required = true) @NonNull String nino,
            @JsonProperty(value = DATEOFBIRTH_KEY, required = true) @NonNull LocalDate dateOfBirth,
            @JsonProperty(value = FROMDATE_KEY, required = true) @NonNull LocalDate fromDate,
            @JsonProperty(value = TODATE_KEY, required = true) @NonNull LocalDate toDate,
            @JsonProperty(value = ALIASSURNAMES_KEY) String aliasSurnames
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nino = nino;
        this.dateOfBirth = dateOfBirth;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.aliasSurnames = isNull(aliasSurnames) ? "" : aliasSurnames;
    }

}
