package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.{
  CommonOptionConfigTraits,
  CompNodeConfigElement
}
import gov.irs.factgraph.monads.Result
import org.scalatest.funspec.AnyFunSpec

class LengthSpec extends AnyFunSpec:
  describe("Length") {
    it("is used with string nodes") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Length",
          Seq(
            new CompNodeConfigElement(
              "String",
              Seq.empty,
              CommonOptionConfigTraits.value("Test")
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(4))
    }

    it("is can't work with other nodes") {
      assertThrows[UnsupportedOperationException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Length",
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
