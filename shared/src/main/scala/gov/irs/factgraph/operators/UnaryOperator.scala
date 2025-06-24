package gov.irs.factgraph.operators

import gov.irs.factgraph.monads.*
import gov.irs.factgraph.{Explanation, Expression, Factual}

trait UnaryOperator[+A, -X] extends Operator:
  protected def operation(x: X): A

  def apply(x: Result[X]): Result[A] = x match
    case Result(value, complete) => Result(operation(value), complete)
    case _                       => Result.Incomplete

  final def apply(x: MaybeVector[Result[X]]): MaybeVector[Result[A]] =
    for result <- x yield this(result)

  final def thunk(x: Thunk[Result[X]]): Thunk[Result[A]] =
    for result <- x yield this(result)

  final def thunk(
      x: MaybeVector[Thunk[Result[X]]],
  ): MaybeVector[Thunk[Result[A]]] =
    for t <- x yield thunk(t)

  def explain(x: Expression[_])(using Factual): MaybeVector[Explanation] =
    for {
      explanation <- x.explain
    } yield Explanation.opWithInclusiveChildren(explanation)
