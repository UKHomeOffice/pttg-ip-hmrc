package uk.gov.digital.ho.pttg

import spock.lang.Specification
import uk.gov.digital.ho.pttg.application.DataCleanser
import uk.gov.digital.ho.pttg.dto.Income
import uk.gov.digital.ho.pttg.dto.Individual

class DataCleanserSpec extends Specification {


    def 'should allow duplicate income payments with same payment details'() {

        def incomeInput = Arrays.asList(
                new Income("EE", new BigDecimal(1200.00), new BigDecimal(1300.00), "2017-06-06", 6, 4),
                new Income("EE", new BigDecimal(1200.00), new BigDecimal(1300.00), "2017-06-06", 6, 4),
                new Income("EE", new BigDecimal(1200.00), new BigDecimal(1300.00), "2017-06-07", 7, 5),
                new Income("EE", new BigDecimal(1200.00), new BigDecimal(1300.00), "2017-06-08", 8, 6))
        when:
        def incomes = DataCleanser.clean(new Individual("", "", "12344", null),
                new ArrayList<>(incomeInput))
        then:
        incomes.size()==4
    }

    def 'should remove zero income payments'() {

        def incomeInput = Arrays.asList(
                new Income("EE", new BigDecimal(1200.00), new BigDecimal(1300.00), "2017-06-06", 6, 4),
                new Income("EE", new BigDecimal(0), new BigDecimal(0), "2017-06-06", 6, 4),
                new Income("EE", new BigDecimal(0), new BigDecimal(0), "2017-05-06", 4, 4))
        when:
        def incomes = DataCleanser.clean(new Individual("", "", "", null),
                new ArrayList<>(incomeInput))
        then:
        incomes.size()==1
    }

    def 'should rreturn all non zero from different months'() {

        def incomeInput = new ArrayList<>(Arrays.asList(
                new Income("EE", new BigDecimal(1200.00), new BigDecimal(1300.00), "2017-06-06", 6, 4),
                new Income("EE", new BigDecimal(1200.00), new BigDecimal(1300.00), "2017-06-06", null, 2)))
        when:
        def incomes = DataCleanser.clean(new Individual("", "", "", null),
                incomeInput)
        then:
        incomes.size()==2
    }


    def 'should handle 0 incomes'() {

        def incomeInput = new ArrayList<>()
        when:
        def incomes = DataCleanser.clean(new Individual("", "", "", null),
                incomeInput)
        then:
        incomes.size()==0
    }

    def 'should null incomes'() {

        when:
        def incomes = DataCleanser.clean(new Individual("", "", "", null),
                null)
        then:
        incomes==null
    }
}
