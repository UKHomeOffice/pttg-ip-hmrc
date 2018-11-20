package uk.gov.digital.ho.pttg.application.namematching;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.pttg.application.namematching.candidates.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        NameMatchingCandidatesService.class,
        GeneratorFactory.class,
        NameCombinations.class,
        MultipleLastNames.class,
        SpecialCharacters.class,
        AliasCombinations.class,
        EntireNonAliasName.class,
        EntireLastNameAndEachFirstName.class,
        AbbreviatedNames.class
})
public class NameMatchingCandidatesServiceIT {

    private static final String INCORRECT_ORDER = "The names should be correctly generated in the defined order";
    private static final String INCORRECT_NUMBER_OF_GENERATED_NAMES = "The number of generated names should be as expected";
    private static final String DEDUPLICATION = "The name should be removed by deduplication";

    @Autowired
    private NameMatchingCandidatesService nameMatchingCandidatesService;

    @Test
    public void shouldHandleMultipleLastNames() {
        List<CandidateName> names = nameMatchingCandidatesService.generateCandidateNames("Arthur", "Brian Coates", "");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(6));
        assertThat("The lastname is used unsplit for the first permutation", names.get(0), is(new CandidateName("Arthur", "Brian Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new CandidateName("Arthur", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new CandidateName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new CandidateName("Coates", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new CandidateName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new CandidateName("Brian", "Arthur")));

        assertThat(DEDUPLICATION, names, not(contains(new CandidateName("Arthur", "Brian"))));

    }

    @Test
    public void shouldHandleHyphenatedNames() {
        List<CandidateName> names = nameMatchingCandidatesService.generateCandidateNames("Arthur-Brian", "Coates", "");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(6));
        assertThat(INCORRECT_ORDER, names.get(0), is(new CandidateName("Arthur-Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new CandidateName("Coates", "Arthur-Brian")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new CandidateName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new CandidateName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new CandidateName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new CandidateName("Brian", "Arthur")));

        assertThat(DEDUPLICATION, names, not(contains(new CandidateName("ArthurBrian", "Coates"))));
        assertThat(DEDUPLICATION, names, not(contains(new CandidateName("Coates", "ArthurBrian"))));
        assertThat(DEDUPLICATION, names, not(contains(new CandidateName("Arthur", "Coates"))));
        assertThat(DEDUPLICATION, names, not(contains(new CandidateName("Coates", "Arthur"))));
    }

    @Test
    public void shouldHandleApostrophedNames() {
        List<CandidateName> names = nameMatchingCandidatesService.generateCandidateNames("Arthur", "O'Bobbins", "");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(9));
        assertThat(INCORRECT_ORDER, names.get(0), is(new CandidateName("Arthur", "O'Bobbins")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new CandidateName("O'Bobbins", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new CandidateName("Arthur", "OBobbins")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new CandidateName("Arthur", "O Bobbins")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new CandidateName("Arthur", "Bobbins")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new CandidateName("O", "Bobbins")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new CandidateName("Bobbins", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(7), is(new CandidateName("Bobbins", "O")));
        assertThat(INCORRECT_ORDER, names.get(8), is(new CandidateName("Arthur", "O")));

        assertThat(DEDUPLICATION, names, not(contains(new CandidateName("OBobbins", "Arthur"))));
        assertThat(DEDUPLICATION, names, not(contains(new CandidateName("O", "Arthur"))));
    }

    @Test
    public void shouldHandleHyphensAndApostrophes() {
        List<CandidateName> names = nameMatchingCandidatesService.generateCandidateNames("Arthur-Brian", "O'Coates", "");

        assertThat(INCORRECT_NUMBER_OF_GENERATED_NAMES, names.size(), is(16));

        assertThat(INCORRECT_ORDER, names.get(0), is(new CandidateName("Arthur-Brian", "O'Coates")));
        assertThat(INCORRECT_ORDER, names.get(1), is(new CandidateName("O'Coates", "Arthur-Brian")));
        assertThat(INCORRECT_ORDER, names.get(2), is(new CandidateName("ArthurBrian", "OCoates")));
        assertThat(INCORRECT_ORDER, names.get(3), is(new CandidateName("Arthur Brian", "O Coates")));
        assertThat(INCORRECT_ORDER, names.get(4), is(new CandidateName("Brian", "O Coates")));
        assertThat(INCORRECT_ORDER, names.get(5), is(new CandidateName("Arthur", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(6), is(new CandidateName("Brian", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(7), is(new CandidateName("O", "Coates")));
        assertThat(INCORRECT_ORDER, names.get(8), is(new CandidateName("Arthur", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(9), is(new CandidateName("Arthur", "O")));
        assertThat(INCORRECT_ORDER, names.get(10), is(new CandidateName("Brian", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(11), is(new CandidateName("Coates", "Arthur")));
        assertThat(INCORRECT_ORDER, names.get(12), is(new CandidateName("Brian", "O")));
        assertThat(INCORRECT_ORDER, names.get(13), is(new CandidateName("O", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(14), is(new CandidateName("Coates", "Brian")));
        assertThat(INCORRECT_ORDER, names.get(15), is(new CandidateName("Coates", "O")));

        assertThat(DEDUPLICATION, names, not(is(new CandidateName("Arthur", "O Coates"))));
        assertThat(DEDUPLICATION, names, not(is(new CandidateName("OCoates", "ArthurBrian"))));
        assertThat(DEDUPLICATION, names, not(is(new CandidateName("O", "Arthur"))));
    }

    @Test
    public void shouldOnlyUseFiveOfSevenNameCandidate() {
        List<CandidateName> candidateNames = nameMatchingCandidatesService.generateCandidateNames("A B C D E F G", "Van Halen", "");

        List<CandidateName> expectedCandidateNames = Arrays.asList(
                new CandidateName("A", "Van"),
                new CandidateName("B", "Van"),
                new CandidateName("C", "Van"),
                new CandidateName("D", "Van"),
                new CandidateName("E", "Van"),
                new CandidateName("A", "Halen"),
                new CandidateName("B", "Halen"),
                new CandidateName("C", "Halen"),
                new CandidateName("D", "Halen"),
                new CandidateName("E", "Halen")
        );

        for (CandidateName expectedCandidateName : expectedCandidateNames) {
            assertThat(containsHmrcEquivalentName(candidateNames, expectedCandidateName), is(true));
        }

        assertThat(candidateNames.contains(new CandidateName("F", "Van")), is(false));
        assertThat(candidateNames.contains(new CandidateName("G", "Van")), is(false));
        assertThat(candidateNames.contains(new CandidateName("F", "Halen")), is(false));
        assertThat(candidateNames.contains(new CandidateName("G", "Halen")), is(false));
    }

    @Test
    public void shouldUseJoiningInBothNamesWithSuppliedNamesAsFirstAttempt() {
        List<CandidateName> names = nameMatchingCandidatesService.generateCandidateNames("Bob-Brian", "Hill O'Coates-Smith", "");

        assertThat(INCORRECT_ORDER, names.get(0), is(new CandidateName("Bob-Brian", "Hill O'Coates-Smith")));

        Assertions.assertThat(names).doesNotHaveDuplicates();
        assertThat(DEDUPLICATION, containsHmrcDuplicateNames(names), is(false));
    }

    private boolean containsHmrcEquivalentName(Collection<CandidateName> candidateNames, CandidateName expectedName) {
        return candidateNames.stream()
                .anyMatch(name -> name.hmrcNameMatchingEquivalent().equals(expectedName.hmrcNameMatchingEquivalent()));
    }

    private boolean containsHmrcDuplicateNames(Collection<CandidateName> candidateNames) {
        int totalNames = candidateNames.size();
        long totalDedupedNames = candidateNames.stream()
                .map(CandidateName::hmrcNameMatchingEquivalent)
                .distinct()
                .count();
        return totalNames > totalDedupedNames;
    }
}
