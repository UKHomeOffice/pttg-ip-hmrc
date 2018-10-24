package uk.gov.digital.ho.pttg.application.util;

import me.xuender.unidecode.Unidecode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.digital.ho.pttg.dto.IndividualForNameMatching;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DiacriticNameNormalizer implements NameNormalizer {

    private static final Set<Character> JOINERS = new HashSet<>(Arrays.asList('-', '\''));

    @Override
    public IndividualForNameMatching normalizeNames(IndividualForNameMatching individual) {
        String normalizedFirstName = normalizeName(individual.getFirstName());
        String normalizedLastName = normalizeName(individual.getLastName());

        return new IndividualForNameMatching(normalizedFirstName, normalizedLastName, individual.getNino(), individual.getDateOfBirth());
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }

        String strippedAccents = StringUtils.stripAccents(name);
        String decoded = Unidecode.decode(strippedAccents);
        return permitOnlyLettersAndWhitespaceAndJoiners(decoded);
    }

    private String permitOnlyLettersAndWhitespaceAndJoiners(String name) {

        StringBuilder sb = new StringBuilder(name.length());

        for (char c : name.toCharArray()) {
            if (Character.isLetter(c) || Character.isWhitespace(c) || JOINERS.contains(c)) {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
