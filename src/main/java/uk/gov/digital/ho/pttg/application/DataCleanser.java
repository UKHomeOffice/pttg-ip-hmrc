package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.pttg.dto.Income;

import java.math.BigDecimal;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
public class DataCleanser {

    public static List<Income> clean(List<Income> incomes) {
        return removeIncomesWithZeroPayment(incomes);
    }

    private static List<Income> removeIncomesWithZeroPayment(List<Income> incomes) {

        List<Income> incomeZerosRemoved = null;

        if (incomes != null) {

            incomeZerosRemoved = incomes
                                         .stream()
                                         .filter(p -> (p.getTaxablePayment().compareTo(BigDecimal.ZERO) > 0))
                                         .collect(toList());

            int numberToRemove = incomes.size() - incomeZerosRemoved.size();

            if (numberToRemove > 0) {
                log.info("Removing {} incomes (<= 0) from HMRC income response", numberToRemove);
            }
        }

        return incomeZerosRemoved;
    }
}