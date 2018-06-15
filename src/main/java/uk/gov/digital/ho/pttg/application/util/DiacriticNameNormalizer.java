package uk.gov.digital.ho.pttg.application.util;

import me.xuender.unidecode.Unidecode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.digital.ho.pttg.dto.Individual;

public class DiacriticNameNormalizer implements NameNormalizer {

    @Override
    public Individual normalizeNames(Individual individual) {
        String normalizedFirstName = normalizeName(individual.getFirstName());
        String normalizedLastName = normalizeName(individual.getLastName());

        return new Individual(normalizedFirstName, normalizedLastName, individual.getNino(), individual.getDateOfBirth());
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }

        String strippedAccents = StringUtils.stripAccents(name);
        String decoded = Unidecode.decode(strippedAccents);
        return permitOnlyLettersAndWhitespace(decoded);
    }

    private String permitOnlyLettersAndWhitespace(String name) {
        StringBuilder sb = new StringBuilder(name.length());
        for (char c : name.toCharArray()) {
            if (Character.isLetter(c) || Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
