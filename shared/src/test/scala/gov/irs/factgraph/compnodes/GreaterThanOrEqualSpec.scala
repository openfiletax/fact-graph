package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result

class GreaterThanOrEqualSpec extends AnyFunSpec:
  describe("GreaterThanOrEqual") {
    it("compares Ints") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "GreaterThanOrEqual",
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

      assert(node.get(0) == Result.Complete(false))
    }

    it("compares Rationals") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "GreaterThanOrEqual",
          Seq(
            new CompNodeConfigElement(
              "Left",
              Seq(
                new CompNodeConfigElement(
                  "Rational",
                  Seq.empty,
                  CommonOptionConfigTraits.value("2/3")
                )
              )
            ),
            new CompNodeConfigElement(
              "Right",
              Seq(
                new CompNodeConfigElement(
                  "Rational",
                  Seq.empty,
                  CommonOptionConfigTraits.value("1/3")
                )
              )
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(true))
    }

    it("compares Dollars") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "GreaterThanOrEqual",
          Seq(
            new CompNodeConfigElement(
              "Left",
              Seq(
                new CompNodeConfigElement(
                  "Dollar",
                  Seq.empty,
                  CommonOptionConfigTraits.value("1.00")
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

      assert(node.get(0) == Result.Complete(true))
    }

    it("compares Days") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "GreaterThanOrEqual",
          Seq(
            new CompNodeConfigElement(
              "Left",
              Seq(
                new CompNodeConfigElement(
                  "Day",
                  Seq.empty,
                  CommonOptionConfigTraits.value("2022-01-01")
                )
              )
            ),
            new CompNodeConfigElement(
              "Right",
              Seq(
                new CompNodeConfigElement(
                  "Day",
                  Seq.empty,
                  CommonOptionConfigTraits.value("2022-01-01")
                )
              )
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(true))
    }

    it("requires numbers as inputs") {
      assertThrows[UnsupportedOperationException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "GreaterThanOrEqual",
            Seq(
              new CompNodeConfigElement(
                "Left",
                Seq(
                  new CompNodeConfigElement(
                    "String",
                    Seq.empty,
                    CommonOptionConfigTraits.value("Hello")
                  )
                )
              ),
              new CompNodeConfigElement(
                "Right",
                Seq(
                  new CompNodeConfigElement(
                    "String",
                    Seq.empty,
                    CommonOptionConfigTraits.value("World")
                  )
                )
              )
            )
          )
        )
      }
    }
  }
