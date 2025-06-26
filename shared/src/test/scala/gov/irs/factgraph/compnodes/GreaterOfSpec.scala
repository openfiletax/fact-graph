package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.types.*
import gov.irs.factgraph.monads.Result

class GreaterOfSpec extends AnyFunSpec:
  describe("GreaterOf") {
    it("finds the maximum of a set of Ints") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "GreaterOf",
          Seq(
            new CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("1")
            ),
            new CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("2")
            ),
            new CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("3")
            )
          )
        )
      )
      assert(node.get(0) == Result.Complete(3))
    }

    it("finds the maximum of a set of Rationals") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "GreaterOf",
          Seq(
            new CompNodeConfigElement(
              "Rational",
              Seq.empty,
              CommonOptionConfigTraits.value("1/4")
            ),
            new CompNodeConfigElement(
              "Rational",
              Seq.empty,
              CommonOptionConfigTraits.value("3/4")
            ),
            new CompNodeConfigElement(
              "Rational",
              Seq.empty,
              CommonOptionConfigTraits.value("2/4")
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(Rational("3/4")))
    }

    it("finds the maximum of a set of Dollars") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "GreaterOf",
          Seq(
            new CompNodeConfigElement(
              "Dollar",
              Seq.empty,
              CommonOptionConfigTraits.value("4.56")
            ),
            new CompNodeConfigElement(
              "Dollar",
              Seq.empty,
              CommonOptionConfigTraits.value("7.89")
            ),
            new CompNodeConfigElement(
              "Dollar",
              Seq.empty,
              CommonOptionConfigTraits.value("1.23")
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(Dollar("7.89")))
    }

    it("finds the maximum of a set of days") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "GreaterOf",
          Seq(
            new CompNodeConfigElement(
              "Day",
              Seq.empty,
              CommonOptionConfigTraits.value("2022-01-01")
            ),
            new CompNodeConfigElement(
              "Day",
              Seq.empty,
              CommonOptionConfigTraits.value("2022-01-02")
            ),
            new CompNodeConfigElement(
              "Day",
              Seq.empty,
              CommonOptionConfigTraits.value("2022-01-03")
            )
          )
        )
      )
      assert(node.get(0) == Result.Complete(Day("2022-01-03")))
    }

    it("all inputs must be numbers of the same type") {
      assertThrows[UnsupportedOperationException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "GreaterOf",
            Seq(
              new CompNodeConfigElement(
                "String",
                Seq.empty,
                CommonOptionConfigTraits.value("Hello")
              ),
              new CompNodeConfigElement(
                "String",
                Seq.empty,
                CommonOptionConfigTraits.value("World")
              )
            )
          )
        )
      }

      assertThrows[UnsupportedOperationException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "GreaterOf",
            Seq(
              new CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("1")
              ),
              new CompNodeConfigElement(
                "Dollar",
                Seq.empty,
                CommonOptionConfigTraits.value("2.34")
              )
            )
          )
        )
      }
    }
  }
