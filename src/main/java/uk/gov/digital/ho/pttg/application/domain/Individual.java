package uk.gov.digital.ho.pttg.application.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.InvalidIdentityException;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@EqualsAndHashCode
public class Individual implements Serializable {
    private String firstName;
    private String lastName;
    private String nino;
    private LocalDate dateOfBirth;
    private String aliasSurnames;

    @ConstructorProperties({"firstName", "lastName", "nino", "dateOfBirth", "aliasSurnames"})
    public Individual(String firstName, String lastName, String nino, LocalDate dateOfBirth, String aliasSurnames) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nino = nino;
        this.dateOfBirth = dateOfBirth;
        this.aliasSurnames = Objects.isNull(aliasSurnames) ? "" : aliasSurnames;

        if (isBlank(firstName) && isBlank(lastName)) {
            throw new InvalidIdentityException("First name and Last name cannot both be empty");
        }
    }
}