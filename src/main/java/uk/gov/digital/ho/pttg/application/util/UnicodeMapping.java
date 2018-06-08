package uk.gov.digital.ho.pttg.application.util;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

final class UnicodeMapping {
    private final Map<Character, String> mappedCharacters;

    private UnicodeMapping(File file) {
        try {
            List<Entry> expectedUnicodeMapping = getUnicodeMappings(file);
            mappedCharacters = expectedUnicodeMapping.stream().collect(toMap(Entry::getKey, Entry::getValue));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static UnicodeMapping fromFile(File file) {
        return new UnicodeMapping(file);
    }

    private static List<Entry> getUnicodeMappings(File file) throws IOException {
        MappingIterator<Entry> mappingIterator = new CsvMapper()
                .readerWithTypedSchemaFor(Entry.class)
                .readValues(file);

        return mappingIterator.readAll();
    }

    boolean contains(char key) {
        return mappedCharacters.containsKey(key);
    }

    public String get(char key) {
        return mappedCharacters.get(key);
    }

    private static class Entry {
        private char key;
        private String value;

        public char getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
