package uk.gov.digital.ho.pttg.application.namematching;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

import static java.util.Collections.singletonList;
import static uk.gov.digital.ho.pttg.application.namematching.DerivationAction.ENTIRE;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.FIRST;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.LAST;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public class Derivation {

    public static final Derivation ALL_FIRST_NAMES = new Derivation(FIRST, null, null, false, false, singletonList(ENTIRE));
    public static final Derivation ALL_LAST_NAMES = new Derivation(LAST, null, null, false, false, singletonList(ENTIRE));

    @JsonProperty(value = "section")
    private NameType section;

    @JsonProperty(value = "index")
    private List<Integer> index;

    @JsonProperty(value = "originalLength")
    private Integer originalLength;

    @JsonProperty(value = "splittersRemoved")
    private boolean splittersRemoved;

    @JsonProperty(value = "splittersReplaced")
    private boolean splittersReplaced;

    @JsonProperty(value = "derivationActions")
    private List<DerivationAction> derivationActions;

    public Derivation(Name name, DerivationAction derivationAction, boolean splittersRemoved, boolean splittersReplaced) {
        this(name.nameType(),
                singletonList(name.index()),
                name.name().length(),
                splittersRemoved,
                splittersReplaced,
                singletonList(derivationAction));
    }
}
