package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.AliasCombinationsFunctions.removeName;

public class AliasCombinationsFunctionsRemoveTest {

    @Test
    public void removeNameFromEmptyListShouldReturnEmptyList() {
        assertThat(removeName("", emptyList())).isEqualTo(emptyList());
        assertThat(removeName("any non-empty string", emptyList())).isEqualTo(emptyList());
    }

    @Test
    public void removeNameThatIsNotInListShouldReturnOriginalList() {
        List<String> names = asList("John", "Smith");
        assertThat(removeName("David", names)).isEqualTo(names);
    }

    @Test
    public void removeNameThatIsInListShouldRemoveName() {
        List<String> names = asList("John", "Smith");
        assertThat(removeName("John", names)).isEqualTo(singletonList("Smith"));
        assertThat(removeName("Smith", names)).isEqualTo(singletonList("John"));
    }

}
