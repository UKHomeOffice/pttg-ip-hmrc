package uk.gov.digital.ho.pttg.application.util;

import org.apache.commons.lang3.StringUtils;
import uk.gov.digital.ho.pttg.dto.Individual;

import static java.lang.Character.UnicodeBlock.BASIC_LATIN;

public class DiacriticNameNormalizer implements NameNormalizer {
    @Override
    public Individual normalizeNames(Individual individual) {
        String normalizedFirstName = toBasicLatin(individual.getFirstName());
        String normalizedLastName = toBasicLatin(individual.getLastName());

        return new Individual(normalizedFirstName, normalizedLastName, individual.getNino(), individual.getDateOfBirth());
    }

    private String toBasicLatin(String name) {
        String nameWithoutAccents = StringUtils.stripAccents(name);
        return permitOnlyBasicLatinCharacters(nameWithoutAccents);
    }

    private String permitOnlyBasicLatinCharacters(String str) {
        if (str == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (isAllowedCharacter(c)) {
                stringBuilder.append(c);
            }
        }

        return stringBuilder.toString();
    }

    private boolean isAllowedCharacter(char c) {
        return Character.UnicodeBlock.of(c).equals(BASIC_LATIN);
    }
}
