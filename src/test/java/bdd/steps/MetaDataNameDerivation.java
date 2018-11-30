package bdd.steps;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import uk.gov.digital.ho.pttg.application.namematching.DerivationAction;
import uk.gov.digital.ho.pttg.application.namematching.NameType;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

@AllArgsConstructor
@Getter
@Accessors(fluent = true)
class MetaDataNameDerivation {

    private NameType section;

    private int length;

    @Getter(AccessLevel.NONE)
    private String index;

    @Getter(AccessLevel.NONE)
    private String derivationActions;

    List<Integer> index() {
        if (index.equals("null")) {
            return null;
        }

        return Arrays.stream(index.split("\\s+"))
                .map(s -> Integer.valueOf(s))
                .collect(toList());
    }

    List<DerivationAction> derivationActions() {
        return Arrays.stream(derivationActions.split("\\s+"))
                       .map(DerivationAction::valueOf)
                       .collect(toList());
    }
}
