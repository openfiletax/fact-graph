package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result

class TrimSpec extends AnyFunSpec:
  describe("Trim") {
    it("returns the trimmed version of its input") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "Trim",
            Seq(
              new CompNodeConfigElement(
                "String",
                Seq.empty,
                CommonOptionConfigTraits.value("  No mud  ")
              )
            )
          )
        )
        .asInstanceOf[StringNode]
      assert(node.get(0) == Result.Complete(String("No mud")))
    }
    it("returns the trimmed version, collapses internal spaces") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "Trim",
            Seq(
              new CompNodeConfigElement(
                "String",
                Seq.empty,
                CommonOptionConfigTraits.value("  No     lotus  ")
              )
            )
          )
        )
        .asInstanceOf[StringNode]
      assert(node.get(0) == Result.Complete(String("No lotus")))
    }

  }
