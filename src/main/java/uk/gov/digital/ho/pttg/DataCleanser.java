package uk.gov.digital.ho.pttg;

import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.pttg.dto.Income;
import uk.gov.digital.ho.pttg.dto.Individual;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/* removes inconsistencies from the HMRC income response:
* 1. any payments with full matching details - amounts, dates etc are the same
* 2. any payments with a taxable amnount of zero
*  Any adjusted income lists will be identified in the loglogged by NINO for information */
@Slf4j
class DataCleanser {

    public static List<Income> clean(Individual individual, List<Income> incomes) {
        return removeIncomesWithZeroPayment(individual,removeDuplicateIncomes(individual, incomes));
    }

    private static List<Income> removeDuplicateIncomes(Individual individual, List<Income> incomes) {
        final List<Income> incomeDuplicatesRemoved = incomes.stream().distinct().collect(Collectors.toList());
        if (incomeDuplicatesRemoved.size() < incomes.size()) {
            log.info("Removing duplicate incomes from {} HMRC income response", individual.getNino());
        }
        return incomeDuplicatesRemoved;
    }

    private static List<Income> removeIncomesWithZeroPayment(Individual individual, List<Income> incomes) {
        final List<Income> incomeZerosRemoved = incomes.stream().filter(p -> (p.getTaxablePayment().compareTo(BigDecimal.ZERO) > 0)).collect(Collectors.toList());
        if (incomeZerosRemoved.size() < incomes.size()) {
            log.info("Removing zero-valued incomes from HMRC income response from NINO: {}", individual.getNino());
        }
        return incomeZerosRemoved;
    }
}