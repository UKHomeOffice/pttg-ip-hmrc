package uk.gov.digital.ho.pttg.application.namematching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.javatuples.Triplet;
import uk.gov.digital.ho.pttg.application.namematching.Name.End;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static uk.gov.digital.ho.pttg.application.namematching.InputNamesFunctions.locate;
import static uk.gov.digital.ho.pttg.application.namematching.InputNamesFunctions.splitIntoDistinctNames;
import static uk.gov.digital.ho.pttg.application.namematching.Name.End.LEFT;
import static uk.gov.digital.ho.pttg.application.namematching.NameType.*;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AbbreviatedNamesFunctions.splitAroundAbbreviatedNames;

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

    private InputNames(List<Name> firstNames, List<Name> lastNames, List<Name> aliasSurnames) {
        this.firstNames = firstNames;
        this.lastNames = lastNames;
        this.aliasSurnames = aliasSurnames;
    }

    public InputNames(String firstNames, String lastNames) {
        this(firstNames, lastNames, "");
    }

    public InputNames(String firstNames, String lastNames, String aliasSurnames) {
        this.firstNames = analyse(FIRST, splitIntoDistinctNames(firstNames));
        this.lastNames = analyse(LAST, splitIntoDistinctNames(lastNames));
        this.aliasSurnames = analyse(ALIAS, splitIntoDistinctNames(aliasSurnames));
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

    public List<String> allNames() {
        return unmodifiableList(
                Stream.concat(
                        allNonAliasNames().stream(),
                        nameStringsOf(aliasSurnames).stream()).collect(toList()));
    }

    List<String> allNonAliasNames() {
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
        List<Name> firstNames = analyse(FIRST, splitAroundAbbreviatedNames(this.fullFirstName()));
        List<Name> lastNames = analyse(LAST, splitAroundAbbreviatedNames(this.fullLastName()));
        List<Name> aliasNames = analyse(ALIAS, splitAroundAbbreviatedNames(this.fullAliasNames()));

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

    private List<Name> analyse(NameType nameType, List<String> names) {

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

    public Triplet<NameType, Integer, DerivationAction> locateName(String rawName) {

        Optional<Triplet<NameType, Integer, DerivationAction>> optionalTuple;

        optionalTuple = locate(rawName, firstNames);

        if (optionalTuple.isPresent()) {
            return optionalTuple.get();
        }

        optionalTuple = locate(rawName, lastNames);

        if (optionalTuple.isPresent()) {
            return optionalTuple.get();
        }

        optionalTuple = locate(rawName, aliasSurnames);

        return optionalTuple.orElse(Triplet.with(null, -1, null));

//        return optionalTuple.orElseThrow(() -> new IllegalArgumentException(String.format("The name %s cannot be located in the input names %s",
//                rawName,
//                String.join(" ", fullName(), fullAliasNames()))));
    }

}
