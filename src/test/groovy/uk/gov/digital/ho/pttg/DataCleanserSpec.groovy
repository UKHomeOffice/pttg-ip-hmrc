package uk.gov.digital.ho.pttg

import spock.lang.Specification
import uk.gov.digital.ho.pttg.application.DataCleanser
import uk.gov.digital.ho.pttg.dto.Income

class DataCleanserSpec extends Specification {


    def 'should allow duplicate income payments with same payment details'() {

        def incomeInput = Arrays.asList(
                new Income("EE", new BigDecimal(1200.00), new BigDecimal(1300.00), "2017-06-06", 6, 4, "IRREGULAR"),
                new Income("EE", new BigDecimal(1200.00), new BigDecimal(1300.00), "2017-06-06", 6, 4, "IRREGULAR"),
                new Income("EE", new BigDecimal(1200.00), new BigDecimal(1300.00), "2017-06-07", 7, 5, "IRREGULAR"),
                new Income("EE", new BigDecimal(1200.00), new BigDecimal(1300.00), "2017-06-08", 8, 6, "IRREGULAR"))
        when:
        def incomes = DataCleanser.clean(new ArrayList<>(incomeInput))
        then:
        incomes.size()==4
    }

    def 'should remove zero income payments'() {

        def incomeInput = Arrays.asList(
                new Income("EE", new BigDecimal(1200.00), new BigDecimal(1300.00), "2017-06-06", 6, 4, "IRREGULAR"),
                new Income("EE", new BigDecimal(0), new BigDecimal(0), "2017-06-06", 6, 4, "IRREGULAR"),
                new Income("EE", new BigDecimal(0), new BigDecimal(0), "2017-05-06", 4, 4, "IRREGULAR"))
        when:
        def incomes = DataCleanser.clean(new ArrayList<>(incomeInput))
        then:
        incomes.size()==1
    }

    def 'should return all non zero from different months'() {

        def incomeInput = new ArrayList<>(Arrays.asList(
                new Income("EE", new BigDecimal(1200.00), new BigDecimal(1300.00), "2017-06-06", 6, 4, "IRREGULAR"),
                new Income("EE", new BigDecimal(1200.00), new BigDecimal(1300.00), "2017-06-06", null, 2, "IRREGULAR")))
        when:
        def incomes = DataCleanser.clean(incomeInput)
        then:
        incomes.size()==2
    }


    def 'should handle 0 incomes'() {

        def incomeInput = new ArrayList<>()
        when:
        def incomes = DataCleanser.clean(incomeInput)
        then:
        incomes.size()==0
    }

    def 'should null incomes'() {

        when:
        def incomes = DataCleanser.clean(null)
        then:
        incomes==null
    }
}
