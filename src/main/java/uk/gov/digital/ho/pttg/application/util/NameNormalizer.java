package uk.gov.digital.ho.pttg.application.util;

import uk.gov.digital.ho.pttg.dto.HmrcIndividual;

public interface NameNormalizer {
    HmrcIndividual normalizeNames(HmrcIndividual individual);
}
