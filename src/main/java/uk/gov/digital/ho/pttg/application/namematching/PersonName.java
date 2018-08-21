package uk.gov.digital.ho.pttg.application.namematching;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class PersonName {
    private String firstName;
    private String surname;
}
