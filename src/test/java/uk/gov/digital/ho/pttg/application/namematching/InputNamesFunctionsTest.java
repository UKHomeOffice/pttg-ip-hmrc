package uk.gov.digital.ho.pttg.application.namematching;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static java.lang.Character.UnicodeBlock.BASIC_LATIN;
import static java.lang.Character.UnicodeBlock.LATIN_EXTENDED_B;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.digital.ho.pttg.application.namematching.InputNamesFunctions.*;

@RunWith(MockitoJUnitRunner.class)
public class InputNamesFunctionsTest {

    @Test
    public void shouldHandleSplittingEmptyNames() {
        assertThat(splitIntoDistinctNames(null)).isEmpty();
        assertThat(splitIntoDistinctNames("")).isEmpty();
        assertThat(splitIntoDistinctNames(" ")).isEmpty();
    }

    @Test
    public void splitIntoDistinctNames_singleName() {
        List<String> splitNames = splitIntoDistinctNames("single");

        assertThat(splitNames.size()).isEqualTo(1);
        assertThat(splitNames.get(0)).isEqualTo("single");
    }

    @Test
    public void splitIntoDistinctNames_twoName() {
        List<String> splitNames = splitIntoDistinctNames("first second");

        assertThat(splitNames.size()).isEqualTo(2);
        assertThat(splitNames.get(0)).isEqualTo("first");
        assertThat(splitNames.get(1)).isEqualTo("second");
    }

    @Test
    public void splitIntoDistinctNames_severalNames() {
        List<String> splitNames = splitIntoDistinctNames("first second third fourth fifth sixth");

        assertThat(splitNames.size()).isEqualTo(6);
        assertThat(splitNames.get(0)).isEqualTo("first");
        assertThat(splitNames.get(1)).isEqualTo("second");
        assertThat(splitNames.get(4)).isEqualTo("fifth");
        assertThat(splitNames.get(5)).isEqualTo("sixth");
    }

    @Test
    public void shouldIdentifyAbbreviation() {
        assertThat(isAbbreviation(".abcdef")).isTrue();
        assertThat(isAbbreviation("abcdef.")).isTrue();
        assertThat(isAbbreviation("abc.def")).isTrue();
        assertThat(isAbbreviation("a. ")).isTrue();
        assertThat(isAbbreviation("a. b")).isTrue();
    }

    @Test
    public void shouldLocateNameSplitter() {
        assertThat(hasNameSplitter("'abcdef")).isTrue();
        assertThat(hasNameSplitter("-abcdef")).isTrue();
        assertThat(hasNameSplitter(".abcdef")).isTrue();

        assertThat(hasNameSplitter("abc'def")).isTrue();
        assertThat(hasNameSplitter("abc-def")).isTrue();
        assertThat(hasNameSplitter("abc.def")).isTrue();

        assertThat(hasNameSplitter("abcdef'")).isTrue();
        assertThat(hasNameSplitter("abcdef-")).isTrue();
        assertThat(hasNameSplitter("abcdef.")).isTrue();
    }

    @Test
    public void shouldLocateDiacritics() {
        assertThat(hasDiacritics("\u00C0abcdef")).isTrue();
        assertThat(hasDiacritics("abc\u00C0def")).isTrue();
        assertThat(hasDiacritics("abcdef\u00C0")).isTrue();
    }

    @Test
    public void shouldLocateUmlauts() {
        assertThat(hasDiacritics("\u00CBabcdef")).isTrue();
        assertThat(hasDiacritics("abc\u00CBdef")).isTrue();
        assertThat(hasDiacritics("abcdef\u00CB")).isTrue();
    }

    @Test
    public void shouldLocateBasicLatinUnicodeBlock() {
        assertThat(calculateUnicodeBlocks("abcdef")).containsExactly(BASIC_LATIN);
    }

    @Test
    public void shouldLocateOtherUnicodeBlock() {
        assertThat(calculateUnicodeBlocks("Ǽabcdef")).containsExactlyInAnyOrder(BASIC_LATIN, LATIN_EXTENDED_B);
    }

    @Test
    public void shouldCombineSingleList() {
        Name nameA = new Name(Optional.empty(), NameType.ALIAS, 99, "some name");

        assertThat(combine(asList(nameA))).containsExactly(nameA);
    }

    @Test
    public void shouldCombineAsImmutableList() {
        Name nameA = new Name(Optional.empty(), NameType.ALIAS, 99, "some name");

        List<Name> combinedList = combine(asList(nameA));


        assertThatThrownBy(() -> combinedList.add(nameA))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void shouldCombineMultipleLists() {
        Name nameA = new Name(Optional.empty(), NameType.ALIAS, 99, "some name");
        Name nameB = new Name(Optional.empty(), NameType.FIRST, 1, "some other name");

        assertThat(combine(asList(nameA), asList(nameB))).containsExactly(nameA, nameB);
    }


}
