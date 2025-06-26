package gov.irs.factgraph.operators

import gov.irs.factgraph.monads.*
import gov.irs.factgraph.{Explanation, Expression, Factual}

trait BinaryOperator[+A, -L, -R] extends Operator:
  protected def operation(lhs: L, rhs: R): A

  def apply(lhs: Result[L], rhs: Thunk[Result[R]]): Result[A] =
    if (lhs == Result.Incomplete || rhs.get == Result.Incomplete) Result.Incomplete
    else
      val value = operation(lhs.get, rhs.get.get)
      val complete = lhs.complete && rhs.get.complete

      Result(value, complete)

  final def apply(
      lhs: MaybeVector[Result[L]],
      rhs: MaybeVector[Thunk[Result[R]]],
  ): MaybeVector[Result[A]] =
    MaybeVector.vectorize2(apply, lhs, rhs)

  final def thunk(
      lhs: Thunk[Result[L]],
      rhs: Thunk[Result[R]],
  ): Thunk[Result[A]] =
    for result <- lhs yield this(result, rhs)

  final def thunk(
      lhs: MaybeVector[Thunk[Result[L]]],
      rhs: MaybeVector[Thunk[Result[R]]],
  ): MaybeVector[Thunk[Result[A]]] =
    MaybeVector.vectorize2(thunk, lhs, rhs)

  def explain(
      lhs: Expression[_],
      rhs: Expression[_],
  )(using Factual): MaybeVector[Explanation] =
    MaybeVector.vectorize2(
      (lhsExplanation: Explanation, rhsExplanation: Explanation) =>
        Explanation.opWithInclusiveChildren(lhsExplanation, rhsExplanation),
      lhs.explain,
      rhs.explain,
    )
