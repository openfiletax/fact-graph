package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.Day
import gov.irs.factgraph.FactDictionary
import org.scalatest.funspec.AnyFunSpec

class AddPayrollMonthsSpec extends AnyFunSpec:
  describe("AddPayrollMonths") {
    it("handles increasing date") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "AddPayrollMonths",
          Seq(
            new CompNodeConfigElement(
              "Day",
              Seq.empty,
              CommonOptionConfigTraits.value("2025-02-28"),
            ),
            new CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("1"),
            ),
          ),
        ),
      )

      assert(node.get(0) == Result.Complete(Day("2025-03-31")))
    }
    it("handles decreasing date") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "AddPayrollMonths",
          Seq(
            new CompNodeConfigElement(
              "Day",
              Seq.empty,
              CommonOptionConfigTraits.value("2025-03-31"),
            ),
            new CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("1"),
            ),
          ),
        ),
      )

      assert(node.get(0) == Result.Complete(Day("2025-04-30")))
    }

    it("uses standard logic when the day is not the last day of the month") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "AddPayrollMonths",
          Seq(
            new CompNodeConfigElement(
              "Day",
              Seq.empty,
              CommonOptionConfigTraits.value("2025-01-30"),
            ),
            new CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("2"),
            ),
          ),
        ),
      )

      assert(node.get(0) == Result.Complete(Day("2025-03-30")))
    }
  }
