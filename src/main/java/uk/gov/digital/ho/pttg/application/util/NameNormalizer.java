package uk.gov.digital.ho.pttg.application.util;

import uk.gov.digital.ho.pttg.dto.IndividualForNameMatching;

public interface NameNormalizer {
    IndividualForNameMatching normalizeNames(IndividualForNameMatching individual);
}
