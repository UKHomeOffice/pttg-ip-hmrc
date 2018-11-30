package uk.gov.digital.ho.pttg.application.namematching;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.Character.UnicodeBlock;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.DerivationAction.ORIGINAL;
import static uk.gov.digital.ho.pttg.application.namematching.InputNamesFunctions.*;

@Getter
@Accessors(fluent = true)
@ToString
@EqualsAndHashCode(of = "name")
public class Name {

    public enum End {LEFT, RIGHT}

    private String name;

    private NameDerivation derivation;

    @JsonProperty(value = "nameType")
    private NameType nameType;

    @JsonProperty(value = "index")
    private int index;

    @JsonProperty(value = "diacritics")
    private boolean containsDiacritics;

    @JsonProperty(value = "umlauts")
    private boolean containsUmlauts;

    @JsonProperty(value = "abbreviation")
    private boolean abbreviation;

    @JsonProperty(value = "nameSplitter")
    private boolean containsNameSplitter;

    private Set<UnicodeBlock> unicodeBlocks;

    public Name(Optional<NameDerivation> optionalNameDerivation, NameType nameType, int index, String name) {
        this.name = name;
        this.derivation = optionalNameDerivation.orElseGet(() -> new NameDerivation(nameType, singletonList(index), name.length(), singletonList(ORIGINAL)));
        this.nameType = nameType;
        this.index = index;
        this.containsDiacritics = hasDiacritics(name);
        this.containsUmlauts = hasUmlauts(name);
        this.abbreviation = isAbbreviation(name);
        this.containsNameSplitter = hasNameSplitter(name);
        this.unicodeBlocks = calculateUnicodeBlocks(name);
    }

    @JsonProperty(value = "length")
    public int getLength() {
        return name.length();
    }

    @JsonGetter("unicodeBlocks")
    public List<String> getUnicodeBlocks() {
        return unicodeBlocks.stream()
                .map(o -> o.toString())
                .collect(toList());
    }
}
