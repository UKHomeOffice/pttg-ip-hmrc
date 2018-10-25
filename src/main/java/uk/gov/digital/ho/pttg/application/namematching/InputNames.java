package uk.gov.digital.ho.pttg.application.namematching;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static uk.gov.digital.ho.pttg.application.namematching.InputNamesFunctions.splitIntoDistinctNames;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode
@ToString
public class InputNames {

    private List<String> firstNames;
    private List<String> lastNames;
    private List<String> aliasSurnames;

    public InputNames(List<String> firstNames, List<String> lastNames) {
        this(firstNames, lastNames, emptyList());
    }

    public InputNames(String firstNames, String lastNames) {
        this(firstNames, lastNames, "");
    }

    public InputNames(String firstNames, String lastNames, String aliasSurnames) {
        this.firstNames = splitIntoDistinctNames(firstNames);
        this.lastNames = splitIntoDistinctNames(lastNames);
        this.aliasSurnames = splitIntoDistinctNames(aliasSurnames);
    }

    public int size() {
        return firstNames.size() + lastNames.size();
    }

    public boolean multiPartLastName() {
        return lastNames.size() > 1;
    }

    public List<String> allNames() {
        return Collections.unmodifiableList(Stream.concat(allNonAliasNames().stream(), aliasSurnames.stream()).collect(Collectors.toList()));
    }

    public List<String> allNonAliasNames() {
        return Collections.unmodifiableList(Stream.concat(firstNames.stream(), lastNames.stream()).collect(Collectors.toList()));
    }

    public String fullName() {
        return String.join( " ", fullFirstName(), fullLastName());
    }

    public String fullFirstName() {
        return String.join(" ", firstNames);
    }

    public String fullLastName() {
        return String.join(" ", lastNames);
    }

    public boolean hasAliasSurnames() {
        return !aliasSurnames.isEmpty();
    }
}
