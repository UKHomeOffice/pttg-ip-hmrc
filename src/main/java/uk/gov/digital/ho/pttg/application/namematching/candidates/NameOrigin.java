package uk.gov.digital.ho.pttg.application.namematching.candidates;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import uk.gov.digital.ho.pttg.application.namematching.DerivationAction;
import uk.gov.digital.ho.pttg.application.namematching.NameType;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public class NameOrigin {

    private NameType nameType;
    private Integer position;
    private DerivationAction derivationAction;
}
