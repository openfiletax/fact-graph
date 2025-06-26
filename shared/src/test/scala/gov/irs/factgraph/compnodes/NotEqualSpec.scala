package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result

class NotEqualSpec extends AnyFunSpec:
  describe("NotEqual") {
    it("returns true if the inputs are different") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "NotEqual",
          Seq(
            new CompNodeConfigElement(
              "Left",
              Seq(
                new CompNodeConfigElement(
                  "Int",
                  Seq.empty,
                  CommonOptionConfigTraits.value("1")
                )
              )
            ),
            new CompNodeConfigElement(
              "Right",
              Seq(
                new CompNodeConfigElement(
                  "Int",
                  Seq.empty,
                  CommonOptionConfigTraits.value("2")
                )
              )
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(true))
    }

    it("returns false if the inputs are the same") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "NotEqual",
          Seq(
            new CompNodeConfigElement(
              "Left",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("Test")
                )
              )
            ),
            new CompNodeConfigElement(
              "Right",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("Test")
                )
              )
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(false))
    }

    it("requires both inputs to be of the same type") {
      assertThrows[UnsupportedOperationException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "NotEqual",
            Seq(
              new CompNodeConfigElement(
                "Left",
                Seq(
                  new CompNodeConfigElement(
                    "Int",
                    Seq.empty,
                    CommonOptionConfigTraits.value("1")
                  )
                )
              ),
              new CompNodeConfigElement(
                "Right",
                Seq(
                  new CompNodeConfigElement(
                    "Dollar",
                    Seq.empty,
                    CommonOptionConfigTraits.value("1.00")
                  )
                )
              )
            )
          )
        )
      }
    }
  }
