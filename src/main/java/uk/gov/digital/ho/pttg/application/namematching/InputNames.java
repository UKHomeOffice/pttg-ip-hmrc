package uk.gov.digital.ho.pttg.application.namematching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.*;
import lombok.experimental.Accessors;
import uk.gov.digital.ho.pttg.application.namematching.Name.End;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.InputNamesFunctions.splitIntoDistinctNames;
import static uk.gov.digital.ho.pttg.application.namematching.Name.End.LEFT;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.*;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NamesWithFullStopSpaceCombinationsFunctions.splitNamesIgnoringFullStopSpace;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    public InputNames(String firstNames, String lastNames) {
        this(firstNames, lastNames, "");
    }

    public InputNames(String firstNames, String lastNames, String aliasSurnames) {
        this.firstNames = analyse(FIRST, firstNames);
        this.lastNames = analyse(LAST, lastNames);
        this.aliasSurnames = analyse(ALIAS, aliasSurnames);
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

    public List<String> rawAliasSurnames() {
        return unmodifiableList(nameStringsOf(aliasSurnames));
    }

    public List<String> allNames() {
        return unmodifiableList(
                Stream.concat(
                        allNonAliasNames().stream(),
                        nameStringsOf(aliasSurnames).stream()).collect(toList()));
    }

    public List<String> allNonAliasNames() {
        return unmodifiableList(
                Stream.concat(
                        nameStringsOf(firstNames).stream(),
                        nameStringsOf(lastNames).stream()).collect(toList()));
    }

    public String fullName() {
        return String.join( " ", fullFirstName(), fullLastName());
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
        List<Name> firstNames = analyseAbbreviatedName(FIRST, splitNamesIgnoringFullStopSpace(this.fullFirstName()));
        List<Name> lastNames = analyseAbbreviatedName(LAST, splitNamesIgnoringFullStopSpace(this.fullLastName()));
        List<Name> aliasNames = analyseAbbreviatedName(ALIAS, splitNamesIgnoringFullStopSpace(this.fullAliasNames()));

        return new InputNames(firstNames, lastNames, aliasNames);
    }

    public InputNames reduceFirstNames(End end, int amount) {
        return new InputNames(reduceNames(firstNames, end, amount), lastNames, aliasSurnames);
    }

    public InputNames reduceLastNames(End end, int amount) {
        return new InputNames(firstNames, reduceNames(lastNames, end, amount), aliasSurnames);
    }

    private List<Name> reduceNames(List<Name> names, End end, int amount) {

        if (end == LEFT) {
            names = Lists.reverse(names);
        }

        List<Name> reducedNames = names.stream()
                                     .limit(amount)
                                     .collect(collectingAndThen(toList(), Collections::unmodifiableList));

        if (end == LEFT) {
            return Lists.reverse(reducedNames);
        }

        return reducedNames;
    }

    private List<Name> analyseAbbreviatedName(NameType nameType, List<String> names) {
        return produceNames(nameType, names);
    }

    private List<Name> analyse(NameType nameType, String names) {
        return produceNames(nameType, splitIntoDistinctNames(names));
    }

    private List<Name> produceNames(NameType nameType, List<String> names) {

        if (names.isEmpty()) {
            return unmodifiableList(emptyList());
        }

        AtomicInteger index = new AtomicInteger(0);

        return names
               .stream()
               .map(name -> new Name(nameType, index.getAndIncrement(), name))
               .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    private List<String> nameStringsOf(List<Name> names) {
        return names.stream()
                       .map(Name::name)
                       .collect(toList());
    }

    public List<Name> combine(List<Name>... namesToCombine) {
        return Stream.of(namesToCombine)
                .flatMap(Collection::stream)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    public int indexOfFirstName(Name name) {
        return firstNames().indexOf(name);
    }

    public int indexOfLastName(Name name) {
        return lastNames().indexOf(name);
    }
}
