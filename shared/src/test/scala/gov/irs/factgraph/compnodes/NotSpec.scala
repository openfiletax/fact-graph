package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.FactDictionary
import org.scalatest.funspec.AnyFunSpec

class NotSpec extends AnyFunSpec:
  describe("Not") {
    it("returns the opposite of its input") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "Not",
            Seq(
              new CompNodeConfigElement("True"),
            ),
          ),
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
                CommonOptionConfigTraits.value("42"),
              ),
            ),
          ),
        )
      }
    }
  }
