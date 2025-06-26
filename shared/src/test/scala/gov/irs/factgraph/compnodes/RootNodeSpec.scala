package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec

import gov.irs.factgraph.{Expression, Path}
import gov.irs.factgraph.monads.Result

class RootNodeSpec extends AnyFunSpec:
  describe("RootNode") {
    val node = RootNode()

    it("contains nothing") {
      assert(node.get(0) == Result.Incomplete)
    }

    describe(".switch") {
      it("returns a RootNode") {
        assert(node.switch(Nil).isInstanceOf[RootNode])
      }
    }

    describe(".dependency") {
      it("returns a RootNode") {
        assert(node.dependency(Path.Relative).isInstanceOf[RootNode])
      }
    }

    describe(".fromExpression") {
      it("throws an exception") {
        assertThrows[UnsupportedOperationException] {
          node.fromExpression(Expression.Constant(None))
        }
      }
    }
  }
