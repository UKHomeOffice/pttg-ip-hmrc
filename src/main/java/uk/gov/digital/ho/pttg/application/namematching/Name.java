package uk.gov.digital.ho.pttg.application.namematching;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Set;

import static uk.gov.digital.ho.pttg.application.namematching.InputNamesFunctions.*;

@Getter
@Accessors(fluent = true)
@ToString
@EqualsAndHashCode(of = "name")
public class Name {

    public enum End {LEFT, RIGHT}

    private String name;

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

    @JsonProperty(value = "unicodeBlocks")
    private Set<String> unicodeBlocks;

    Name(NameType nameType, int index, String name) {
        this.nameType = nameType;
        this.index = index;
        this.name = name;
        this.containsDiacritics = hasDiacritics(name);
        this.containsUmlauts = hasUmlauts(name);
        this.abbreviation = hasFullStopSpace(name);
        this.containsNameSplitter = hasNameSplitter(name);
        this.unicodeBlocks = calculateUnicodeBlocks(name);
    }

    @JsonProperty(value = "length")
    public int getLength() {
        return name.length();
    }
}
