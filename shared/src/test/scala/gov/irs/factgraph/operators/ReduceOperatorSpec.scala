package gov.irs.factgraph.operators

import org.scalatest.funspec.AnyFunSpec

import gov.irs.factgraph.*
import gov.irs.factgraph.monads.Result

class ReduceOperatorSpec extends AnyFunSpec:
  describe("ReduceOperator") {
    val operator = new ReduceOperator[Boolean]:
      override def reduce(x: Boolean, y: Boolean): Boolean = x && y

    describe(".get") {
      describe("when any input is incomplete") {
        it("returns incomplete") {
          assert(
            Expression
              .Reduce(
                List(completeExpr, placeholderExpr, incompleteExpr),
                operator
              )
              .get(0) ==
              Result.Incomplete
          )
        }
      }

      describe("when any input is placeholder") {
        it("returns a placeholder") {
          assert(
            Expression
              .Reduce(
                List(completeExpr, placeholderExpr, completeExpr),
                operator
              )
              .get(0) ==
              Result.Placeholder(true)
          )
        }
      }

      describe("when all inputs are complete") {
        it("returns complete") {
          assert(
            Expression
              .Reduce(List(completeExpr, completeExpr, completeExpr), operator)
              .get(0) ==
              Result.Complete(true)
          )
        }
      }

      it("doesn't evaluate past an incomplete expression") {
        var canary = false

        Expression
          .Reduce(
            List(incompleteExpr, canaryExpr { canary = true }),
            operator
          )
          .get(0)

        assert(!canary)

        Expression
          .Reduce(
            List(completeExpr, incompleteExpr, canaryExpr { canary = true }),
            operator
          )
          .get(0)

        assert(!canary)

        Expression
          .Reduce(
            List(completeExpr, completeExpr, canaryExpr { canary = true }),
            operator
          )
          .get(0)

        assert(canary)
      }
    }

    describe(".getThunk") {
      it("waits to evaluate the expressions until the thunk is called") {
        var canary = false

        val thunk = Expression
          .Reduce(List(canaryExpr { canary = true }, completeExpr), operator)
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
            .Reduce(List(completeExpr, completeExpr, completeExpr), operator)
            .explain(0) ==
            Explanation.Operation(
              List(
                List(
                  Explanation.Constant,
                  Explanation.Constant,
                  Explanation.Constant
                )
              )
            )
        )
      }
    }
  }
