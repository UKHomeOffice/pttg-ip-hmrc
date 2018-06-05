package uk.gov.digital.ho.pttg.application.util;

import uk.gov.digital.ho.pttg.dto.Individual;

public interface NameNormalizer {
    Individual normalizeNames(Individual individual);
}
