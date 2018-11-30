package uk.gov.digital.ho.pttg.application.namematching;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import static java.util.Collections.singletonList;
import static uk.gov.digital.ho.pttg.application.namematching.NameDerivation.ALL_FIRST_NAMES;
import static uk.gov.digital.ho.pttg.application.namematching.NameDerivation.ALL_LAST_NAMES;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(of = {"firstName", "lastName"})
@ToString
public class CandidateName {

    private String firstName;
    private String lastName;
    private CandidateDerivation derivation;

    // TODO: This constructor is a temporary measure to facilitate this refactor! It is only used in tests
    public CandidateName(String firstName, String lastName) {
        this(firstName,
                lastName,
                new CandidateDerivation(
                        null,
                        singletonList(null),
                        ALL_FIRST_NAMES,
                        ALL_LAST_NAMES));
    }

    CandidateName hmrcNameMatchingEquivalent() {
        String firstInitial = firstNLetters(1, this.firstName);
        String surnameStart = firstNLetters(3, this.lastName);
        return new CandidateName(firstInitial, surnameStart, derivation);
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
