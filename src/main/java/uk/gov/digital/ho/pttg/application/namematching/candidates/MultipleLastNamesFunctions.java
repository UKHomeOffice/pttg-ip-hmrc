package uk.gov.digital.ho.pttg.application.namematching.candidates;

import org.apache.commons.lang3.StringUtils;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.InputNames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class MultipleLastNamesFunctions {

    static List<CandidateName> addAllLastNameCombinations(List<String> firstNames, List<String> lastNameCombinations) {
        List<CandidateName> combinations =
                firstNames.stream()
                        .flatMap(firstName -> lastNameCombinations.stream()
                                .map(lastNameCombination -> new CandidateName(firstName, lastNameCombination)))
                        .collect(toList());

        return Collections.unmodifiableList(combinations);
    }

    static List<String> addMultiPartLastNameToCombination(List<String> lastNameCombinations, List<String> listOfLastNames) {
        final int MIN_NAMES_FOR_MULTIPART = 3;

        List<String> newLastNameCombinations = new ArrayList<>();

        if (listOfLastNames.size() >= MIN_NAMES_FOR_MULTIPART) {
            newLastNameCombinations.add(StringUtils.join(listOfLastNames, " "));
        }

        newLastNameCombinations.addAll(lastNameCombinations);
        return Collections.unmodifiableList(newLastNameCombinations);
    }

        static List<CandidateName> addFullNameIfNotAlreadyPresent(List<CandidateName> candidates, InputNames inputNames) {
        CandidateName fullName = new CandidateName(inputNames.fullFirstName(), inputNames.fullLastName());

        List<CandidateName> newCandidates = new ArrayList<>();

        if (!candidates.contains(fullName)) {
            newCandidates.add(fullName);
        }

        newCandidates.addAll(candidates);
        return Collections.unmodifiableList(newCandidates);
    }

    static List<String> generateLastNameCombinations(List<String> lastNames) {
        final int HMRC_SURNAME_LENGTH = 3;

        return lastNames.stream()
                .filter(lastName -> lastName.length() < HMRC_SURNAME_LENGTH)
                .distinct()
                .flatMap(lastName1 -> lastNames.stream()
                        .filter(lastName2 -> !lastName1.equals(lastName2))
                        .map(lastName2 -> lastName1 + " " + lastName2))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }
}
