package gov.irs.factgraph.operators

import org.scalatest.funspec.AnyFunSpec

import gov.irs.factgraph.*
import gov.irs.factgraph.monads.Result

class UnaryOperatorSpec extends AnyFunSpec:
  describe("UnaryOperator") {
    val operator = new UnaryOperator[Boolean, Boolean]:
      override def operation(x: Boolean): Boolean = !x

    describe(".get") {
      describe("when the input is incomplete") {
        it("returns incomplete") {
          assert(
            Expression.Unary(incompleteExpr, operator).get(0) ==
              Result.Incomplete
          )
        }
      }

      describe("when the input is placeholder") {
        it("returns a placeholder") {
          assert(
            Expression.Unary(placeholderExpr, operator).get(0) ==
              Result.Placeholder(false)
          )
        }
      }

      describe("when the input is complete") {
        it("returns complete") {
          assert(
            Expression.Unary(completeExpr, operator).get(0) ==
              Result.Complete(false)
          )
        }
      }
    }

    describe(".getThunk") {
      it("waits to evaluate the expression until the thunk is called") {
        var canary = false

        val thunk = Expression
          .Unary(canaryExpr { canary = true }, operator)
          .getThunk(0)

        assert(!canary)

        assert(thunk.get == Result.Complete(false))
        assert(canary)
      }
    }

    describe(".explain") {
      it("provides an explanation with its input as a child") {
        assert(
          Expression.Unary(completeExpr, operator).explain(0) ==
            Explanation.Operation(List(List(Explanation.Constant)))
        )
      }
    }
  }
