package gov.irs.factgraph.operators

import gov.irs.factgraph.{Explanation, Expression, Factual, Path}
import gov.irs.factgraph.monads.*
import gov.irs.factgraph.types.CollectionItem

trait AggregateOperator[+A, -X] extends Operator:
  def apply(vect: MaybeVector[Thunk[Result[X]]]): Result[A]

  final def thunk(vect: MaybeVector[Thunk[Result[X]]]): Thunk[Result[A]] =
    Thunk(() => this(vect))

  def explain(x: Expression[_])(using Factual): MaybeVector[Explanation] =
    val explanations = for explanation <- x.explain yield explanation
    MaybeVector(Explanation.opWithInclusiveChildren(explanations.toList))
