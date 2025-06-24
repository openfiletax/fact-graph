package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.{
  Expression,
  FactDefinition,
  FactDictionary,
  Graph,
  Path
}
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.Ein
import org.scalatest.funspec.AnyFunSpec

class EinNodeSpec extends AnyFunSpec {
  describe("EinNode") {
    describe("$apply") {
      it("creates nodes from values") {
        val node = EinNode(Ein("123456789"))
        assert(node.get(0) == Result.Complete(Ein("123456789")))
      }
    }
    describe("$fromDerivedConfig") {
      it("parses config") {
        val node =
          CompNode
            .fromDerivedConfig(
              new CompNodeConfigElement(
                "EIN",
                Seq.empty,
                CommonOptionConfigTraits.value("123456789")
              )
            )
            .asInstanceOf[EinNode]
        assert(node.get(0) == Result.Complete(Ein("123456789")))
      }
    }
  }
}
