package uk.gov.digital.ho.pttg.application.namematching;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

import static java.util.Collections.singletonList;
import static uk.gov.digital.ho.pttg.application.namematching.DerivationAction.ENTIRE;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.FIRST;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.LAST;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public class NameDerivation {

    public static final NameDerivation ALL_FIRST_NAMES = new NameDerivation(FIRST, null, null, singletonList(ENTIRE));
    public static final NameDerivation ALL_LAST_NAMES = new NameDerivation(LAST, null, null, singletonList(ENTIRE));

    @JsonProperty(value = "section")
    private NameType section;

    @JsonProperty(value = "index")
    @Setter
    private List<Integer> index;

    @JsonProperty(value = "length")
    private Integer length;

    @JsonProperty(value = "derivationActions")
    private List<DerivationAction> derivationActions;

    public NameDerivation(Name name) {
        this(name.derivation().section(),
                name.derivation().index(),
                name.name().length(),
                name.derivation().derivationActions());
    }
}
