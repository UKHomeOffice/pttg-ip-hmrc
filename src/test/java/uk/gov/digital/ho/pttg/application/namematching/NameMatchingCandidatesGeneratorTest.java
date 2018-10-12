package uk.gov.digital.ho.pttg.application.namematching;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(MockitoJUnitRunner.class)
public class NameMatchingCandidatesGeneratorTest {

    private static final String INCORRECT_ORDER = "The names should be correctly generated in the defined order";
    private static final String INCORRECT_NUMBER_OF_GENERATED_NAMES = "The number of generated names should be as expected";
    private static final String DEDUPLICATION = "The name should be removed by deduplication";

    @Test(expected = IllegalArgumentException.class)
    public void shouldErrorIfNoName() {
        NameMatchingCandidatesGenerator.generateCandidateNames("", "");
    }

    @Test
    public void shouldDuplicateSingleFirstName() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Mono", "");

        assertThat("There should be a single name", names.size(), is(1));
        assertThat("The first name should be duplicated", names.get(0), is(new PersonName("Mono", "Mono")));
    }

    @Test
    public void shouldDuplicateSingleLastName() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("", "Mono");

        assertThat("There should be a single name", names.size(), is(1));
        assertThat("The last name should be duplicated", names.get(0), is(new PersonName("Mono", "Mono")));
    }

    @Test
    public void shouldSwitchSingleFirstAndLastName() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Arthur", "Bobbins");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(2));
        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur", "Bobbins")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("Bobbins", "Arthur")));
    }

    @Test
    public void shouldTryAllCombinationsOfTwoFirstNames() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Arthur Brian", "Coates");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(6));
        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("Coates", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("Brian", "Arthur")));
    }

    @Test
    public void shouldTryAllCombinationsOfThreeFirstNames() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Arthur Brian Chris", "Doom");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(12));
        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur", "Doom")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("Brian", "Doom")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("Chris", "Doom")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("Arthur", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("Brian", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new PersonName("Chris", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(7), is(new PersonName("Doom", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(8), is(new PersonName("Brian", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(9), is(new PersonName("Chris", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(10), is(new PersonName("Doom", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(11), is(new PersonName("Doom", "Chris")));
    }

    @Test
    public void shouldTryAllCombinationsOfFourFirstNames() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Arthur Brian Chris Daniel", "Eccles");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(20));
        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur", "Eccles")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("Brian", "Eccles")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("Chris", "Eccles")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Daniel", "Eccles")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("Arthur", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new PersonName("Arthur", "Daniel")));
        assertThat(INCORRECT_ORDER, names.get(7), is(new PersonName("Brian", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(8), is(new PersonName("Brian", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(9), is(new PersonName("Brian", "Daniel")));
        assertThat(INCORRECT_ORDER, names.get(10), is(new PersonName("Chris", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(11), is(new PersonName("Chris", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(12), is(new PersonName("Chris", "Daniel")));
        assertThat(INCORRECT_ORDER, names.get(13), is(new PersonName("Daniel", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(14), is(new PersonName("Daniel", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(15), is(new PersonName("Daniel", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(16), is(new PersonName("Eccles", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(17), is(new PersonName("Eccles", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(18), is(new PersonName("Eccles", "Chris")));
        assertThat(INCORRECT_ORDER, names.get(19), is(new PersonName("Eccles", "Daniel")));
    }

    @Test
    public void shouldHandleExtraWhitespace() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames(" Arthur   Brian   ", " Coates ");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(6));
        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("Coates", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("Brian", "Arthur")));
    }

    @Test
    public void shouldHandleMultipleLastNames() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Arthur", "Brian Coates");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(6));
        assertThat("The lastname is used unsplit for the first permutation", names.get(0), is(new PersonName("Arthur", "Brian Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("Arthur", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Coates", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("Brian", "Arthur")));

        assertThat(DEDUPLICATION, names, not(contains(new PersonName("Arthur", "Brian"))));

    }

    @Test
    public void shouldHandleHyphenatedNames() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Arthur-Brian", "Coates");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(6));
        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur-Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("Coates", "Arthur-Brian")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("Brian", "Arthur")));

        assertThat(DEDUPLICATION, names, not(contains(new PersonName("ArthurBrian", "Coates"))));
        assertThat(DEDUPLICATION, names, not(contains(new PersonName("Coates", "ArthurBrian"))));
        assertThat(DEDUPLICATION, names, not(contains(new PersonName("Arthur", "Coates"))));
        assertThat(DEDUPLICATION, names, not(contains(new PersonName("Coates", "Arthur"))));
    }

    @Test
    public void shouldHandleApostrophedNames() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Arthur", "O'Bobbins");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(9));
        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur", "O'Bobbins")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("O'Bobbins", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("Arthur", "O Bobbins")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Arthur", "OBobbins")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("Arthur", "Bobbins")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("O", "Bobbins")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new PersonName("Bobbins", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(7), is(new PersonName("Bobbins", "O")));
        assertThat(INCORRECT_ORDER, names.get(8), is(new PersonName("Arthur", "O")));

        assertThat(DEDUPLICATION, names, not(contains(new PersonName("OBobbins", "Arthur"))));
        assertThat(DEDUPLICATION, names, not(contains(new PersonName("O", "Arthur"))));
    }

    @Test
    public void shouldHandleHyphensAndApostrophes() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Arthur-Brian", "O'Coates");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(16));

        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur-Brian", "O'Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("O'Coates", "Arthur-Brian")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("Arthur Brian", "O Coates")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Brian", "O Coates")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("ArthurBrian", "OCoates")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("Arthur", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new PersonName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(7), is(new PersonName("O", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(8), is(new PersonName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(9), is(new PersonName("Arthur", "O")));
        assertThat(INCORRECT_ORDER, names.get(10), is(new PersonName("Brian", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(11), is(new PersonName("Coates", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(12), is(new PersonName("Brian", "O")));
        assertThat(INCORRECT_ORDER, names.get(13), is(new PersonName("O", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(14), is(new PersonName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(15), is(new PersonName("Coates", "O")));

        assertThat(DEDUPLICATION, names, not(is(new PersonName("Arthur", "O Coates"))));
        assertThat(DEDUPLICATION, names, not(is(new PersonName("OCoates", "ArthurBrian"))));
        assertThat(DEDUPLICATION, names, not(is(new PersonName("O", "Arthur"))));
    }

    @Test
    public void shouldUseEntireMultiWordSurname() {
        List<PersonName> candidateNames = NameMatchingCandidatesGenerator.generateCandidateNames("A B C D E", "F G H");
        assertThat(INCORRECT_ORDER, candidateNames.get(0), equalTo(new PersonName("A B C D", "F G H")));
        assertThat(candidateNames, hasItems(
                new PersonName("B", "F G H"),
                new PersonName("C", "F G H"),
                new PersonName("D", "F G H")
        ));
        assertThat(DEDUPLICATION, candidateNames, not(hasItem(new PersonName("A", "F G H"))));
    }

    @Test
    public void shouldUseFirstFourAndLastThreeNamesIfOverSevenProvided() {
        List<PersonName> candidateNames = NameMatchingCandidatesGenerator.generateCandidateNames("A B C D E", "F G H");

        assertThat(candidateNames.stream().noneMatch(candidateName -> candidateName.firstName().equals("E")), is(true));
        assertThat(candidateNames.stream().noneMatch(candidateName -> candidateName.lastName().equals("E")), is(true));
    }

    @Test
    public void shouldOnlyUseFiveOfSevenNameCandidate() {
        List<PersonName> candidateNames = NameMatchingCandidatesGenerator.generateCandidateNames("A B C D E F G", "Van Halen");

        List<PersonName> expectedCandidateNames = Arrays.asList(
                new PersonName("A", "Van"),
                new PersonName("B", "Van"),
                new PersonName("C", "Van"),
                new PersonName("D", "Van"),
                new PersonName("G", "Van"),
                new PersonName("A", "Halen"),
                new PersonName("B", "Halen"),
                new PersonName("C", "Halen"),
                new PersonName("D", "Halen"),
                new PersonName("G", "Halen")
        );

        for (PersonName expectedCandidateName : expectedCandidateNames) {
            assertThat(containsHmrcEquivalentName(candidateNames, expectedCandidateName), is(true));
        }

        assertThat(candidateNames.contains(new PersonName("E", "Van")), is(false));
        assertThat(candidateNames.contains(new PersonName("F", "Van")), is(false));
        assertThat(candidateNames.contains(new PersonName("E", "Halen")), is(false));
        assertThat(candidateNames.contains(new PersonName("F", "Halen")), is(false));
    }

    @Test
    public void shouldUseJoiningInBothNamesWithSuppliedNamesAsFirstAttempt() {
        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidateNames("Bob-Brian", "Hill O'Coates-Smith");

        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Bob-Brian", "Hill O'Coates-Smith")));

        Assertions.assertThat(names).doesNotHaveDuplicates();
        assertThat(DEDUPLICATION, containsHmrcDuplicateNames(names), is(false));
    }

    @Test
    public void shouldGenerateCorrectSurnameCombinationsForMultipleSurnames() {

        List<PersonName> names = NameMatchingCandidatesGenerator.generateCandidatesForMultiWordLastName("A B C", "D E F");
        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(22));

        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("A B C", "D E F")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("A", "D E")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("A", "D F")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("A", "E D")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("A", "E F")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new PersonName("A", "F D")));
        assertThat(INCORRECT_ORDER, names.get(7), is(new PersonName("A", "F E")));
        assertThat(INCORRECT_ORDER, names.get(8), is(new PersonName("B", "D E F")));
        assertThat(INCORRECT_ORDER, names.get(9), is(new PersonName("B", "D E")));
        assertThat(INCORRECT_ORDER, names.get(10), is(new PersonName("B", "D F")));
        assertThat(INCORRECT_ORDER, names.get(11), is(new PersonName("B", "E D")));
        assertThat(INCORRECT_ORDER, names.get(12), is(new PersonName("B", "E F")));
        assertThat(INCORRECT_ORDER, names.get(13), is(new PersonName("B", "F D")));
        assertThat(INCORRECT_ORDER, names.get(14), is(new PersonName("B", "F E")));
        assertThat(INCORRECT_ORDER, names.get(15), is(new PersonName("C", "D E F")));
        assertThat(INCORRECT_ORDER, names.get(16), is(new PersonName("C", "D E")));
        assertThat(INCORRECT_ORDER, names.get(17), is(new PersonName("C", "D F")));
        assertThat(INCORRECT_ORDER, names.get(18), is(new PersonName("C", "E D")));
        assertThat(INCORRECT_ORDER, names.get(19), is(new PersonName("C", "E F")));
        assertThat(INCORRECT_ORDER, names.get(20), is(new PersonName("C", "F D")));
        assertThat(INCORRECT_ORDER, names.get(21), is(new PersonName("C", "F E")));
    }

    private boolean containsHmrcEquivalentName(Collection<PersonName> candidateNames, PersonName expectedName) {
        return candidateNames.stream()
                .anyMatch(name -> name.hmrcNameMatchingEquivalent().equals(expectedName.hmrcNameMatchingEquivalent()));
    }

    private boolean containsHmrcDuplicateNames(Collection<PersonName> candidateNames) {
        int totalNames = candidateNames.size();
        long totalDedupedNames = candidateNames.stream()
                .map(PersonName::hmrcNameMatchingEquivalent)
                .distinct()
                .count();
        return totalNames > totalDedupedNames;
    }
}
