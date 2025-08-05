package gov.irs.factgraph

import gov.irs.factgraph.types.*
import org.scalatest.funsuite.AnyFunSuite

import scala.io.Source

/**
  *
  * This test is meant to be illustrative of:
  *  1. How to instantiate a fact dictionary (FactDictionary) from an xml file.
  *  2. How to instantiate a fact graph (Graph) from a FactDictionary.
  *  3. How to set writable facts programatically using Graph#set.
  *  4. How to compute the fact graph using Graph#save.
  *  5. Some idomatic Scala syntax.
  *
  * When you write your tests, ensure you are testing the logic.
  * These tests are testing the logic, but the logic boils down to straight math.
  * Your tests will likely be more involved.
  *
  * Run the following command from the root `fact-graph`
  * `sbt "factGraphJVM/testOnly *ExampleAgiSpec"`
  *
  * Happy Tax Logicizing!
  */

class ExampleAgiSpec extends AnyFunSuite {
    //Load the XML file from disk as a String
    val factsFile = Source.fromResource("exampleAgiFacts.xml").getLines().mkString("\n")
    //Instantiate a FactDictionary object from the XML
    val factDictionary = FactDictionary.importFromXml(factsFile)

    private def makeGraphWith(facts: (Path, WritableType)*): Graph = {
        //Instantiate a Graph object
        val graph = Graph(factDictionary)
        facts.foreach {case (path, value) =>
            graph.set(path, value) }
        graph
    }

    val totalWages = Path("/totalWages")
    val totalInterestIncome = Path("/totalInterestIncome")
    val total1099Income = Path("/total1099Income")
    val totalAdjustments = Path("/totalAdjustments")
    val totalAdjustedGrossIncome = Path("/totalAdjustedGrossIncome")
    val yearOfBirth = Path("/yearOfBirth")
    val taxYear = Path("/taxYear")
    val age = Path("/age")
    val age16OrOlder = Path("/age16OrOlder")
    val ageCalculatedByYear = Path("/ageCalculatedByYear")
    val isUsCitizen = Path("/isUsCitizen")
    val eligibleForDirectFile = Path("/eligibleForDirectFile")
    val filingDeadline2026 = Path("/filingDeadline2026")
    val dateOfBirth = Path("/dateOfBirth")
    val ageAtDeadline2026 = Path("/ageAtDeadline2026")

    test("calculates AGI correctly given positive wage, 1099, and interest income values, minus non-negative adjustments") {
        //Given taxpayer has non-zero, non-negative income from all 3 income types and a non-zero, non-negative adjustment amount
        val graph = makeGraphWith(
            totalWages -> Dollar(50000.00),
            totalInterestIncome -> Dollar(1500.00),
            total1099Income -> Dollar(3000.00),
            totalAdjustments -> Dollar(2000.00))

        //When AGI is calculated
        graph.save()

         //Then the AGI should be the sum of income minus adjustments
        val agi = graph.get(totalAdjustedGrossIncome)
        assert(agi.value.contains(Dollar(52500.00)))
    }

    test("handles zero value income types and adjustments correctly") {
        //Given all income types are zero
        val graph = makeGraphWith(
            totalWages -> Dollar(0.00),
            totalInterestIncome -> Dollar(0.00),
            total1099Income -> Dollar(0.00),
            totalAdjustments -> Dollar(0.00))

        //When AGI is calculated
        graph.save()

         //Then the AGI should be the sum of income minus adjustments
        val agi = graph.get(totalAdjustedGrossIncome)
        assert(agi.value.contains(Dollar(0.00)))
    }

    test("calculates a negative amount when adjustments greater than combined income values") {
        //Given income values that sum up to be less than the adjustment amount
        val graph = makeGraphWith(
            totalWages -> Dollar(10000.00),
            totalInterestIncome -> Dollar(1500.00),
            total1099Income -> Dollar(3000.00),
            totalAdjustments -> Dollar(20000.00))

        //When AGI is calculated
        graph.save()

         //Then the AGI should be the sum of income minus adjustments
        val agi = graph.get(totalAdjustedGrossIncome)
        assert(agi.value.contains(Dollar(-5500.00)))
    }

