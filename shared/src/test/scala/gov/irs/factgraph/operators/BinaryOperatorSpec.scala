package gov.irs.factgraph.operators

import org.scalatest.funspec.AnyFunSpec

import gov.irs.factgraph.*
import gov.irs.factgraph.monads.Result

class BinaryOperatorSpec extends AnyFunSpec:
  describe("BinaryOperator") {
    val operator = new BinaryOperator[Boolean, Boolean, Boolean]:
      override def operation(lhs: Boolean, rhs: Boolean): Boolean = lhs && rhs

    describe(".get") {
      describe("when any input is incomplete") {
        it("returns incomplete") {
          assert(
            Expression
              .Binary(placeholderExpr, incompleteExpr, operator)
              .get(0) ==
              Result.Incomplete
          )

          assert(
            Expression.Binary(incompleteExpr, completeExpr, operator).get(0) ==
              Result.Incomplete
          )
        }
      }

      describe("when any input is placeholder") {
        it("returns a placeholder") {
          assert(
            Expression.Binary(completeExpr, placeholderExpr, operator).get(0) ==
              Result.Placeholder(true)
          )

          assert(
            Expression.Binary(placeholderExpr, completeExpr, operator).get(0) ==
              Result.Placeholder(true)
          )
        }
      }

      describe("when all inputs are complete") {
        it("returns complete") {
          assert(
            Expression.Binary(completeExpr, completeExpr, operator).get(0) ==
              Result.Complete(true)
          )
        }
      }

      it("only evaluates the second expression if needed") {
        var canary = false

        Expression
          .Binary(incompleteExpr, canaryExpr { canary = true }, operator)
          .get(0)

        assert(!canary)

        Expression
          .Binary(completeExpr, canaryExpr { canary = true }, operator)
          .get(0)

        assert(canary)
      }
    }

    describe(".getThunk") {
      it("waits to evaluate the expressions until the thunk is called") {
        var canary = false

        val thunk = Expression
          .Binary(canaryExpr { canary = true }, completeExpr, operator)
          .getThunk(0)

        assert(!canary)

        assert(thunk.get == Result.Complete(true))
        assert(canary)
      }
    }

    describe(".explain") {
      it("provides an explanation with its inputs as inclusive children") {
        assert(
          Expression
            .Binary(completeExpr, completeExpr, operator)
            .explain(0) ==
            Explanation.Operation(
              List(
                List(
                  Explanation.Constant,
                  Explanation.Constant
                )
              )
            )
        )
      }
    }
  }
