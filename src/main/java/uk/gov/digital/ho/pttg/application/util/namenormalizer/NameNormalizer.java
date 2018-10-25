package uk.gov.digital.ho.pttg.application.util.namenormalizer;

import uk.gov.digital.ho.pttg.dto.HmrcIndividual;

public interface NameNormalizer {
    HmrcIndividual normalizeNames(HmrcIndividual individual);
}
