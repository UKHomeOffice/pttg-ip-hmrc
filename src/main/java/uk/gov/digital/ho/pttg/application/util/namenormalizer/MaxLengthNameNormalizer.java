package uk.gov.digital.ho.pttg.application.util.namenormalizer;

import uk.gov.digital.ho.pttg.dto.HmrcIndividual;

public class MaxLengthNameNormalizer implements NameNormalizer {
    private final int nameMaxLength;

    public MaxLengthNameNormalizer(int nameMaxLength) {
        this.nameMaxLength = nameMaxLength;
    }

    @Override
    public HmrcIndividual normalizeNames(HmrcIndividual individual) {
        String truncatedFirstName = truncateFirstName(individual);
        String truncatedLastName = truncateLastName(individual);

        return new HmrcIndividual(truncatedFirstName, truncatedLastName, individual.getNino(), individual.getDateOfBirth());
    }

    private String truncateFirstName(HmrcIndividual individual) {
        return truncateName(individual.getFirstName());
    }

    private String truncateLastName(HmrcIndividual individual) {
        return truncateName(individual.getLastName());
    }

    private String truncateName(String name) {
        if (shouldBeTruncated(name)) {
            return truncate(name);
        }

        return name;
    }

    private boolean shouldBeTruncated(String str) {
        return (str != null) && str.length() > nameMaxLength;
    }

    private String truncate(String str) {
        return str.substring(0, nameMaxLength);
    }
}
