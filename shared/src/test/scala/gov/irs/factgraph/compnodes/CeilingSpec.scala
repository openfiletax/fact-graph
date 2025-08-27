package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.FactDictionary
import org.scalatest.funspec.AnyFunSpec

class Ceiling extends AnyFunSpec:
  describe("Ceiling") {
    it("Ceilings a rational to the nearest, higher, Int") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "Ceiling",
            Seq(
              new CompNodeConfigElement(
                "Rational",
                Seq.empty,
                CommonOptionConfigTraits.value("1/3"),
              ),
            ),
          ),
        )

      val integerNode = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "Ceiling",
            Seq(
              new CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("1"),
              ),
            ),
          ),
        )

      assert(node.get(0) == Result.Complete(1))
      assert(integerNode.get(0) == Result.Complete(1))
    }
    it("Ceilings negative values to the nearest, higher, Int") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "Ceiling",
            Seq(
              new CompNodeConfigElement(
                "Rational",
                Seq.empty,
                CommonOptionConfigTraits.value("-1/2"),
              ),
            ),
          ),
        )

      val integerNode = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "Ceiling",
            Seq(
              new CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("-1"),
              ),
            ),
          ),
        )

      assert(node.get(0) == Result.Complete(0))
      assert(integerNode.get(0) == Result.Complete(-1))
    }
  }
