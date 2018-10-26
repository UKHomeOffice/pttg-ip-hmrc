package uk.gov.digital.ho.pttg.application.util.namenormalizer;

import uk.gov.digital.ho.pttg.dto.HmrcIndividual;

import static java.util.Objects.requireNonNull;

public class CompositeNameNormalizer implements NameNormalizer {
    private final NameNormalizer[] nameNormalizers;

    public CompositeNameNormalizer(NameNormalizer[] nameNormalizers) {
        this.nameNormalizers = requireNonNull(nameNormalizers);
    }

    @Override
    public HmrcIndividual normalizeNames(HmrcIndividual individual) {

        HmrcIndividual normalizedIndividual = individual;
        for (NameNormalizer nameNormalizer : nameNormalizers) {
            normalizedIndividual = nameNormalizer.normalizeNames(normalizedIndividual);
        }

        return normalizedIndividual;
    }
}
