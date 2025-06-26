package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.{
  CommonOptionConfigTraits,
  CompNodeConfigElement
}
import gov.irs.factgraph.monads.Result
import org.scalatest.funspec.AnyFunSpec

class RegexSpec extends AnyFunSpec:
  describe("Regex") {
    it("returns true when the regex matches") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Regex",
          Seq(
            new CompNodeConfigElement(
              "Input",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("aaaaaa")
                )
              )
            ),
            new CompNodeConfigElement(
              "Pattern",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("a*")
                )
              )
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(true))
    }

    it("returns false when the regex doesn't match") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Regex",
          Seq(
            new CompNodeConfigElement(
              "Input",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("ababa")
                )
              )
            ),
            new CompNodeConfigElement(
              "Pattern",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("a*")
                )
              )
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(false))
    }

    it("can handle some basic pattern matching regex and pass") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Regex",
          Seq(
            new CompNodeConfigElement(
              "Input",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("45ffg")
                )
              )
            ),
            new CompNodeConfigElement(
              "Pattern",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("[a-z0-9]{5}")
                )
              )
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(true))
    }

    it("can handle some basic pattern matching regex and fail") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Regex",
          Seq(
            new CompNodeConfigElement(
              "Input",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("45ffg123")
                )
              )
            ),
            new CompNodeConfigElement(
              "Pattern",
              Seq(
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("[a-z0-9]{5}")
                )
              )
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(false))
    }

    it("can only handle string inputs") {
      assertThrows[IllegalArgumentException] {
        val node = CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Regex",
            Seq(
              new CompNodeConfigElement(
                "Input",
                Seq(
                  new CompNodeConfigElement(
                    "Int",
                    Seq.empty,
                    CommonOptionConfigTraits.value("999")
                  )
                )
              ),
              new CompNodeConfigElement(
                "Pattern",
                Seq(
                  new CompNodeConfigElement(
                    "String",
                    Seq.empty,
                    CommonOptionConfigTraits.value("[a-z0-9]")
                  )
                )
              )
            )
          )
        )
      }
    }
  }
