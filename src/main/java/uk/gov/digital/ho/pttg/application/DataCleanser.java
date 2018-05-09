package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.pttg.dto.Income;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @deprecated HMRC now provide the data without empty income data
 */
@Slf4j
@Deprecated
public class DataCleanser {

    public static List<Income> clean(Individual individual, List<Income> incomes) {
        return removeIncomesWithZeroPayment(individual, incomes);
    }

    private static List<Income> removeIncomesWithZeroPayment(Individual individual, List<Income> incomes) {
        List<Income> incomeZerosRemoved = null;
        if (incomes != null) {
            incomeZerosRemoved = incomes.stream().filter(p -> (p.getTaxablePayment().compareTo(BigDecimal.ZERO) > 0)).collect(Collectors.toList());
            if (incomeZerosRemoved.size() < incomes.size()) {
                log.info("Removing zero-valued incomes from HMRC income response");
            }
        }
        return incomeZerosRemoved;
    }
}