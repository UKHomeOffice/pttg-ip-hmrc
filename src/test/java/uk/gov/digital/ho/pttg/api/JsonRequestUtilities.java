package uk.gov.digital.ho.pttg.api;

import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.Charset.defaultCharset;

class JsonRequestUtilities {

    private final static String DEFAULT_JSON_REQUEST = "template/IncomeDataRequest/basic-request.json";
    private final static String REQUEST_WITHOUT_ALIAS = "template/IncomeDataRequest/request-no-alias.json";
    private final static String REQUEST_WITH_NULL_ALIAS = "template/IncomeDataRequest/request-null-alias.json";

    static String getDefaultRequest() throws IOException {
        return FileUtils.readFileToString(getRequestAsFile(DEFAULT_JSON_REQUEST), defaultCharset());
    }

    static String getRequestWithoutAlias() throws IOException {
        return FileUtils.readFileToString(getRequestAsFile(REQUEST_WITHOUT_ALIAS), defaultCharset());
    }

    static String getRequestWithNullAlias() throws IOException {
        return FileUtils.readFileToString(getRequestAsFile(REQUEST_WITH_NULL_ALIAS), defaultCharset());
    }

    static String getRequestAndRemoveLineWithKey(String key) throws IOException {
        return getDefaultRequestLines().stream()
                .filter(s -> !s.contains(String.format("\"%s\":", key)))
                .collect(Collectors.joining());
    }

    static String getRequestAndReplaceValueWithNull(String key) throws IOException {
        return getDefaultRequestLines().stream()
                .map(s -> {
                    if(s.contains(String.format("\"%s\":", key))) {
                        return String.format("  \"%s\": null,", key);
                    } else {
                        return s;
                    }
                }).collect(Collectors.joining());
    }

    static String getRequestAndReplaceValue(String key, String newValue) throws IOException {
        return getDefaultRequestLines().stream()
                .map(s -> {
                    if(s.contains(String.format("\"%s\":", key))) {
                        return String.format("  \"%s\": \"%s\",", key, newValue);
                    } else {
                        return s;
                    }
                }).collect(Collectors.joining());
    }

    private static List<String> getDefaultRequestLines() throws IOException {
        File requestFile = getRequestAsFile(DEFAULT_JSON_REQUEST);

        return FileUtils.readLines(requestFile, defaultCharset());
    }

    private static File getRequestAsFile(String requestFile) throws IOException {
        return ResourceUtils.getFile(String.format("classpath:%s", requestFile));
    }
}
