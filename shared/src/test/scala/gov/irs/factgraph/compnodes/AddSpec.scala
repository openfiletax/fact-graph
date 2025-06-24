package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.{
  CommonOptionConfigTraits,
  CompNodeConfigElement
}
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.*

class AddSpec extends AnyFunSpec:
  describe("Add config") {
    it("adds Ints") {
      val config = new CompNodeConfigElement(
        "Add",
        Seq(
          CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("1")
          ),
          CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("2")
          ),
          CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("3")
          )
        )
      )
      val node = CompNode
        .fromDerivedConfig(config)
      assert(node.get(0) == Result.Complete(6))
    }

    it("adds Rationals") {
      val config = new CompNodeConfigElement(
        "Add",
        Seq(
          CompNodeConfigElement(
            "Rational",
            Seq.empty,
            CommonOptionConfigTraits.value("1/2")
          ),
          CompNodeConfigElement(
            "Rational",
            Seq.empty,
            CommonOptionConfigTraits.value("2/3")
          ),
          CompNodeConfigElement(
            "Rational",
            Seq.empty,
            CommonOptionConfigTraits.value("3/4")
          )
        )
      )
      val node = CompNode.fromDerivedConfig(config)
      assert(node.get(0) == Result.Complete(Rational("23/12")))
    }

    it("adds Dollars") {
      val config = new CompNodeConfigElement(
        "Add",
        Seq(
          CompNodeConfigElement(
            "Dollar",
            Seq.empty,
            CommonOptionConfigTraits.value("1.23")
          ),
          CompNodeConfigElement(
            "Dollar",
            Seq.empty,
            CommonOptionConfigTraits.value("4.56")
          ),
          CompNodeConfigElement(
            "Dollar",
            Seq.empty,
            CommonOptionConfigTraits.value("7.89")
          )
        )
      )
      val node = CompNode.fromDerivedConfig(config)
      assert(node.get(0) == Result.Complete(Dollar("13.68")))
    }

    it("adds a long sequence of mixed types") {
      val config = new CompNodeConfigElement(
        "Add",
        Seq(
          CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("0")
          ),
          CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("1")
          ),
          CompNodeConfigElement(
            "Rational",
            Seq.empty,
            CommonOptionConfigTraits.value("0/1")
          ),
          CompNodeConfigElement(
            "Rational",
            Seq.empty,
            CommonOptionConfigTraits.value("2/3")
          ),
          CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("4")
          ),
          CompNodeConfigElement(
            "Dollar",
            Seq.empty,
            CommonOptionConfigTraits.value("5.67")
          ),
          CompNodeConfigElement(
            "Rational",
            Seq.empty,
            CommonOptionConfigTraits.value("8/9")
          ),
          CompNodeConfigElement(
            "Dollar",
            Seq.empty,
            CommonOptionConfigTraits.value("10.11")
          ),
          CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("12")
          )
        )
      )
      val node = CompNode.fromDerivedConfig(config)
      assert(node.get(0) == Result.Complete(Dollar("34.34")))
    }

    describe("when adding an Int and a Rational") {
      it("returns a Rational") {
        val config = new CompNodeConfigElement(
          "Add",
          Seq(
            CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("1")
            ),
            CompNodeConfigElement(
              "Rational",
              Seq.empty,
              CommonOptionConfigTraits.value("2/3")
            )
          )
        )
        val node = CompNode.fromDerivedConfig(config)
        assert(node.get(0) == Result.Complete(Rational("5/3")))
      }
    }

    describe("when adding an Int and a Dollar") {
      it("returns a Dollar") {
        val config = new CompNodeConfigElement(
          "Add",
          Seq(
            CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("1")
            ),
            CompNodeConfigElement(
              "Dollar",
              Seq.empty,
              CommonOptionConfigTraits.value("2.34")
            )
          )
        )
        val node = CompNode.fromDerivedConfig(config)
        assert(node.get(0) == Result.Complete(Dollar("3.34")))
      }
    }

    describe("when adding a Rational and a Dollar") {
      it("returns a Dollar") {
        val config = new CompNodeConfigElement(
          "Add",
          Seq(
            CompNodeConfigElement(
              "Rational",
              Seq.empty,
              CommonOptionConfigTraits.value("1/2")
            ),
            CompNodeConfigElement(
              "Dollar",
              Seq.empty,
              CommonOptionConfigTraits.value("3.45")
            )
          )
        )
        val node = CompNode.fromDerivedConfig(config)
        assert(node.get(0) == Result.Complete(Dollar("3.95")))
      }
    }

    describe("when the inputs are not numbers") {
      it("throws an exception") {
        val config = new CompNodeConfigElement(
          "Add",
          Seq(
            CompNodeConfigElement(
              "String",
              Seq.empty,
              CommonOptionConfigTraits.value("Hello")
            ),
            CompNodeConfigElement(
              "String",
              Seq.empty,
              CommonOptionConfigTraits.value("World")
            )
          )
        )
        assertThrows[UnsupportedOperationException] {
          CompNode.fromDerivedConfig(config)
        }
      }
    }

    describe("when one of the inputs is not a number") {
      it("throws an exception") {
        val config = new CompNodeConfigElement(
          "Add",
          Seq(
            CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("3")
            ),
            CompNodeConfigElement(
              "String",
              Seq.empty,
              CommonOptionConfigTraits.value("Stooges")
            )
          )
        )
        assertThrows[UnsupportedOperationException] {
          CompNode.fromDerivedConfig(config)
        }
      }
    }
  }
