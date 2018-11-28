package bdd.steps;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import uk.gov.digital.ho.pttg.application.namematching.NameType;

import java.util.Arrays;
import java.util.Set;

import static java.lang.Character.UnicodeBlock;
import static java.util.stream.Collectors.toSet;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
class MetaDataInputName {

    private NameType nameType;
    private int index;
    private boolean diacritics;
    private boolean umlauts;
    private boolean abbreviation;
    private boolean nameSplitter;

    @Getter(AccessLevel.NONE)
    private String uniCodeBlocks;

    private int length;

    Set<UnicodeBlock> uniCodeBlocks() {

        return Arrays.stream(uniCodeBlocks.split(" "))
                .map(UnicodeBlock::forName)
                .collect(toSet());
    }
}
