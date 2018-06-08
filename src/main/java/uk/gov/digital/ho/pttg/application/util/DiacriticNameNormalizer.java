package uk.gov.digital.ho.pttg.application.util;

import org.apache.commons.lang3.StringUtils;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.io.File;
import java.net.URL;

import static java.lang.Character.UnicodeBlock.BASIC_LATIN;
import static java.util.Objects.requireNonNull;

public class DiacriticNameNormalizer implements NameNormalizer {
    private static final String UNICODE_MAPPING_CSV_FILENAME = "unicode_map.csv";
    private static final UnicodeMapping UNICODE_MAPPING = UnicodeMapping.fromFile(getUnicodeMappingFile());

    private static File getUnicodeMappingFile() {
        ClassLoader classLoader = DiacriticNameNormalizer.class.getClassLoader();
        URL resource = requireNonNull(classLoader.getResource(UNICODE_MAPPING_CSV_FILENAME));
        return new File(resource.getFile());
    }

    @Override
    public Individual normalizeNames(Individual individual) {
        String normalizedFirstName = toBasicLatin(individual.getFirstName());
        String normalizedLastName = toBasicLatin(individual.getLastName());

        return new Individual(normalizedFirstName, normalizedLastName, individual.getNino(), individual.getDateOfBirth());
    }

    private String toBasicLatin(String name) {
        String replacedCharactersFromMapping = replaceCharactersFromMapping(name);
        String nameWithoutAccents = StringUtils.stripAccents(replacedCharactersFromMapping);
        return removeNonBasicLatinCharacters(nameWithoutAccents);
    }

    private String replaceCharactersFromMapping(String name) {
        if (name == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder(name.length());
        for (char character : name.toCharArray()) {
            boolean characterHasReplacement = UNICODE_MAPPING.contains(character);
            if (characterHasReplacement) {
                String replacementString = UNICODE_MAPPING.get(character);
                stringBuilder.append(replacementString);
            } else {
                stringBuilder.append(character);
            }
        }

        return stringBuilder.toString();
    }

    private String removeNonBasicLatinCharacters(String name) {
        if (name == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder(name.length());
        for (char c : name.toCharArray()) {
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
