package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.*

class StepwiseMultiplySpec extends AnyFunSpec:
  describe("StepwiseMultiply") {
    describe("StepwiseMultiply") {
      it("always rounds down") {
        val node = CompNode.fromDerivedConfig(
          new CompNodeConfigElement(
            "StepwiseMultiply",
            Seq(
              new CompNodeConfigElement(
                "Multiplicand",
                Seq(
                  new CompNodeConfigElement(
                    "Dollar",
                    Seq.empty,
                    CommonOptionConfigTraits.value("9999.99")
                  )
                )
              ),
              new CompNodeConfigElement(
                "Rate",
                Seq(
                  new CompNodeConfigElement(
                    "Rational",
                    Seq.empty,
                    CommonOptionConfigTraits.value("50/1000")
                  )
                )
              )
            )
          )
        )

        assert(node.get(0) == Result.Complete(Dollar("450.00")))
      }

      it("must have a dollar and a rational as inputs") {
        assertThrows[UnsupportedOperationException] {
          CompNode.fromDerivedConfig(
            new CompNodeConfigElement(
              "StepwiseMultiply",
              Seq(
                new CompNodeConfigElement(
                  "Multiplicand",
                  Seq(
                    new CompNodeConfigElement(
                      "String",
                      Seq.empty,
                      CommonOptionConfigTraits.value("Hello")
                    )
                  )
                ),
                new CompNodeConfigElement(
                  "Rate",
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
  }
