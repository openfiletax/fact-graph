package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result

class Floor extends AnyFunSpec:
  describe("Floor") {
    it("Floors a rational to the nearest, lower, Int") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "Floor",
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
            "Floor",
            Seq(
              new CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("1"),
              ),
            ),
          ),
        )


      assert(node.get(0) == Result.Complete(0))
      assert(integerNode.get(0) == Result.Complete(1))
    }

    it("Floors a negative rational to the nearest, lower, Int") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "Floor",
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
            "Floor",
            Seq(
              new CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("-1"),
              ),
            ),
          ),
        )


      assert(node.get(0) == Result.Complete(-1))
      assert(integerNode.get(0) == Result.Complete(-1))
    }
  }