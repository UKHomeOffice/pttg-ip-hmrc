package uk.gov.digital.ho.pttg.application.namematching;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
public class PersonName {
    private String firstName;
    private String lastName;
}
