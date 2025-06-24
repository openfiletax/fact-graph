package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.*

class TruncateCentsSpec extends AnyFunSpec:
  describe("TruncateCents") {
    it("returns a rational that has the additional decimal places truncated") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "TruncateCents",
            Seq(
              new CompNodeConfigElement(
                "Rational",
                Seq.empty,
                CommonOptionConfigTraits.value("1/3")
              )
            )
          )
        )
        .asInstanceOf[RationalNode]

      assert(node.get(0) == Result.Complete(Rational("33/100")))
    }

    it(
      "returns a rational that has the additional decimal places truncated even when close to another number"
    ) {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "TruncateCents",
            Seq(
              new CompNodeConfigElement(
                "Rational",
                Seq.empty,
                CommonOptionConfigTraits.value("69980/1458")
              )
            )
          )
        )
        .asInstanceOf[RationalNode]

      assert(node.get(0) == Result.Complete(Rational("4799/100")))
    }

    it(
      "returns the same rational number when there is nothing to truncate"
    ) {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "TruncateCents",
            Seq(
              new CompNodeConfigElement(
                "Rational",
                Seq.empty,
                CommonOptionConfigTraits.value("-3/6")
              )
            )
          )
        )
        .asInstanceOf[RationalNode]

      assert(node.get(0) == Result.Complete(Rational("-1/2")))
    }
  }
