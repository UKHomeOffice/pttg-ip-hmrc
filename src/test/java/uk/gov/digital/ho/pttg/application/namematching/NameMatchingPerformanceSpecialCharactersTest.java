package uk.gov.digital.ho.pttg.application.namematching;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.gov.digital.ho.pttg.application.namematching.NameMatchingPerformance.HasSpecialCharacters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingPerformance.HasSpecialCharacters.*;

@RunWith(Parameterized.class)
public class NameMatchingPerformanceSpecialCharactersTest {

    private String firstName;
    private String lastName;
    private HasSpecialCharacters expectedHasSpecialCharacters;

    private NameMatchingPerformance nameMatchingPerformance = new NameMatchingPerformance();

    public NameMatchingPerformanceSpecialCharactersTest(String firstName, String lastName, HasSpecialCharacters expectedHasSpecialCharacters) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.expectedHasSpecialCharacters = expectedHasSpecialCharacters;
    }

    @Parameterized.Parameters(name = "For firstName=\"{0}\", lastName=\"{1}\" - expected HasSpecialCharacters={2}")
    public static Iterable<Object[]> testCases() {
        List<Object[]> testCases = new ArrayList<>();
        testCases.add(new Object[]{"John", "Smith", NONE});

        testCases.addAll(diacriticsTestCases());
        testCases.addAll(umlautTestCases());
        testCases.addAll(fullStopTestCases());
        testCases.addAll(hyphenTestCases());
        testCases.addAll(unicodeTestCases());
        return testCases;
    }

    @Test
    public void addNameMatchingPerformance_givenNames_expectedSpecialCharactersLog() {
        assertThat(nameMatchingPerformance.hasSpecialCharacters(new InputNames(firstName, lastName)))
                .isEqualTo(expectedHasSpecialCharacters);
    }

    private static Collection<Object[]> diacriticsTestCases() {
        return Arrays.asList(new Object[]{"Jóhn", "Smith", FIRST_ONLY},
                             new Object[]{"John", "Smìth", LAST_ONLY},
                             new Object[]{"Jóhn", "Smìth", FIRST_AND_LAST});
    }

    private static Collection<Object[]> umlautTestCases() {
        return Arrays.asList(new Object[]{"Jöhn", "Smith", FIRST_ONLY},
                             new Object[]{"John", "Smïth", LAST_ONLY},
                             new Object[]{"Jöhn", "Smïth", FIRST_AND_LAST}
                            );
    }

    private static Collection<Object[]> fullStopTestCases() {
        return Arrays.asList(new Object[]{"St.John", "Smith", FIRST_ONLY},
                             new Object[]{"St. John", "Smith", FIRST_ONLY},
                             new Object[]{"John", "St.John", LAST_ONLY},
                             new Object[]{"John", "St. John", LAST_ONLY},
                             new Object[]{"St.John", "St. John", FIRST_AND_LAST});
    }

    private static Collection<Object[]> hyphenTestCases() {
        return Arrays.asList(new Object[]{"John-Joe", "Smith", FIRST_ONLY},
                             new Object[]{"John", "Smith-Jones", LAST_ONLY},
                             new Object[]{"John-Joe", "Smith-Jones", FIRST_AND_LAST});
    }

    private static Collection<Object[]> unicodeTestCases() {
        return Arrays.asList(new Object[]{"▣", "Smith", FIRST_ONLY},
                             new Object[]{"J▣hn", "Smith", FIRST_ONLY},
                             new Object[]{"John", "▣", LAST_ONLY},
                             new Object[]{"John", "Sm▣th", LAST_ONLY},
                             new Object[]{"▣", "▣", FIRST_AND_LAST});
    }
}
