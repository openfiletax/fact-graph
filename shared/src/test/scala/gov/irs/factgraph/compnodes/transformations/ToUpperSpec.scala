package gov.irs.factgraph.compnodes

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.fact.*
import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result

class ToUpperSpec extends AnyFunSpec:
  describe("ToUpper") {
    it("returns the upper case version of the input string") {
      val node = CompNode
        .fromDerivedConfig(
          new CompNodeConfigElement(
            "ToUpper",
            Seq(
              new CompNodeConfigElement(
                "String",
                Seq.empty,
                CommonOptionConfigTraits.value("nO MuD")
              )
            )
          )
        )
        .asInstanceOf[StringNode]
      assert(node.get(0) == Result.Complete(String("NO MUD")))
    }

  }
