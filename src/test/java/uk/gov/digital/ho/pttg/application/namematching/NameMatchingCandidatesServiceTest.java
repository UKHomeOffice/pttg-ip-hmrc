package uk.gov.digital.ho.pttg.application.namematching;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Ignore // TODO once the name matching candidate service has been refactored it needs some new unit tests here
public class NameMatchingCandidatesServiceTest {

    private static final String INCORRECT_ORDER = "The names should be correctly generated in the defined order";
    private static final String INCORRECT_NUMBER_OF_GENERATED_NAMES = "The number of generated names should be as expected";

    @InjectMocks
    private NameMatchingCandidatesService nameMatchingCandidatesService;

}
