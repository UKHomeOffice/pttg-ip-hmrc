package uk.gov.digital.ho.pttg.application.namematching;

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

    public enum End {LEFT, RIGHT};

    private NameType nameType;
    private int index;
    private String name;
    private boolean containsDiacritics;
    private boolean containsUmlauts;
    private boolean containsFullStopSpace;
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

}
