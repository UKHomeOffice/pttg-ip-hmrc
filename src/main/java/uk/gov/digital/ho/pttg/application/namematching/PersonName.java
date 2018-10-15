package uk.gov.digital.ho.pttg.application.namematching;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

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
        String surnameStart = firstNLetters(3, this.lastName);
        return new PersonName(firstInitial, surnameStart);
    }

    private String firstNLetters(int n, String name) {
        int endIndex = 0;
        int significantCharacters = 0;
        for (char letter : name.toCharArray()) {
            endIndex++;
            if (!StringUtils.isWhitespace(String.valueOf(letter))) {
                significantCharacters++;
            }
            if (significantCharacters == n) {
                break;
            }
        }
        return name.substring(0, endIndex);
    }
}
