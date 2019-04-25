package uk.gov.digital.ho.pttg.application;

import java.util.Arrays;
import java.util.List;

public enum IncomeDataTypes {
    PAYE, SELF_ASSESSMENT;

    public static boolean allIncomeDataTypes(List<IncomeDataTypes> incomeDataTypes) {
        return incomeDataTypes.containsAll(Arrays.asList(IncomeDataTypes.values()));
    }

    public static boolean noIncomeDataTypes(List<IncomeDataTypes> incomeDataTypes) {
        return incomeDataTypes.isEmpty();
    }
}
