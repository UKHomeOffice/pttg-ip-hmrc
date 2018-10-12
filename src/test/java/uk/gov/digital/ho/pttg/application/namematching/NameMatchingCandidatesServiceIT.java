package uk.gov.digital.ho.pttg.application.namematching;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.pttg.application.namematching.candidates.MultipleLastNames;
import uk.gov.digital.ho.pttg.application.namematching.candidates.NameCombinations;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        NameMatchingCandidatesService.class,
        NameCombinations.class,
        MultipleLastNames.class
})
public class NameMatchingCandidatesServiceIT {

    private static final String INCORRECT_ORDER = "The names should be correctly generated in the defined order";
    private static final String INCORRECT_NUMBER_OF_GENERATED_NAMES = "The number of generated names should be as expected";

    @Autowired
    private NameMatchingCandidatesService nameMatchingCandidatesService;

    @Test
    public void shouldHandleMultipleLastNames() {
        List<PersonName> names = nameMatchingCandidatesService.generateCandidateNames("Arthur", "Brian Coates");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(7));
        assertThat("The lastname is used unsplit for the first permutation", names.get(0), is(new PersonName("Arthur", "Brian Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("Arthur", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Coates", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new PersonName("Brian", "Arthur")));

    }

    @Test
    public void shouldHandleHyphenatedNames() {
        List<PersonName> names = nameMatchingCandidatesService.generateCandidateNames("Arthur-Brian", "Coates");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(10));
        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur-Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("Coates", "Arthur-Brian")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("ArthurBrian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Coates", "ArthurBrian")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("Arthur", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new PersonName("Coates", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(7), is(new PersonName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(8), is(new PersonName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(9), is(new PersonName("Brian", "Arthur")));
    }

    @Test
    public void shouldHandleApostrophedNames() {
        List<PersonName> names = nameMatchingCandidatesService.generateCandidateNames("Arthur", "O'Bobbins");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(11));
        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur", "O'Bobbins")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("O'Bobbins", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("Arthur", "O Bobbins")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Arthur", "OBobbins")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("OBobbins", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("Arthur", "Bobbins")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new PersonName("O", "Bobbins")));
        assertThat(INCORRECT_ORDER, names.get(7), is(new PersonName("Bobbins", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(8), is(new PersonName("Bobbins", "O")));
        assertThat(INCORRECT_ORDER, names.get(9), is(new PersonName("Arthur", "O")));
        assertThat(INCORRECT_ORDER, names.get(10), is(new PersonName("O", "Arthur")));
    }

    @Test
    public void shouldHandleHyphensAndApostrophes() {
        List<PersonName> names = nameMatchingCandidatesService.generateCandidateNames("Arthur-Brian", "O'Coates");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(19));

        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Arthur-Brian", "O'Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new PersonName("O'Coates", "Arthur-Brian")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new PersonName("Arthur Brian", "O Coates")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new PersonName("Arthur", "O Coates")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new PersonName("Brian", "O Coates")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new PersonName("ArthurBrian", "OCoates")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new PersonName("OCoates", "ArthurBrian")));
        assertThat(INCORRECT_ORDER, names.get(7), is(new PersonName("Arthur", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(8), is(new PersonName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(9), is(new PersonName("O", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(10), is(new PersonName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(11), is(new PersonName("Arthur", "O")));
        assertThat(INCORRECT_ORDER, names.get(12), is(new PersonName("Brian", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(13), is(new PersonName("O", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(14), is(new PersonName("Coates", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(15), is(new PersonName("Brian", "O")));
        assertThat(INCORRECT_ORDER, names.get(16), is(new PersonName("O", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(17), is(new PersonName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(18), is(new PersonName("Coates", "O")));
    }

    @Test
    public void shouldUseFirstFourAndLastThreeNamesIfOverSevenProvided() {
        List<PersonName> candidateNames = nameMatchingCandidatesService.generateCandidateNames("A B C D E", "F G H");

        assertThat(candidateNames.size(), is(71));

        candidateNames = candidateNames.stream().
                filter(name -> !name.lastName().equals("F G H")).
                collect(Collectors.toList());

        assertThat(candidateNames.size(), is(66)); // Expect there to be 5 candidates of retained first names paired with the entire lastname "F G H"

        for (PersonName name : candidateNames) {
            assertThat(name.firstName().contains("E"), is(false));
            assertThat(name.lastName().contains("E"), is(false));
        }
    }

    @Test
    public void shouldOnlyUseFiveOfSevenNameCandidate() {
        List<PersonName> candidateNames = nameMatchingCandidatesService.generateCandidateNames("A B C D E F G", "Van Halen");

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
            assertThat(candidateNames.contains(expectedCandidateName), is(true));
        }

        assertThat(candidateNames.contains(new PersonName("E", "Van")), is(false));
        assertThat(candidateNames.contains(new PersonName("F", "Van")), is(false));
        assertThat(candidateNames.contains(new PersonName("E", "Halen")), is(false));
        assertThat(candidateNames.contains(new PersonName("F", "Halen")), is(false));
    }

    @Test
    public void shouldUseJoiningInBothNamesWithSuppliedNamesAsFirstAttempt() {
        List<PersonName> names = nameMatchingCandidatesService.generateCandidateNames("Bob-Brian", "Hill O'Coates-Smith");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(53));

        assertThat(INCORRECT_ORDER, names.get(0), is(new PersonName("Bob-Brian", "Hill O'Coates-Smith")));

        Assertions.assertThat(names).doesNotHaveDuplicates();
    }

}
