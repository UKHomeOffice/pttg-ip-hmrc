package uk.gov.digital.ho.pttg.application.namematching.formatters;

import org.apache.commons.lang3.tuple.Pair;

public interface NameFormatter {

    Pair<String, String> formatName(Pair<String, String> names);
    boolean appliesTo(Pair<String, String> names);
}
