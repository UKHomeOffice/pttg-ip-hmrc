package uk.gov.digital.ho.pttg.application.util.namenormalizer;

import uk.gov.digital.ho.pttg.dto.HmrcIndividual;

public class TrimmingNameNormalizer implements NameNormalizer {

    @Override
    public HmrcIndividual normalizeNames(HmrcIndividual individual) {
        String trimmedFirstName = individual.getFirstName().trim();
        String trimmedLastName = individual.getLastName().trim();
        return new HmrcIndividual(trimmedFirstName, trimmedLastName, individual.getNino(), individual.getDateOfBirth());
    }
}