    test("calculates partial income correctly") {
        //Given 0 wages, but 1099 and interest income, and a higher adjustment amount
        val graph = makeGraphWith(
            totalWages -> Dollar(0.00),
            totalInterestIncome -> Dollar(1500.00),
            total1099Income -> Dollar(3000.00),
            totalAdjustments -> Dollar(20000.00))

        //When AGI is calculated
        graph.save()

         //Then the AGI should be the sum of income minus adjustments
        val agi = graph.get(totalAdjustedGrossIncome)
        assert(agi.value.contains(Dollar(-15500.00)))
    }

     test("handles large numbers") {
        //Given taxpayer has non-zero, non-negative income from all 3 income types and a non-zero, non-negative adjustment amount
        val graph = makeGraphWith(
            totalWages -> Dollar(5000000.00),
            totalInterestIncome -> Dollar(1500000.00),
            total1099Income -> Dollar(300000.00),
            totalAdjustments -> Dollar(2000000.00))

        //When AGI is calculated
        graph.save()

         //Then the AGI should be the sum of income minus adjustments
        val agi = graph.get(totalAdjustedGrossIncome)
        assert(agi.value.contains(Dollar(4800000.00))) // 5MM + 1.5MM + 300K - 2MM
     }

    test("Taxpayer is 16 or older and a US Citizen") {
        val graph = makeGraphWith(
            yearOfBirth -> 2009,
            isUsCitizen -> true
        )

        graph.save()

        val is16OrOlder = graph.get(age16OrOlder)
        val taxpayerAge = graph.get(age)
        val isEligibleForDirectFile = graph.get(eligibleForDirectFile)
        assert(is16OrOlder.value.contains(true))
        assert(taxpayerAge.value.contains(16))
        assert(isEligibleForDirectFile.value.contains(true))
    }

    test("Taxpayer is under 16 and a US Citizen") {
        val graph = makeGraphWith(
            yearOfBirth -> 2010,
            isUsCitizen -> true
        )

        graph.save()

        val is16OrOlder = graph.get(age16OrOlder)
        val taxpayerAge = graph.get(age)
        val isEligibleForDirectFile = graph.get(eligibleForDirectFile)
        assert(!is16OrOlder.value.contains(true))
        assert(taxpayerAge.value.contains(15))
        assert(!isEligibleForDirectFile.value.contains(true))
    }

    test("Taxpayer is 16 or older and not a US Citizen") {
        val graph = makeGraphWith(
            yearOfBirth -> 2009,
            isUsCitizen -> false
        )

        graph.save()

        val is16OrOlder = graph.get(age16OrOlder)
        val taxpayerAge = graph.get(age)
        val isEligibleForDirectFile = graph.get(eligibleForDirectFile)
        assert(is16OrOlder.value.contains(true))
        assert(taxpayerAge.value.contains(16))
        assert(!isEligibleForDirectFile.value.contains(true))
    }

    test("Taxpayer is under 16 and not a US Citizen") {
        val graph = makeGraphWith(
            yearOfBirth -> 2010,
            isUsCitizen -> false
        )

        graph.save()

        val is16OrOlder = graph.get(age16OrOlder)
        val taxpayerAge = graph.get(age)
        val isEligibleForDirectFile = graph.get(eligibleForDirectFile)
        assert(!is16OrOlder.value.contains(true))
        assert(taxpayerAge.value.contains(15))
        assert(!isEligibleForDirectFile.value.contains(true))
    }

    test("Year returns correctly") {
        val graph = makeGraphWith(
            dateOfBirth -> Day("2000-01-01"),
            filingDeadline2026 -> Day("2026-04-15")
        )

        graph.save()

        val tpDateOfBirth: Day = graph.get(dateOfBirth).get.asInstanceOf[Day]
        assert(tpDateOfBirth.year == 2000)
        val deadlineYear = graph.get(filingDeadline2026).get.asInstanceOf[Day]
        assert(deadlineYear.year == 2026)
        val tpAgeAtDeadline2026 = graph.get(ageAtDeadline2026)
        assert(tpAgeAtDeadline2026.value.contains(26))
    }

}
