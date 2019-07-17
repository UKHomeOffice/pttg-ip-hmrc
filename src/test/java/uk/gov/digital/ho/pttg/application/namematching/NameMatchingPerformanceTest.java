package uk.gov.digital.ho.pttg.application.namematching;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.then;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingPerformance.HasAliases.HAS_ALIASES;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingPerformance.HasAliases.NO_ALIASES;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingPerformance.HasSpecialCharacters.*;

@RunWith(MockitoJUnitRunner.class)
public class NameMatchingPerformanceTest {

    private static final String NO_SPECIAL_CHARACTERS = "John";
    private static final String NAME_WITH_SPECIAL_CHARACTER = "Jóhn";

    private static final InputNames ANY_INPUT_NAMES = new InputNames("any", "any");
    private static final NameDerivation ANY_NAME_DERIVATION = new NameDerivation(new Name(Optional.empty(), NameType.FIRST, 0, "any"));
    private static final CandidateDerivation ANY_CANDIDATE_DERIVATION = new CandidateDerivation(ANY_INPUT_NAMES,
                                                                                                Collections.emptyList(),
                                                                                                ANY_NAME_DERIVATION,
                                                                                                ANY_NAME_DERIVATION);

    @Mock private Appender<ILoggingEvent> mockLoggingAppender;
    private static final String LOG_TEST_APPENDER = "tester";

    private NameMatchingPerformance matchingPerformance;
    private ArgumentCaptor<LoggingEvent> logCaptor;

    @Before
    public void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger(NameMatchingPerformance.class);
        logger.addAppender(mockLoggingAppender);
        logger.setLevel(Level.DEBUG);
        mockLoggingAppender.setName(LOG_TEST_APPENDER);

        logCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
        matchingPerformance = new NameMatchingPerformance();
    }

    @After
    public void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(NameMatchingPerformance.class);
        logger.detachAppender(LOG_TEST_APPENDER);
    }


    @Test
    public void hasAliases_inputNameHasNoAliases_noAliases() {
        InputNames noAliases = new InputNames("John", "Smith");
        assertThat(matchingPerformance.hasAliases(noAliases)).isEqualTo(NO_ALIASES);
    }

    @Test
    public void hasAliases_inputNameHasAlias_hasAliases() {
        InputNames hasAliases = new InputNames("John", "Smith", "Jones");
        assertThat(matchingPerformance.hasAliases(hasAliases)).isEqualTo(HAS_ALIASES);
    }

    @Test
    public void hasSpecialCharacters_neitherNameHasSpecialCharacters_none() {
        InputNames noSpecialCharacters = new InputNames(NO_SPECIAL_CHARACTERS, NO_SPECIAL_CHARACTERS);
        assertThat(matchingPerformance.hasSpecialCharacters(noSpecialCharacters)).isEqualTo(NONE);
    }

    @Test
    public void hasSpecialCharacters_firstNameSpecial_firstOnly() {
        InputNames firstNameSpecial = new InputNames(NAME_WITH_SPECIAL_CHARACTER, NO_SPECIAL_CHARACTERS);
        assertThat(matchingPerformance.hasSpecialCharacters(firstNameSpecial)).isEqualTo(FIRST_ONLY);
    }

    @Test
    public void hasSpecialCharacters_lastNameSpecial_lastOnly() {
        InputNames lastNameSpecial = new InputNames(NO_SPECIAL_CHARACTERS, NAME_WITH_SPECIAL_CHARACTER);
        assertThat(matchingPerformance.hasSpecialCharacters(lastNameSpecial)).isEqualTo(LAST_ONLY);
    }

    @Test
    public void hasSpecialCharacters_bothNamesSpecial_firstAndLast() {
        InputNames bothNamesSpecial = new InputNames(NAME_WITH_SPECIAL_CHARACTER, NAME_WITH_SPECIAL_CHARACTER);
        assertThat(matchingPerformance.hasSpecialCharacters(bothNamesSpecial)).isEqualTo(FIRST_AND_LAST);
    }

    @Test
    public void logNameMatchingPerformanceForNoMatch_anyInputNames_debugLevelLog() {
        matchingPerformance.logNameMatchingPerformanceForNoMatch(ANY_INPUT_NAMES);

        then(mockLoggingAppender).should().doAppend(logCaptor.capture());

        assertThat(logCaptor.getValue().getLevel())
                .isEqualTo(Level.DEBUG);
    }

    @Test
    public void logNameMatchingPerformanceForMatch_anyCandidateDerivation_debugLevelLog() {
        matchingPerformance.logNameMatchingPerformanceForMatch(ANY_CANDIDATE_DERIVATION);

        then(mockLoggingAppender).should().doAppend(logCaptor.capture());

        assertThat(logCaptor.getValue().getLevel())
                .isEqualTo(Level.DEBUG);
    }

    @Test
    public void logNameMatchingPerformanceForNoMatch_someInputNames_logAnalysis() {
        InputNames someInputNames = new InputNames("some first names", "some last names");

        matchingPerformance.logNameMatchingPerformanceForNoMatch(someInputNames);

        then(mockLoggingAppender).should().doAppend(logCaptor.capture());
        assertThat(logCaptor.getValue().getArgumentArray())
                .contains(new ObjectAppendingMarker("name-matching-analysis", someInputNames));
    }

    @Test
    public void logNameMatchingPerformanceForMatch_someCandidateDerivation_logAnalysis() {
        CandidateDerivation someCandidateDerivation = new CandidateDerivation(ANY_INPUT_NAMES, Collections.emptyList(), ANY_NAME_DERIVATION, ANY_NAME_DERIVATION);

        matchingPerformance.logNameMatchingPerformanceForMatch(someCandidateDerivation);

        then(mockLoggingAppender).should().doAppend(logCaptor.capture());
        assertThat(logCaptor.getValue().getArgumentArray())
                .contains(new ObjectAppendingMarker("name-matching-analysis", someCandidateDerivation));
    }
}