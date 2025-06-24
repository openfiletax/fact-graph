package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.*

class MultiplySpec extends AnyFunSpec:
  describe("Multiply") {
    it("multiplies Ints") {
      val config = new CompNodeConfigElement(
        "Multiply",
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
      val node = CompNode.fromDerivedConfig(config)

      assert(node.get(0) == Result.Complete(6))
    }

    it("multiplies Rationals") {
      val config = new CompNodeConfigElement(
        "Multiply",
        Seq(
          new CompNodeConfigElement(
            "Rational",
            Seq.empty,
            CommonOptionConfigTraits.value("1/2")
          ),
          new CompNodeConfigElement(
            "Rational",
            Seq.empty,
            CommonOptionConfigTraits.value("2/3")
          ),
          new CompNodeConfigElement(
            "Rational",
            Seq.empty,
            CommonOptionConfigTraits.value("3/4")
          )
        )
      )
      val node = CompNode.fromDerivedConfig(config)

      assert(node.get(0) == Result.Complete(Rational("1/4")))
    }

    it("multiplies Dollars") {
      val config = new CompNodeConfigElement(
        "Multiply",
        Seq(
          new CompNodeConfigElement(
            "Dollar",
            Seq.empty,
            CommonOptionConfigTraits.value("1.23")
          ),
          new CompNodeConfigElement(
            "Dollar",
            Seq.empty,
            CommonOptionConfigTraits.value("4.56")
          ),
          new CompNodeConfigElement(
            "Dollar",
            Seq.empty,
            CommonOptionConfigTraits.value("7.89")
          )
        )
      )
      val node = CompNode.fromDerivedConfig(config)

      assert(node.get(0) == Result.Complete(Dollar("44.26")))
    }

    it("multiplies a long sequence of mixed types") {
      val config = new CompNodeConfigElement(
        "Multiply",
        Seq(
          new CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("1")
          ),
          new CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("1")
          ),
          new CompNodeConfigElement(
            "Rational",
            Seq.empty,
            CommonOptionConfigTraits.value("2/3")
          ),
          new CompNodeConfigElement(
            "Rational",
            Seq.empty,
            CommonOptionConfigTraits.value("2/3")
          ),
          new CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("4")
          ),
          new CompNodeConfigElement(
            "Dollar",
            Seq.empty,
            CommonOptionConfigTraits.value("5.67")
          ),
          new CompNodeConfigElement(
            "Rational",
            Seq.empty,
            CommonOptionConfigTraits.value("8/9")
          ),
          new CompNodeConfigElement(
            "Dollar",
            Seq.empty,
            CommonOptionConfigTraits.value("10.11")
          ),
          new CompNodeConfigElement(
            "Int",
            Seq.empty,
            CommonOptionConfigTraits.value("12")
          )
        )
      )
      val node = CompNode.fromDerivedConfig(config)

      assert(node.get(0) == Result.Complete(Dollar("1087.08")))
    }

    describe("when multiplying an Int and a Rational") {
      it("returns a Rational") {
        val config = new CompNodeConfigElement(
          "Multiply",
          Seq(
            new CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("2")
            ),
            new CompNodeConfigElement(
              "Rational",
              Seq.empty,
              CommonOptionConfigTraits.value("3/4")
            )
          )
        )
        val node = CompNode.fromDerivedConfig(config)

        assert(node.get(0) == Result.Complete(Rational("3/2")))
      }
    }

    describe("when multiplying an Int and a Dollar") {
      it("returns a Dollar") {
        val config = new CompNodeConfigElement(
          "Multiply",
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
        val node = CompNode.fromDerivedConfig(config).asInstanceOf[DollarNode]

        assert(node.get(0) == Result.Complete(Dollar("2.34")))
      }
    }

    describe("when multiplying a Rational and a Dollar") {
      it("returns a Dollar") {
        val config = new CompNodeConfigElement(
          "Multiply",
          Seq(
            new CompNodeConfigElement(
              "Rational",
              Seq.empty,
              CommonOptionConfigTraits.value("3/4")
            ),
            new CompNodeConfigElement(
              "Dollar",
              Seq.empty,
              CommonOptionConfigTraits.value("2.00")
            )
          )
        )
        val node = CompNode.fromDerivedConfig(config).asInstanceOf[DollarNode]

        assert(node.get(0) == Result.Complete(Dollar("1.50")))
      }

      it("does not preemptively round") {
        val node1 = CompNode
          .fromDerivedConfig(
            new CompNodeConfigElement(
              "Multiply",
              Seq(
                new CompNodeConfigElement(
                  "Dollar",
                  Seq.empty,
                  CommonOptionConfigTraits.value("100.00")
                ),
                new CompNodeConfigElement(
                  "Rational",
                  Seq.empty,
                  CommonOptionConfigTraits.value("1/3")
                )
              )
            )
          )
          .asInstanceOf[DollarNode]

        assert(node1.get(0) == Result.Complete(Dollar("33.33")))

        val node2 = CompNode
          .fromDerivedConfig(
            new CompNodeConfigElement(
              "Multiply",
              Seq(
                new CompNodeConfigElement(
                  "Rational",
                  Seq.empty,
                  CommonOptionConfigTraits.value("1/3")
                ),
                new CompNodeConfigElement(
                  "Dollar",
                  Seq.empty,
                  CommonOptionConfigTraits.value("100.00")
                )
              )
            )
          )
          .asInstanceOf[DollarNode]

        assert(node2.get(0) == Result.Complete(Dollar("33.33")))
      }
    }

    describe("when the inputs are not numbers") {
      it("throws an exception") {
        assertThrows[UnsupportedOperationException] {
          CompNode.fromDerivedConfig(
            new CompNodeConfigElement(
              "Multiply",
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
      }
    }

    describe("when one of the inputs is not a number") {
      it("throws an exception") {
        assertThrows[UnsupportedOperationException] {
          CompNode.fromDerivedConfig(
            new CompNodeConfigElement(
              "Multiply",
              Seq(
                new CompNodeConfigElement(
                  "Int",
                  Seq.empty,
                  CommonOptionConfigTraits.value("3")
                ),
                new CompNodeConfigElement(
                  "String",
                  Seq.empty,
                  CommonOptionConfigTraits.value("Stooges")
                )
              )
            )
          )
        }
      }
    }
  }
