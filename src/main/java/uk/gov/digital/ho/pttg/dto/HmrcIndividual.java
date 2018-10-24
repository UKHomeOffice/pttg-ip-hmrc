package uk.gov.digital.ho.pttg.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class HmrcIndividual implements Serializable {
    private final String firstName;
    private final String lastName;
    private final String nino;
    private final LocalDate dateOfBirth;
}