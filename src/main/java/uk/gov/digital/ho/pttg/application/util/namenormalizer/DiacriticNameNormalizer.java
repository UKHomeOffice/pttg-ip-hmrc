package uk.gov.digital.ho.pttg.application.util.namenormalizer;

import me.xuender.unidecode.Unidecode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.digital.ho.pttg.dto.HmrcIndividual;

public class DiacriticNameNormalizer implements NameNormalizer {

    @Override
    public HmrcIndividual normalizeNames(HmrcIndividual individual) {
        String normalizedFirstName = normalizeName(individual.getFirstName());
        String normalizedLastName = normalizeName(individual.getLastName());

        return new HmrcIndividual(normalizedFirstName, normalizedLastName, individual.getNino(), individual.getDateOfBirth());
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }

        String strippedAccents = StringUtils.stripAccents(name);
        return Unidecode.decode(strippedAccents);
    }
}
