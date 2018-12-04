package uk.gov.digital.ho.pttg.application.namematching;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.pttg.spring.SpringConfiguration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator.ALIAS_COMBINATIONS;
import static uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator.Generator.MULTIPLE_NAMES;

@RunWith(SpringRunner.class)
public class CandidateDerivationTest {

    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        SpringConfiguration.initialiseObjectMapper(objectMapper);
    }

    @Test
    public void shouldProduceUnmodifiableVersionOfGenerators() {

        CandidateDerivation candidateDerivation = new CandidateDerivation(
                null,
                Arrays.asList(MULTIPLE_NAMES),
                null,
                null);

        assertThatThrownBy(() -> candidateDerivation.generators().add(ALIAS_COMBINATIONS))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void shouldMarshallEmptyData() throws IOException {

        CandidateDerivation candidateDerivation = new CandidateDerivation(null, null, null, null);

        String json = objectMapper.writeValueAsString(candidateDerivation);

        DocumentContext documentContext = JsonPath.parse(json);

        assertThat((String)documentContext.read("$.inputNames")).isNull();
        assertThat((String)documentContext.read("$.generators")).isNull();
        assertThat((String)documentContext.read("$.firstName")).isNull();
        assertThat((String)documentContext.read("$.lastName")).isNull();
    }

    @Test
    public void shouldAddGenerator() {

        List<Generator> generators = Arrays.asList(ALIAS_COMBINATIONS);
        CandidateDerivation candidateDerivation = new CandidateDerivation(null, generators, null, null);

        candidateDerivation.addGenerator(MULTIPLE_NAMES);

        assertThat(candidateDerivation.generators()).containsExactly(MULTIPLE_NAMES, ALIAS_COMBINATIONS);
    }
}
