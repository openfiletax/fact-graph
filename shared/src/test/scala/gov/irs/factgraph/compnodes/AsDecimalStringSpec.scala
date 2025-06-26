package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result

class AsDecimalStringSpec extends AnyFunSpec:
  describe("AsDecimalString") {
    it(
      "returns the decimal string representation of a rational node and adds appropriate scale"
    ) {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "AsDecimalString",
            Seq(
              new CompNodeConfigElement(
                "Rational",
                Seq.empty,
                CommonOptionConfigTraits.value("3/10")
              )
            )
          )
        )
        .asInstanceOf[StringNode]

      assert(node.get(0) == Result.Complete("0.30"))
    }
    it("returns the expected scale for non-terminal rationals") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "AsDecimalString",
            Seq(
              new CompNodeConfigElement(
                "Rational",
                Seq.empty,
                CommonOptionConfigTraits.value("1/3")
              )
            )
          )
        )
        .asInstanceOf[StringNode]

      assert(node.get(0) == Result.Complete("0.33"))
    }
    it("has no issue representing 0") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "AsDecimalString",
            Seq(
              new CompNodeConfigElement(
                "Rational",
                Seq.empty,
                CommonOptionConfigTraits.value("0/100")
              )
            )
          )
        )
        .asInstanceOf[StringNode]

      assert(node.get(0) == Result.Complete("0.00"))
    }
    it("can represent negative rationals") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "AsDecimalString",
            Seq(
              new CompNodeConfigElement(
                "Rational",
                Seq.empty,
                CommonOptionConfigTraits.value("-1/3")
              )
            )
          )
        )
        .asInstanceOf[StringNode]

      assert(node.get(0) == Result.Complete("-0.33"))
    }
    it("Rounds up at the correct scale") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "AsDecimalString",
            Seq(
              new CompNodeConfigElement(
                "Rational",
                Seq.empty,
                CommonOptionConfigTraits.value("2/3")
              )
            )
          )
        )
        .asInstanceOf[StringNode]

      assert(node.get(0) == Result.Complete("0.67"))
    }
    it("Rounds uses the correct scale") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "AsDecimalString",
            Seq(
              new CompNodeConfigElement(
                "Rational",
                Seq.empty,
                CommonOptionConfigTraits.value("2/3")
              )
            ),
            CommonOptionConfigTraits.scale("5")
          )
        )
        .asInstanceOf[StringNode]

      assert(node.get(0) == Result.Complete("0.66667"))
    }

    it("throws on invalid input for scale - has decimal") {
      assertThrows[NoSuchElementException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "AsDecimalString",
            Seq(
              new CompNodeConfigElement(
                "Rational",
                Seq.empty,
                CommonOptionConfigTraits.value("2/3")
              )
            ),
            CommonOptionConfigTraits.scale("5.2")
          )
        )
      }
    }

    it("throws on invalid input for scale - only letters") {
      assertThrows[NoSuchElementException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "AsDecimalString",
            Seq(
              new CompNodeConfigElement(
                "Rational",
                Seq.empty,
                CommonOptionConfigTraits.value("2/3")
              )
            ),
            CommonOptionConfigTraits.scale("asdf")
          )
        )
      }
    }

    it("throws on a non-rational input") {
      assertThrows[UnsupportedOperationException] {
        CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "AsDecimalString",
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
