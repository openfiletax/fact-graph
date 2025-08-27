package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{ completeExpr, incompleteExpr, placeholderExpr }
import gov.irs.factgraph.definitions.fact.*
import gov.irs.factgraph.monads.Result
import org.scalatest.funspec.AnyFunSpec

class IsCompleteSpec extends AnyFunSpec:
  describe("IsComplete") {
    it("returns true if its input is complete") {
      val node = IsComplete(BooleanNode(completeExpr))

      assert(node.get(0) == Result.Complete(true))
    }

    it("returns false if its input is a placeholder") {
      val node = IsComplete(BooleanNode(placeholderExpr))

      assert(node.get(0) == Result.Complete(false))
    }

    it("returns false if its input is incomplete") {
      val node = IsComplete(BooleanNode(incompleteExpr))

      assert(node.get(0) == Result.Complete(false))
    }

    it("can be created from config") {
      val node = CompNode.fromDerivedConfig(
        new CompNodeConfigElement(
          "IsComplete",
          Seq(new CompNodeConfigElement("True")),
        ),
      )

      assert(node.get(0) == Result.Complete(true))
    }
  }
