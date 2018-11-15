package uk.gov.digital.ho.pttg.application.namematching;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

import static java.util.Collections.singletonList;
import static uk.gov.digital.ho.pttg.application.namematching.DerivationAction.ENTIRE;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.*;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
public class Derivation {

    public static final Derivation ALL_FIRST_NAMES = new Derivation(FIRST, null, null, false, false, false, false, singletonList(ENTIRE));
    public static final Derivation ALL_LAST_NAMES = new Derivation(LAST, null, null, false, false, false, false, singletonList(ENTIRE));
    public static final Derivation ALL_ALIAS = new Derivation(ALIAS, null, null, false, false, false, false, singletonList(ENTIRE));

    @JsonProperty(value = "section")
    private NameType section;

    @JsonProperty(value = "index")
    private List<Integer> index;

    @JsonProperty(value = "originalLength")
    private Integer originalLength;

    @JsonProperty(value = "containsDiacritics")
    private boolean containsDiacritics;

    @JsonProperty(value = "containsUmlauts")
    private boolean containsUmlauts;

    @JsonProperty(value = "containsFullStopSpace")
    private boolean containsFullStopSpace;

    @JsonProperty(value = "containsNameSplitter")
    private boolean containsNameSplitter;

    @JsonProperty(value = "derivationActions")
    private List<DerivationAction> derivationActions;

    public Derivation(Name name, DerivationAction derivationAction) {
        this(name.nameType(),
                singletonList(name.index()),
                name.name().length(),
                name.containsDiacritics(),
                name.containsUmlauts(),
                name.containsFullStopSpace(),
                name.containsNameSplitter(),
                singletonList(derivationAction));
    }
}
