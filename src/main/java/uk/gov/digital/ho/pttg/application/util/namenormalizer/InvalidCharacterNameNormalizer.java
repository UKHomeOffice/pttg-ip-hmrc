package uk.gov.digital.ho.pttg.application.util.namenormalizer;

import uk.gov.digital.ho.pttg.dto.HmrcIndividual;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InvalidCharacterNameNormalizer implements NameNormalizer {

    private static final Set<Character> JOINERS = new HashSet<>(Arrays.asList('-', '\'', '.'));

    @Override
    public HmrcIndividual normalizeNames(HmrcIndividual individual) {
        String firstName = permitOnlyLettersAndWhitespaceAndJoiners(individual.getFirstName());
        String lastName = permitOnlyLettersAndWhitespaceAndJoiners(individual.getLastName());
        return new HmrcIndividual(firstName, lastName, individual.getNino(), individual.getDateOfBirth());
    }

    private String permitOnlyLettersAndWhitespaceAndJoiners(String name) {
        if (name == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(name.length());

        for (char c : name.toCharArray()) {
            if (Character.isLetter(c) || Character.isWhitespace(c) || JOINERS.contains(c)) {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
