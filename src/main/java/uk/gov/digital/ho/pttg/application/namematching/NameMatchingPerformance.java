package uk.gov.digital.ho.pttg.application.namematching;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.pttg.application.LogEvent.EVENT;
import static uk.gov.digital.ho.pttg.application.LogEvent.HMRC_MATCHING_PERFORMANCE_ANALYSIS;

@Component
@Slf4j
public class NameMatchingPerformance {

    public HasAliases hasAliases(InputNames inputNames) {
        return inputNames.hasAliasSurnames() ? HasAliases.HAS_ALIASES : HasAliases.NO_ALIASES;
    }

    public HasSpecialCharacters hasSpecialCharacters(InputNames inputNames) {
        boolean firstNameSpecial = inputNames.firstNames().stream().anyMatch(this::hasSpecialCharacters);
        boolean lastNameSpecial = inputNames.lastNames().stream().anyMatch(this::hasSpecialCharacters);

        if (firstNameSpecial && lastNameSpecial) {
            return HasSpecialCharacters.FIRST_AND_LAST;
        }
        if (firstNameSpecial) {
            return HasSpecialCharacters.FIRST_ONLY;
        }
        if (lastNameSpecial) {
            return HasSpecialCharacters.LAST_ONLY;
        }
        return HasSpecialCharacters.NONE;
    }

    public void logNameMatchingPerformanceForMatch(CandidateDerivation candidateDerivation) {
        log.debug("Name Matching Analysis - matched" ,
                  value("name-matching-analysis", candidateDerivation),
                  value(EVENT, HMRC_MATCHING_PERFORMANCE_ANALYSIS));
    }

    public void logNameMatchingPerformanceForNoMatch(InputNames inputNames) {
        log.debug("Name Matching Analysis - not matched",
                  value("name-matching-analysis", inputNames),
                  value(EVENT, HMRC_MATCHING_PERFORMANCE_ANALYSIS));
    }

    private boolean hasSpecialCharacters(Name name) {
        return name.containsDiacritics() || name.containsNameSplitter() || hasNonBasicLatinCharacters(name);
    }

    private boolean hasNonBasicLatinCharacters(Name name) {
        return name.unicodeBlocks().stream()
                   .anyMatch(unicodeBlock -> !unicodeBlock.equals(Character.UnicodeBlock.BASIC_LATIN));
    }

    public enum HasAliases {
        HAS_ALIASES,
        NO_ALIASES;
    }

    public enum HasSpecialCharacters {
        FIRST_ONLY,
        LAST_ONLY,
        FIRST_AND_LAST,
        NONE;
    }
}
