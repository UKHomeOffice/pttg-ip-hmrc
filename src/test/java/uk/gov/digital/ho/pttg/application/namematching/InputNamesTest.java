package uk.gov.digital.ho.pttg.application.namematching;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class InputNamesTest {

    private static final String SOME_NAME = "some name";

    @Test
    public void multiPartLastNameEmpty() {
        InputNames inputNames = new InputNames("any-name", "");

        assertThat(inputNames.multiPartLastName()).isFalse();
    }

    @Test
    public void multiPartLastNameSingleName() {
        InputNames inputNames = new InputNames("any-name", "any-lastname");

        assertThat(inputNames.multiPartLastName()).isFalse();
    }

    @Test
    public void multiPartLastNameSingleTwoNames() {
        InputNames inputNames = new InputNames("any-name", "lastname1 lastname2");

        assertThat(inputNames.multiPartLastName()).isTrue();
    }

    @Test
    public void multiPartLastNameFirstNameIsIrrelevant() {
        InputNames inputNames = new InputNames("firstname1 firstname2 firstname3", "any-lastname");

        assertThat(inputNames.multiPartLastName()).isFalse();
    }

    @Test
    public void allNonAliasNamesEmptyFirstName() {
        InputNames inputNames = new InputNames("", "lastname");

        List<String> allNames = inputNames.rawAllNonAliasNames();
        
        assertThat(allNames.size()).isEqualTo(1);
        assertThat(allNames.get(0)).isEqualTo("lastname");
    }

    @Test
    public void allNonAliasNamesEmptyLastName() {
        InputNames inputNames = new InputNames("firstname", "");

        List<String> allNames = inputNames.rawAllNonAliasNames();

        assertThat(allNames.size()).isEqualTo(1);
        assertThat(allNames.get(0)).isEqualTo("firstname");
    }

    @Test
    public void allNonAliasNamesEmptyFullName() {
        InputNames inputNames = new InputNames("firstname", "lastname");

        List<String> allNames = inputNames.rawAllNonAliasNames();

        assertThat(allNames.size()).isEqualTo(2);
        assertThat(allNames.get(0)).isEqualTo("firstname");
        assertThat(allNames.get(1)).isEqualTo("lastname");
    }

    @Test
    public void allNonAliasNamesEmptyMultipleName() {
        InputNames inputNames = new InputNames("firstname1 firstname2", "lastname1 lastname2");

        List<String> allNames = inputNames.rawAllNonAliasNames();

        assertThat(allNames.size()).isEqualTo(4);
        assertThat(allNames.get(1)).isEqualTo("firstname2");
        assertThat(allNames.get(3)).isEqualTo("lastname2");
    }
    
    @Test
    public void allNonAliasNamesListConstructor() {
        InputNames inputNames = new InputNames("firstname1 firstname2", "lastname1 lastname2");

        List<String> allNames = inputNames.rawAllNonAliasNames();

        assertThat(allNames.size()).isEqualTo(4);
        assertThat(allNames.get(1)).isEqualTo("firstname2");
        assertThat(allNames.get(3)).isEqualTo("lastname2");
    }
    
    @Test
    public void fullName() {
        InputNames inputNames = new InputNames("firstname1 firstname2", "lastname1 lastname2");

        String fullName = inputNames.fullName();
        
        assertThat(fullName).isEqualTo("firstname1 firstname2 lastname1 lastname2");
    }

    @Test
    public void fullFirstName() {
        InputNames inputNames = new InputNames("firstname1 firstname2", "lastname1 lastname2");

        String firstName = inputNames.fullFirstName();

        assertThat(firstName).isEqualTo("firstname1 firstname2");
    }

    @Test
    public void fullLastName() {
        InputNames inputNames = new InputNames("firstname1 firstname2", "lastname1 lastname2");

        String lastName = inputNames.fullLastName();

        assertThat(lastName).isEqualTo("lastname1 lastname2");
    }

    @Test
    public void allNamesShouldReturnEmptyListWhenNoNames() {
        InputNames inputNames = new InputNames("", "", "");
        assertThat(inputNames.rawAllNames()).isEqualTo(emptyList());
    }

    @Test
    public void allNamesShouldReturnOnlyFirstNameWhenNoOtherNames() {
        InputNames inputNames = new InputNames("John", "", "");
        assertThat(inputNames.rawAllNames()).isEqualTo(singletonList("John"));
    }

    @Test
    public void allNamesShouldReturnOnlyLastNameWhenNoOtherNames() {
        InputNames inputNames = new InputNames("", "Smith", "");
        assertThat(inputNames.rawAllNames()).isEqualTo(singletonList("Smith"));
    }

    @Test
    public void allNamesShouldReturnOnlyAliasSurnameWhenNoOtherNames() {
        InputNames inputNames = new InputNames("", "", "Jones");
        assertThat(inputNames.rawAllNames()).isEqualTo(singletonList("Jones"));
    }

    @Test
    public void allNamesShouldReturnNamesInOrderWhenAllPresent() {
        InputNames inputNames = new InputNames("John", "Smith", "Jones");
        assertThat(inputNames.rawAllNames()).isEqualTo(Arrays.asList("John", "Smith", "Jones"));
    }

    @Test
    public void allNamesShouldReturnNamesInOrderWhenAllPresentAndMultipleWords() {
        InputNames inputNames = new InputNames("John David", "Smith Evans", "Jones McDonald");
        assertThat(inputNames.rawAllNames()).isEqualTo(Arrays.asList("John", "David", "Smith", "Evans", "Jones", "McDonald"));
    }

    @Test
    public void allNamesShouldReturnNamesInOrderWhenUsingListConstructor() {
        InputNames inputNames = new InputNames("John David", "Smith Evans", "Jones McDonald");
        assertThat(inputNames.rawAllNames()).isEqualTo(Arrays.asList("John", "David", "Smith", "Evans", "Jones", "McDonald"));
    }

    @Test
    public void hasAliasSurnamesShouldReturnFalseWhenNoAliasSurnames() {
        InputNames noAliasInputNames = new InputNames("John", "Smith", "");
        assertThat(noAliasInputNames.hasAliasSurnames()).isFalse();
    }

    @Test
    public void hasAliasSurnamesShouldReturnTrueWhenNoAliasSurnames() {
        InputNames noAliasInputNames = new InputNames("John", "Smith", "Evans");
        assertThat(noAliasInputNames.hasAliasSurnames()).isTrue();
    }

    @Test
    public void allAliasSurnamesAsString_emptyAliasNames_emptyString() {
        InputNames emptyAliasNames = new InputNames(SOME_NAME, SOME_NAME, "");
        assertThat(emptyAliasNames.fullAliasNames()).isEmpty();
    }

    @Test
    public void allAliasSurnamesAsString_oneAliasNames_aliasNameReturned() {
        InputNames oneAliasName = new InputNames(SOME_NAME, SOME_NAME, "aliasName");
        assertThat(oneAliasName.fullAliasNames()).isEqualTo("aliasName");
    }

    @Test
    public void allAliasSurnamesAsString_twoAliasNames_aliasNamesJoined() {
        InputNames twoAliasNames = new InputNames(SOME_NAME, SOME_NAME, "aliasName1 aliasName2");
        assertThat(twoAliasNames.fullAliasNames()).isEqualTo("aliasName1 aliasName2");
    }
}
