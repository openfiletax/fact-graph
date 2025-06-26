package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.{
  CommonOptionConfigTraits,
  CompNodeConfigElement
}
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.*

class DivideSpec extends AnyFunSpec:
  describe("Divide") {
    it("divides Rationals") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Divide",
          Seq(
            new CompNodeConfigElement(
              "Dividend",
              Seq(
                new CompNodeConfigElement(
                  "Rational",
                  Seq.empty,
                  CommonOptionConfigTraits.value("1/2")
                )
              )
            ),
            new CompNodeConfigElement(
              "Divisors",
              Seq(
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
          )
        )
      )

      assert(node.get(0) == Result.Complete(Rational("1/1")))
    }

    it("divides Dollars") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Divide",
          Seq(
            new CompNodeConfigElement(
              "Dividend",
              Seq(
                new CompNodeConfigElement(
                  "Dollar",
                  Seq.empty,
                  CommonOptionConfigTraits.value("100.00")
                )
              )
            ),
            new CompNodeConfigElement(
              "Divisors",
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
                )
              )
            )
          )
        )
      )

      assert(node.get(0) == Result.Complete(Dollar("17.83")))
    }

    it("divides a long sequence of mixed types") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "Divide",
          Seq(
            new CompNodeConfigElement(
              "Dividend",
              Seq(
                new CompNodeConfigElement(
                  "Rational",
                  Seq.empty,
                  CommonOptionConfigTraits.value("6/7")
                )
              )
            ),
            new CompNodeConfigElement(
              "Divisors",
              Seq(
                new CompNodeConfigElement(
                  "Int",
                  Seq.empty,
                  CommonOptionConfigTraits.value("1")
                ),
                new CompNodeConfigElement(
                  "Rational",
                  Seq.empty,
                  CommonOptionConfigTraits.value("1/2")
                ),
                new CompNodeConfigElement(
                  "Dollar",
                  Seq.empty,
                  CommonOptionConfigTraits.value("3.45")
                ),
                new CompNodeConfigElement(
                  "Rational",
                  Seq.empty,
                  CommonOptionConfigTraits.value("1/2")
                ),
                new CompNodeConfigElement(
                  "Dollar",
                  Seq.empty,
                  CommonOptionConfigTraits.value("0.1")
                ),
                new CompNodeConfigElement(
                  "Int",
                  Seq.empty,
                  CommonOptionConfigTraits.value("5")
                )
              )
            )
          )
        )
      )
      assert(node.get(0) == Result.Complete(Dollar("2.00")))
    }

    describe("when dividing an Int and an Int") {
      it("returns a Rational") {
        val node = CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Divide",
            Seq(
              new CompNodeConfigElement(
                "Dividend",
                Seq(
                  new CompNodeConfigElement(
                    "Int",
                    Seq.empty,
                    CommonOptionConfigTraits.value("1")
                  )
                )
              ),
              new CompNodeConfigElement(
                "Divisors",
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
        assert(node.get(0) == Result.Complete(Rational("1/2")))
      }
    }

    describe("when dividing an Int and a Rational") {
      it("returns a Rational") {
        val node = CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Divide",
            Seq(
              new CompNodeConfigElement(
                "Dividend",
                Seq(
                  new CompNodeConfigElement(
                    "Int",
                    Seq.empty,
                    CommonOptionConfigTraits.value("1")
                  )
                )
              ),
              new CompNodeConfigElement(
                "Divisors",
                Seq(
                  new CompNodeConfigElement(
                    "Rational",
                    Seq.empty,
                    CommonOptionConfigTraits.value("2/3")
                  )
                )
              )
            )
          )
        )

        assert(node.get(0) == Result.Complete(Rational("3/2")))
      }
    }

    describe("when dividing an Int and a Dollar") {
      it("returns a Dollar") {
        val node = CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Divide",
            Seq(
              new CompNodeConfigElement(
                "Dividend",
                Seq(
                  new CompNodeConfigElement(
                    "Int",
                    Seq.empty,
                    CommonOptionConfigTraits.value("1")
                  )
                )
              ),
              new CompNodeConfigElement(
                "Divisors",
                Seq(
                  new CompNodeConfigElement(
                    "Dollar",
                    Seq.empty,
                    CommonOptionConfigTraits.value("0.25")
                  )
                )
              )
            )
          )
        )
        assert(node.get(0) == Result.Complete(Dollar("4.00")))
      }
    }

    describe("when dividing a Rational and a Dollar") {
      it("returns a Dollar") {
        val node = CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Divide",
            Seq(
              new CompNodeConfigElement(
                "Dividend",
                Seq(
                  new CompNodeConfigElement(
                    "Dollar",
                    Seq.empty,
                    CommonOptionConfigTraits.value("3.00")
                  )
                )
              ),
              new CompNodeConfigElement(
                "Divisors",
                Seq(
                  new CompNodeConfigElement(
                    "Rational",
                    Seq.empty,
                    CommonOptionConfigTraits.value("2/3")
                  )
                )
              )
            )
          )
        )
        assert(node.get(0) == Result.Complete(Dollar("4.50")))
      }

      it("does not preemptively round") {
        val node1 = CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Divide",
            Seq(
              new CompNodeConfigElement(
                "Dividend",
                Seq(
                  new CompNodeConfigElement(
                    "Dollar",
                    Seq.empty,
                    CommonOptionConfigTraits.value("100.00")
                  )
                )
              ),
              new CompNodeConfigElement(
                "Divisors",
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
        assert(node1.get(0) == Result.Complete(Dollar("300.00")))

        val node2 = CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "Divide",
            Seq(
              new CompNodeConfigElement(
                "Dividend",
                Seq(
                  new CompNodeConfigElement(
                    "Rational",
                    Seq.empty,
                    CommonOptionConfigTraits.value("1/3")
                  )
                )
              ),
              new CompNodeConfigElement(
                "Divisors",
                Seq(
                  new CompNodeConfigElement(
                    "Dollar",
                    Seq.empty,
                    CommonOptionConfigTraits.value("0.50")
                  )
                )
              )
            )
          )
        )
        assert(node2.get(0) == Result.Complete(Dollar("0.67")))
      }
    }

    describe("when the inputs are not numbers") {
      it("throws an exception") {
        assertThrows[UnsupportedOperationException] {
          val node = CompNode.fromDerivedConfig(
            new CompNodeConfigElement(
              "Divide",
              Seq(
                new CompNodeConfigElement(
                  "Dividend",
                  Seq(
                    new CompNodeConfigElement(
                      "String",
                      Seq.empty,
                      CommonOptionConfigTraits.value("Hello")
                    )
                  )
                ),
                new CompNodeConfigElement(
                  "Divisors",
                  Seq(
                    CompNodeConfigElement(
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

    describe("when one of the inputs is not a number") {
      it("throws an exception") {
        assertThrows[UnsupportedOperationException] {
          val node = CompNode.fromDerivedConfig(
            new CompNodeConfigElement(
              "Divide",
              Seq(
                new CompNodeConfigElement(
                  "Dividend",
                  Seq(
                    new CompNodeConfigElement(
                      "Int",
                      Seq.empty,
                      CommonOptionConfigTraits.value("3")
                    )
                  )
                ),
                new CompNodeConfigElement(
                  "Divisors",
                  Seq(
                    CompNodeConfigElement(
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
  }
