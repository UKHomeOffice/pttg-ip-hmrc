package uk.gov.digital.ho.pttg.application.namematching;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import uk.gov.digital.ho.pttg.application.namematching.Name.End;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.InputNamesFunctions.*;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.*;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AbbreviatedNamesFunctions.splitAroundAbbreviatedNames;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.GeneratorFunctions.analyse;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
public class InputNames {

    @JsonProperty(value = "firstNames")
    private List<Name> firstNames;

    @JsonProperty(value = "lastNames")
    private List<Name> lastNames;

    @JsonProperty(value = "aliasSurnames")
    private List<Name> aliasSurnames;

    public InputNames(List<Name> firstNames, List<Name> lastNames, List<Name> aliasSurnames) {
        this.firstNames = firstNames;
        this.lastNames = lastNames;
        this.aliasSurnames = aliasSurnames;
    }

    public InputNames(String firstNames, String lastNames) {
        this(firstNames, lastNames, "");
    }

    public InputNames(String firstNames, String lastNames, String aliasSurnames) {
        this.firstNames = analyse(emptyList(), FIRST, splitIntoDistinctNames(firstNames));
        this.lastNames = analyse(emptyList(), LAST, splitIntoDistinctNames(lastNames));
        this.aliasSurnames = analyse(emptyList(), ALIAS, splitIntoDistinctNames(aliasSurnames));
    }

    public int size() {
        return firstNames.size() + lastNames.size();
    }

    public boolean multiPartLastName() {
        return lastNames.size() > 1;
    }

    public List<String> rawFirstNames() {
        return unmodifiableList(nameStringsOf(firstNames));
    }

    public List<String> rawLastNames() {
        return unmodifiableList(nameStringsOf(lastNames));
    }

    public List<String> rawAllNames() {
        return unmodifiableList(
                Stream.concat(
                        rawAllNonAliasNames().stream(),
                        nameStringsOf(aliasSurnames).stream()).collect(toList()));
    }

    List<String> rawAllNonAliasNames() {
        return unmodifiableList(
                Stream.concat(
                        nameStringsOf(firstNames).stream(),
                        nameStringsOf(lastNames).stream()).collect(toList()));
    }

    public String fullName() {
        return String.join(" ", fullFirstName(), fullLastName());
    }

    public String fullFirstName() {
        return String.join(" ", nameStringsOf(firstNames));
    }

    public String fullLastName() {
        return String.join(" ", nameStringsOf(lastNames));
    }

    public String fullAliasNames() {
        return String.join(" ", nameStringsOf(aliasSurnames));
    }

    public boolean hasAliasSurnames() {
        return !aliasSurnames.isEmpty();
    }

    public InputNames groupByAbbreviatedNames() {
        List<Name> firstNames = analyse(firstNames(), FIRST, splitAroundAbbreviatedNames(this.fullFirstName()));
        List<Name> lastNames = analyse(lastNames(), LAST, splitAroundAbbreviatedNames(this.fullLastName()));
        List<Name> aliasNames = analyse(aliasSurnames, ALIAS, splitAroundAbbreviatedNames(this.fullAliasNames()));

        return new InputNames(firstNames, lastNames, aliasNames);
    }

    private List<Name> allNames() {
        return Stream.concat(Stream.concat(firstNames.stream(), lastNames.stream()), aliasSurnames.stream()).collect(toList());
    }

    public InputNames reduceFirstNames(End end, int amount) {
        return new InputNames(reduceNames(firstNames, end, amount), lastNames, aliasSurnames);
    }

    public InputNames reduceLastNames(End end, int amount) {
        return new InputNames(firstNames, reduceNames(lastNames, end, amount), aliasSurnames);
    }

}
