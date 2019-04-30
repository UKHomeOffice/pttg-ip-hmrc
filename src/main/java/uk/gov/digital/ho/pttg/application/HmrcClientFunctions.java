package uk.gov.digital.ho.pttg.application;

import java.time.LocalDate;
import java.time.MonthDay;

final class HmrcClientFunctions {

    private HmrcClientFunctions() {
        throw new UnsupportedOperationException("Companion class for HmrcClient - do not instatiate.");
    }

    private static final MonthDay END_OF_TAX_YEAR = MonthDay.of(4, 5);

    static String getTaxYear(LocalDate date) {
        String taxYear;
        if (MonthDay.from(date).isAfter(END_OF_TAX_YEAR)) {
            taxYear = date.getYear() + "-" + (removeFirstTwoDigits(date.getYear() + 1));
        } else {
            taxYear = (date.getYear() - 1) + "-" + removeFirstTwoDigits(date.getYear());
        }
        return taxYear;
    }

    private static int removeFirstTwoDigits(int fourDigitYear) {
        return fourDigitYear % 100;
    }

}
