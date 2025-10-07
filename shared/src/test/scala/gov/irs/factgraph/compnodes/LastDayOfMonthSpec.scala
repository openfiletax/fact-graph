package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.Day
import gov.irs.factgraph.FactDictionary
import org.scalatest.funspec.AnyFunSpec

class LastDayOfMonthSpec extends AnyFunSpec:
  describe("LastDayOfMonth") {
    it("handles leap years") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "LastDayOfMonth",
          Seq(
            new CompNodeConfigElement(
              "Day",
              Seq.empty,
              CommonOptionConfigTraits.value("2024-02-01"),
            ),
          ),
        ),
      )
      assert(node.get(0) == Result.Complete(Day("2024-02-29")))
    }

    it("handles non leap year Feb") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "LastDayOfMonth",
          Seq(
            new CompNodeConfigElement(
              "Day",
              Seq.empty,
              CommonOptionConfigTraits.value("2025-02-01"),
            ),
          ),
        ),
      )
      assert(node.get(0) == Result.Complete(Day("2025-02-28")))
    }

    it("value doesn't change if it's already the last day of the month") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "LastDayOfMonth",
          Seq(
            new CompNodeConfigElement(
              "Day",
              Seq.empty,
              CommonOptionConfigTraits.value("2025-05-31"),
            ),
          ),
        ),
      )
      assert(node.get(0) == Result.Complete(Day("2025-05-31")))
    }

    it("throws errors when types don't match") {
      assertThrows[UnsupportedOperationException] {
        val node = CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "LastDayOfMonth",
            Seq(
              new CompNodeConfigElement(
                "Days",
                Seq.empty,
                CommonOptionConfigTraits.value("10"),
              ),
            ),
          ),
        )
      }
    }
  }
