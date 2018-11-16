package uk.gov.digital.ho.pttg.application.namematching.candidates;

import com.google.common.collect.ImmutableList;
import lombok.Getter;

import java.util.List;

// TODO: This isn't really used as an enum
public enum NamePairRules {
    ONE(ImmutableList.of(
            NamePair.of(0, 0)
    )),

    TWO(ImmutableList.of(
            NamePair.of(0, 1), NamePair.of(1, 0)
    )),

    THREE(ImmutableList.of(
            NamePair.of(0, 2), NamePair.of(1, 2),
            NamePair.of(2, 0), NamePair.of(2, 1),
            NamePair.of(0, 1), NamePair.of(1, 0)
    )),

    FOUR(ImmutableList.of(
            NamePair.of(0, 3), NamePair.of(1, 3), NamePair.of(2, 3),
            NamePair.of(0, 1), NamePair.of(0, 2), NamePair.of(1, 0),
            NamePair.of(2, 0), NamePair.of(3, 0), NamePair.of(1, 2),
            NamePair.of(2, 1), NamePair.of(3, 1), NamePair.of(3, 2)
    )),

    FIVE(ImmutableList.of(
            NamePair.of(0, 4), NamePair.of(1, 4), NamePair.of(2, 4), NamePair.of(3, 4),
            NamePair.of(0, 1), NamePair.of(0, 2), NamePair.of(0, 3),
            NamePair.of(1, 0), NamePair.of(1, 2), NamePair.of(1, 3),
            NamePair.of(2, 0), NamePair.of(2, 1), NamePair.of(2, 3),
            NamePair.of(3, 0), NamePair.of(3, 1), NamePair.of(3, 2),
            NamePair.of(4, 0), NamePair.of(4, 1), NamePair.of(4, 2), NamePair.of(4, 3)
    )),

    SIX(ImmutableList.of(
            NamePair.of(0, 5), NamePair.of(1, 5), NamePair.of(2, 5), NamePair.of(3, 5),
            NamePair.of(0, 1), NamePair.of(0, 2), NamePair.of(0, 3), NamePair.of(0, 4),
            NamePair.of(1, 0), NamePair.of(1, 2), NamePair.of(1, 3), NamePair.of(1, 4),
            NamePair.of(2, 0), NamePair.of(2, 1), NamePair.of(2, 3), NamePair.of(2, 4),
            NamePair.of(3, 0), NamePair.of(3, 1), NamePair.of(3, 2), NamePair.of(3, 4),
            NamePair.of(4, 0), NamePair.of(4, 1), NamePair.of(4, 2), NamePair.of(4, 3), NamePair.of(4, 5),
            NamePair.of(5, 0), NamePair.of(5, 1), NamePair.of(5, 2), NamePair.of(5, 3), NamePair.of(5, 4)
    )),

    SEVEN(ImmutableList.of(
            NamePair.of(0, 6), NamePair.of(1, 6), NamePair.of(2, 6), NamePair.of(3, 6), NamePair.of(4, 6), NamePair.of(5, 6),
            NamePair.of(0, 1), NamePair.of(0, 2), NamePair.of(0, 3), NamePair.of(0, 4), NamePair.of(0, 5),
            NamePair.of(1, 0), NamePair.of(1, 2), NamePair.of(1, 3), NamePair.of(1, 4), NamePair.of(1, 5),
            NamePair.of(2, 0), NamePair.of(2, 1), NamePair.of(2, 3), NamePair.of(2, 4), NamePair.of(2, 5),
            NamePair.of(3, 0), NamePair.of(3, 1), NamePair.of(3, 2), NamePair.of(3, 4), NamePair.of(3, 5),
            NamePair.of(4, 0), NamePair.of(4, 1), NamePair.of(4, 2), NamePair.of(4, 3), NamePair.of(4, 5),
            NamePair.of(5, 0), NamePair.of(5, 1), NamePair.of(5, 2), NamePair.of(5, 3), NamePair.of(5, 4),
            NamePair.of(6, 0), NamePair.of(6, 1), NamePair.of(6, 2), NamePair.of(6, 3), NamePair.of(6, 4), NamePair.of(6, 5)
    ));

    @Getter
    private final List<NamePair> namePairs;

    NamePairRules(List<NamePair> namePairs) {
        this.namePairs = namePairs;
    }

    static List<NamePair> forNameCount(int nameCount) {
        validateNameCount(nameCount);

        NamePairRules namePairRules = values()[nameCount - 1];
        return namePairRules.getNamePairs();
    }

    private static void validateNameCount(int nameCount) {
        boolean isInvalidNameCount = (nameCount <= 0) || (nameCount > values().length);
        if (isInvalidNameCount) {
            throw new IllegalArgumentException(String.format("There are no name rules for `%d` number of names", nameCount));
        }
    }
}
