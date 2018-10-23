package uk.gov.digital.ho.pttg.application.namematching;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class InputNamesFunctions {

    static List<String> splitIntoDistinctNames(String name) {
        if (isBlank(name)) {
            return Collections.emptyList();
        }

        String[] splitNames = name.trim().split("\\s+");

        return Arrays.asList(splitNames);
    }

}
