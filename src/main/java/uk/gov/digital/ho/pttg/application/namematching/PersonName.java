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
        String surnameFirstThreeSignificantCharacters = firstNLetters(3, this.lastName);
        return new PersonName(firstInitial, surnameFirstThreeSignificantCharacters);
    }

    private String firstNLetters(int n, String name) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            stringBuilder.append(name, i, i + 1);
            if (countSignificantCharacters(stringBuilder.toString()) >= n) {
                break;
            }
        }
        return stringBuilder.toString();
    }

    private long countSignificantCharacters(String name) {
        int total = 0;
        for (char character : name.toCharArray()) {
            if (!StringUtils.isWhitespace(String.valueOf(character))) {
                total++;
            }
        }
        return total;
    }
}
