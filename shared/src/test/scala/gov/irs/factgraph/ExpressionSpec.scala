package gov.irs.factgraph

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.compnodes.IntNode
import gov.irs.factgraph.definitions.*
import gov.irs.factgraph.definitions.fact.{
  CommonOptionConfigTraits,
  CompNodeConfigElement,
  FactConfigElement,
  WritableConfigElement
}
import gov.irs.factgraph.operators.UnaryOperator
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.compnodes.given_FactDictionary

import scala.annotation.unused

class ExpressionSpec extends AnyFunSpec:
  describe("Expression") {
    describe(".isWritable") {
      describe("when the Expression is .Writable") {
        it("returns true") {
          assert(Expression.Writable(classOf[Int]).isWritable)
        }
      }

      describe("when the Expression is not .Writable") {
        it("returns false") {
          assert(!Expression.Constant(Some(42)).isWritable)
        }
      }
    }

    describe(".Writable") {
      val dictionary = FactDictionary()
      FactDefinition.fromConfig(
        FactConfigElement(
          "/test",
          Some(new WritableConfigElement("Int")),
          None,
          None
        )
      )(using dictionary)

      val graph = Graph(dictionary)
      val fact = graph(Path("/test"))(0).get
      val expr = Expression.Writable(classOf[Int])

      given Factual = fact

      describe(".set") {
        it("saves the value") {
          assert(expr.get(0) == Result.Incomplete)
          expr.set(42)
          graph.save()
          assert(expr.get(0) == Result.Complete(42))
        }
      }

      describe(".get") {
        it("gets the saved value") {
          assert(expr.get(0) == Result.Complete(42))
        }
      }

      describe(".getThunk") {
        it("gets a thunk of the saved value") {
          assert(expr.getThunk(0).get == Result.Complete(42))
        }
      }

      describe(".delete") {
        it("deletes the value") {
          assert(expr.get(0) == Result.Complete(42))
          expr.delete()
          graph.save()
          assert(expr.get(0) == Result.Incomplete)
        }
      }

      describe(".explain") {
        it("provides an explanation with the path and its completeness") {
          assert(expr.explain(0) == Explanation.Writable(false, Path("/test")))
        }
      }
    }

    describe(".Switch") {
      given Factual = FactDefinition.fromConfig(
        FactConfigElement(
          "/test",
          None,
          Some(
            CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("42")
            )
          ),
          None
        )
      )

      describe(".get") {
        describe("when there is a complete, true case") {
          it("returns that case") {
            val expr = Expression.Switch(
              List(
                (
                  Expression.Constant(Some(false)),
                  Expression.Constant(Some(1))
                ),
                (completeExpr, Expression.Constant(Some(2)))
              )
            )

            assert(expr.get(0) == Result.Complete(2))
          }
        }

        describe(
          "when there is a placeholder, false case before a complete, true case"
        ) {
          it("returns the true case, but marks the result as a placeholder") {
            val expr = Expression.Switch(
              List(
                (placeholderFalseExpr, Expression.Constant(Some(1))),
                (completeExpr, Expression.Constant(Some(2)))
              )
            )

            assert(expr.get(0) == Result.Placeholder(2))
          }
        }

        describe(
          "when there is an incomplete case before a complete, true case"
        ) {
          it("returns the true case, but marks the result as a placeholder") {
            val expr = Expression.Switch(
              List(
                (incompleteExpr, Expression.Constant(Some(1))),
                (completeExpr, Expression.Constant(Some(2)))
              )
            )

            assert(expr.get(0) == Result.Placeholder(2))
          }
        }

        describe(
          "when there is a placeholder, true case before a complete, true case"
        ) {
          it(
            "returns the placeholder case, and marks the result as a placeholder"
          ) {
            val expr = Expression.Switch(
              List(
                (
                  Expression.Constant(Some(false)),
                  Expression.Constant(Some(1))
                ),
                (placeholderExpr, Expression.Constant(Some(2))),
                (completeExpr, Expression.Constant(Some(3)))
              )
            )

            assert(expr.get(0) == Result.Placeholder(2))
          }
        }

        describe("when none of the cases match") {
          it("returns incomplete") {
            val expr = Expression.Switch(
              List(
                (
                  Expression.Constant(Some(false)),
                  Expression.Constant(Some(1))
                ),
                (Expression.Constant(Some(false)), Expression.Constant(Some(2)))
              )
            )

            assert(expr.get(0) == Result.Incomplete)
          }
        }
      }

      describe(".getThunk") {
        it("the expressions aren't evaluated until needed") {
          var canary = false

          val thunk = Expression
            .Switch(
              List(
                (
                  Expression.Constant(Some(false)),
                  Expression.Constant(Some(1))
                ),
                (canaryExpr { canary = true }, Expression.Constant(Some(2)))
              )
            )
            .getThunk(0)

          assert(!canary)

          assert(thunk.get == Result.Complete(2))
          assert(canary)
        }
      }

      describe(".explain") {
        describe("when there is a complete, true case") {
          it(
            "explains all the predicates prior to that case, and the selected result"
          ) {
            val expr = Expression.Switch(
              List(
                (
                  Expression.Constant(Some(false)),
                  Expression.Constant(Some(1))
                ),
                (completeExpr, Expression.Constant(Some(2))),
                (
                  Expression.Constant(Some(false)),
                  Expression.Constant(Some(3))
                )
              )
            )

            assert(
              expr.explain(0) == Explanation.Operation(
                List(
                  List(Explanation.Constant /*false*/ ),
                  List(
                    Explanation.Constant /*true*/,
                    Explanation.Constant /*2*/
                  )
                )
              )
            )
          }
        }

        describe(
          "when there is a placeholder, false case before a complete, true case"
        ) {
          it("explains only the incomplete predicate, not the placeholders") {
            val expr = Expression.Switch(
              List(
                (placeholderFalseExpr, Expression.Constant(Some(1))),
                (completeExpr, Expression.Constant(Some(2)))
              )
            )

            assert(
              expr.explain(0) == Explanation.Operation(
                List(
                  List(Explanation.Operation(List(List(Explanation.Constant))))
                )
              )
            )
          }
        }

        describe("when none of the cases match") {
          it("explains all predicates") {
            val expr = Expression.Switch(
              List(
                (
                  Expression.Constant(Some(false)),
                  Expression.Constant(Some(1))
                ),
                (Expression.Constant(Some(false)), Expression.Constant(Some(2)))
              )
            )

            assert(
              expr.explain(0) == Explanation.Operation(
                List(
                  List(Explanation.Constant /*false*/ ),
                  List(Explanation.Constant /*false*/ )
                )
              )
            )
          }
        }
      }
    }

    describe(".Dependency") {
      val dictionary = FactDictionary()
      FactDefinition.fromConfig(
        FactConfigElement(
          "/value",
          None,
          Some(
            CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("42")
            )
          ),
          None
        )
      )(using dictionary)

      FactDefinition.fromConfig(
        FactConfigElement(
          "/placeholder",
          None,
          Some(
            CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("0")
            )
          ),
          None
        )
      )(using dictionary)

      val graph = Graph(dictionary)
      @unused
      given Factual = graph(Path("/placeholder"))(0).get

      val expr = Expression.Dependency(Path("../value"))

      describe(".get") {
        it("gets the result via the dependency") {
          assert(expr.get(0) == Result.Complete(42))
        }

        it("returns incomplete if there's nothing at the path") {
          val result = Expression.Dependency(Path("../missing")).get(0)
          assert(result == Result.Incomplete)
        }
      }

      describe(".getThunk") {
        it("returns a thunk to get the result via the dependency") {
          assert(expr.getThunk(0).get == Result.Complete(42))
        }

        it("returns an incomplete thunk if there's nothing at the path") {
          val thunk = Expression.Dependency(Path("../missing")).getThunk(0)
          assert(thunk.get == Result.Incomplete)
        }
      }

      describe(".explain") {
        it("explains a resolved dependency") {
          assert(
            expr.explain(0) == Explanation.Dependency(
              true,
              Path("/placeholder"),
              Path("../value"),
              List(List(Explanation.Constant))
            )
          )
        }

        it("explains an unresolved dependency") {
          val explanation = Expression.Dependency(Path("../missing")).explain(0)
          assert(
            explanation == Explanation.Dependency(
              false,
              Path("/placeholder"),
              Path("../missing"),
              List()
            )
          )
        }
      }

      describe(
        "when the dependency navigates via a placeholder fact reference"
      ) {
        describe(".get") {
          it("marks the result as a placeholder") {} // TODO
        }

        describe(".getThunk") {
          it("returns a thunk of a placeholder result") {} // TODO
        }
      }
    }

    describe(".Extract") {
      val dictionary = FactDictionary()

      FactDefinition.fromConfig(
        FactConfigElement(
          "/parent",
          None,
          Some(
            CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("42")
            )
          ),
          None
        )
      )(using dictionary)
      FactDefinition.fromConfig(
        FactConfigElement(
          "/parent/child",
          None,
          Some(
            CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("0")
            )
          ),
          None
        )
      )(using dictionary)

      val graph = Graph(dictionary)
      @unused
      given Factual = graph(Path("/parent/child"))(0).get

      val expr = Expression.Extract((result: Result[Int]) => result.map(_ + 1))

      describe(".get") {
        it("gets a value by applying the function to the parent fact") {
          assert(expr.get(0) == Result.Complete(43))
        }
      }

      describe(".getThunk") {
        it("gets a thunk of applying the function to the parent fact") {
          assert(expr.getThunk(0).get == Result.Complete(43))
        }
      }

      describe(".explain") {
        it("passes through the explanation from the parent fact") {
          assert(expr.explain(0) == Explanation.Constant)
        }
      }
    }
  }
