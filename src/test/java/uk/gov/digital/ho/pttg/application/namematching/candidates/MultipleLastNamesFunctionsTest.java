package uk.gov.digital.ho.pttg.application.namematching.candidates;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.MultipleLastNamesFunctions.*;

@RunWith(MockitoJUnitRunner.class)
public class MultipleLastNamesFunctionsTest {

    @Test
    public void generateAllLastNameCombinations() {
        List<String> firstNames = Arrays.asList("Aaa", "Bbb", "Ccc");
        List<String> lastNameCombinations = Arrays.asList("Ddd", "Eee", "Ddd Eee");

        List<CandidateName> newCandidateNames = MultipleLastNamesFunctions.generateAllLastNameCombinations(firstNames, lastNameCombinations);

        assertThat(newCandidateNames.size()).isEqualTo(9);
        assertThat(newCandidateNames.get(0)).isEqualTo(new CandidateName("Aaa", "Ddd"));
        assertThat(newCandidateNames.get(1)).isEqualTo(new CandidateName("Aaa", "Eee"));
        assertThat(newCandidateNames.get(2)).isEqualTo(new CandidateName("Aaa", "Ddd Eee"));
        assertThat(newCandidateNames.get(3)).isEqualTo(new CandidateName("Bbb", "Ddd"));
        assertThat(newCandidateNames.get(4)).isEqualTo(new CandidateName("Bbb", "Eee"));
        assertThat(newCandidateNames.get(5)).isEqualTo(new CandidateName("Bbb", "Ddd Eee"));
        assertThat(newCandidateNames.get(6)).isEqualTo(new CandidateName("Ccc", "Ddd"));
        assertThat(newCandidateNames.get(7)).isEqualTo(new CandidateName("Ccc", "Eee"));
        assertThat(newCandidateNames.get(8)).isEqualTo(new CandidateName("Ccc", "Ddd Eee"));
    }

    @Test
    public void addMultiPartLastNameCombinationTwoNames() {
        List<String> lastNameCombinations = Arrays.asList("Aaa");
        List<String> listOfLastNames = Arrays.asList("Bbb", "Ccc");

        List<String> newLastNameCombinations = addMultiPartLastNameToCombination(lastNameCombinations, listOfLastNames);

        assertThat(newLastNameCombinations.size()).isEqualTo(1);
        assertThat(newLastNameCombinations.get(0)).isEqualTo("Aaa");
    }

    @Test
    public void addMultiPartLastNameCombinationThreeNames() {
        List<String> lastNameCombinations = Arrays.asList("Aaa");
        List<String> listOfLastNames = Arrays.asList("Bbb", "Ccc", "Ddd");

        List<String> newLastNameCombinations = addMultiPartLastNameToCombination(lastNameCombinations, listOfLastNames);

        assertThat(newLastNameCombinations.size()).isEqualTo(2);
        assertThat(newLastNameCombinations.get(0)).isEqualTo("Bbb Ccc Ddd");
        assertThat(newLastNameCombinations.get(1)).isEqualTo("Aaa");
    }

    @Test
    public void addMultiPartLastNameCombinationNoSideEffects() {
        List<String> lastNameCombinations = Arrays.asList("Aaa");
        List<String> listOfLastNames = Arrays.asList("Bbb", "Ccc", "Ddd");

        List<String> multipartCombinations = addMultiPartLastNameToCombination(lastNameCombinations, listOfLastNames);

        assertThat(multipartCombinations.size()).isEqualTo(2);
        assertThat(lastNameCombinations.size()).isEqualTo(1);
    }

    @Test
    public void addFullNameIfNotAlreadyPresentWhenNameNotPresent() {
        List<CandidateName> candidateNames = Collections.singletonList(new CandidateName("firstname1", "lastname1"));

        List<CandidateName> newCandidateNames = addFullNameIfNotAlreadyPresent(candidateNames, new InputNames("firstname2", "lastname2"));

        assertThat(newCandidateNames.size()).isEqualTo(2);
        assertThat(newCandidateNames.get(0).firstName()).isEqualTo("firstname2");
        assertThat(newCandidateNames.get(0).lastName()).isEqualTo("lastname2");
        assertThat(newCandidateNames.get(1).firstName()).isEqualTo("firstname1");
        assertThat(newCandidateNames.get(1).lastName()).isEqualTo("lastname1");
    }

    @Test
    public void addFullNameIfNotAlreadyPresentWhenNamePresent() {
        List<CandidateName> candidateNames = Collections.singletonList(new CandidateName("firstname1", "lastname1"));

        List<CandidateName> newCandidateNames = addFullNameIfNotAlreadyPresent(candidateNames, new InputNames("firstname1", "lastname1"));

        assertThat(newCandidateNames.size()).isEqualTo(1);
        assertThat(newCandidateNames.get(0).firstName()).isEqualTo("firstname1");
        assertThat(newCandidateNames.get(0).lastName()).isEqualTo("lastname1");
    }
    
    @Test
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    public void addFullNameIfNotAlreadyPresentNoSideEffects() {
        List<CandidateName> candidateNames = Collections.singletonList(new CandidateName("firstname1", "lastname1"));

        addFullNameIfNotAlreadyPresent(candidateNames, new InputNames("firstname2", "lastname2"));
        
        assertThat(candidateNames.size()).isEqualTo(1);

    }

    @Test
    public void generateLastNameCombinationsNoShortNames() {
        List<String> lastNames = Arrays.asList("Aaa", "Bbb", "Ccc");

        List<String> lastNameCombinations = generateLastNameCombinations(lastNames);

        assertThat(lastNameCombinations.size()).isEqualTo(0);
    }

    @Test
    public void generateLastNameCombinationsOneShortName() {
        List<String> lastNames = Arrays.asList("Aa", "Bbb", "Ccc");

        List<String> lastNameCombinations = generateLastNameCombinations(lastNames);

        assertThat(lastNameCombinations.size()).isEqualTo(2);
        assertThat(lastNameCombinations.get(0)).isEqualTo("Aa Bbb");
        assertThat(lastNameCombinations.get(1)).isEqualTo("Aa Ccc");
    }

    @Test
    public void generateLastNameCombinationsTwoShortNames() {
        List<String> lastNames = Arrays.asList("Aa", "Bb", "Ccc");

        List<String> lastNameCombinations = generateLastNameCombinations(lastNames);

        assertThat(lastNameCombinations.size()).isEqualTo(4);
        assertThat(lastNameCombinations.get(0)).isEqualTo("Aa Bb");
        assertThat(lastNameCombinations.get(1)).isEqualTo("Aa Ccc");
        assertThat(lastNameCombinations.get(2)).isEqualTo("Bb Aa");
        assertThat(lastNameCombinations.get(3)).isEqualTo("Bb Ccc");
    }

    @Test
    public void generateLastNameCombinationsThreeShortNames() {
        List<String> lastNames = Arrays.asList("Aa", "Bb", "Cc");

        List<String> lastNameCombinations = generateLastNameCombinations(lastNames);

        assertThat(lastNameCombinations.size()).isEqualTo(6);
        assertThat(lastNameCombinations.get(0)).isEqualTo("Aa Bb");
        assertThat(lastNameCombinations.get(1)).isEqualTo("Aa Cc");
        assertThat(lastNameCombinations.get(2)).isEqualTo("Bb Aa");
        assertThat(lastNameCombinations.get(3)).isEqualTo("Bb Cc");
        assertThat(lastNameCombinations.get(4)).isEqualTo("Cc Aa");
        assertThat(lastNameCombinations.get(5)).isEqualTo("Cc Bb");
    }

}
