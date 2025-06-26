package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result

class NotSpec extends AnyFunSpec:
  describe("Not") {
    it("returns the opposite of its input") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "Not",
            Seq(
              new CompNodeConfigElement("True")
            )
          )
        )
        .asInstanceOf[BooleanNode]

      assert(node.get(0) == Result.Complete(false))
    }

    it("requires a boolean input") {
      assertThrows[UnsupportedOperationException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Not",
            Seq(
              new CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("42")
              )
            )
          )
        )
      }
    }
  }
