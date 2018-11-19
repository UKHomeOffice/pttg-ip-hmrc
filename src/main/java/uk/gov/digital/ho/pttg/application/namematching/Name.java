package uk.gov.digital.ho.pttg.application.namematching;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

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

    @JsonProperty(value = "fullStopSpace")
    private boolean containsFullStopSpace;

    @JsonProperty(value = "nameSplitter")
    private boolean containsNameSplitter;

    private boolean containsSplitters;

    Name(NameType nameType, int index, String name) {
        this.nameType = nameType;
        this.index = index;
        this.name = name;
        this.containsDiacritics = hasDiacritics(name);
        this.containsUmlauts = hasUmnlauts(name);
        this.containsFullStopSpace = hasFullStopSpace(name);
        this.containsNameSplitter = hasNameSplitter(name);
        this.containsSplitters = containsFullStopSpace || containsNameSplitter;
    }

    @JsonProperty(value = "length")
    public int getLength() {
        return name.length();
    }
}
