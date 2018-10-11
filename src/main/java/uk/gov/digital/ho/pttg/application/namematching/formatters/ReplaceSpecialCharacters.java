package uk.gov.digital.ho.pttg.application.namematching.formatters;

import org.apache.commons.lang3.tuple.Pair;

public class ReplaceSpecialCharacters implements NameFormatter {
    @Override
    public Pair<String, String> formatName(Pair<String, String> names) {
        return null;
    }

    @Override
    public boolean appliesTo(Pair<String, String> names) {
        return false;
    }
}
