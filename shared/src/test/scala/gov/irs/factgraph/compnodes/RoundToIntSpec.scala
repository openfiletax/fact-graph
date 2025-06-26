package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result

class RoundToInt extends AnyFunSpec:
  describe("RoundToInt") {
    it("rounds up to the dollar amount and returns an int") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "RoundToInt",
          Seq(
            new CompNodeConfigElement(
              "Dollar",
              Seq.empty,
              CommonOptionConfigTraits.value("1.50")
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(2))
    }

    it("it rounds down and returns an int") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "RoundToInt",
          Seq(
            new CompNodeConfigElement(
              "Dollar",
              Seq.empty,
              CommonOptionConfigTraits.value("2.05")
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(2))
    }

  }
