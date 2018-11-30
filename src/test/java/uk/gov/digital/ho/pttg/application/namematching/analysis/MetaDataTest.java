package uk.gov.digital.ho.pttg.application.namematching.analysis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.pttg.application.namematching.GeneratorFactory;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;
import uk.gov.digital.ho.pttg.application.namematching.NameMatchingCandidatesService;
import uk.gov.digital.ho.pttg.application.namematching.candidates.NameMatchingCandidateGenerator;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                NameMatchingCandidatesService.class,
        }
)
public class MetaDataTest {

    @MockBean
    private GeneratorFactory mockGeneratorFactory;

    @Autowired
    private NameMatchingCandidatesService nameMatchingCandidatesService;

    @Test
    public void shouldUseCollaborators() {

        NameMatchingCandidateGenerator someGenerator = mock(NameMatchingCandidateGenerator.class);

        given(mockGeneratorFactory.createGenerators(any(InputNames.class))).willReturn(singletonList(someGenerator));

        nameMatchingCandidatesService.generateCandidateNames("some first names", "some last names", "some alias names");

        then(mockGeneratorFactory).should().createGenerators(any(InputNames.class));
    }

}
