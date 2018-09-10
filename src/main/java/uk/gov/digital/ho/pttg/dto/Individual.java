package uk.gov.digital.ho.pttg.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.InvalidIdentityException;

import java.io.Serializable;
import java.time.LocalDate;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@EqualsAndHashCode
public class Individual implements Serializable {
    private String firstName;
    private String lastName;
    private String nino;
    private LocalDate dateOfBirth;

    public Individual(String firstName, String lastName, String nino, LocalDate dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nino = nino;
        this.dateOfBirth = dateOfBirth;

        if (isBlank(firstName) && isBlank(lastName)) {
            throw new InvalidIdentityException("First name and Last name cannot both be empty");
        }
    }
}