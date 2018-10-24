package uk.gov.digital.ho.pttg.application.util;

import uk.gov.digital.ho.pttg.dto.IndividualForNameMatching;

import static java.util.Objects.requireNonNull;

public class CompositeNameNormalizer implements NameNormalizer {
    private final NameNormalizer[] nameNormalizers;

    public CompositeNameNormalizer(NameNormalizer[] nameNormalizers) {
        this.nameNormalizers = requireNonNull(nameNormalizers);
    }

    @Override
    public IndividualForNameMatching normalizeNames(IndividualForNameMatching individual) {

        IndividualForNameMatching normalizedIndividual = individual;
        for (NameNormalizer nameNormalizer : nameNormalizers) {
            normalizedIndividual = nameNormalizer.normalizeNames(normalizedIndividual);
        }

        return normalizedIndividual;
    }
}
