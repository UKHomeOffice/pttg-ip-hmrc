package uk.gov.digital.ho.pttg.application.namematching;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.application.namematching.candidates.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class GeneratorFactoryTest {

    @Mock EntireNonAliasName mockEntireNonAliasName;
    @Mock EntireLastNameAndEachFirstName mockEntireLastNameAndEachFirstName;
    @Mock MultipleLastNames mockMultipleLastNames;
    @Mock AbbreviatedNames mockAbbreviatedNames;
    @Mock AliasCombinations mockAliasCombinations;
    @Mock NameCombinations mockNameCombinations;
    @Mock SpecialCharacters mockSpecialCharacter;

    private GeneratorFactory generatorFactory;
    @Before
    public void setup() {

        generatorFactory = new GeneratorFactory(
                mockEntireNonAliasName,
                mockEntireLastNameAndEachFirstName,
                mockMultipleLastNames,
                mockAbbreviatedNames,
                mockAliasCombinations,
                mockNameCombinations,
                mockSpecialCharacter
        );
    }

    @Test
    public void shouldProduceDefaultGenerators() {

        InputNames anyInputNames = mock(InputNames.class);

        List<NameMatchingCandidateGenerator> generators = generatorFactory.createGenerators(anyInputNames);

        assertThat(generators).containsExactly(
                mockEntireNonAliasName,
                mockEntireLastNameAndEachFirstName,
                mockMultipleLastNames,
                mockAbbreviatedNames,
                mockAliasCombinations,
                mockNameCombinations,
                mockSpecialCharacter);
    }
}