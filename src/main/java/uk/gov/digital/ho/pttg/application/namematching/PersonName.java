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

    PersonName hmrcNameMatchingEquivalent() {
        String firstInitial = firstNLetters(1, this.firstName);
        String surnameFirstThreeLetters = firstNLetters(3, this.lastName);
        return new PersonName(firstInitial, surnameFirstThreeLetters);
    }

    private String firstNLetters(int n, String string) {
        return string.substring(0, Math.min(string.length(), n));
    }
}
