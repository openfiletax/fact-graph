package gov.irs.factgraph.operators

import gov.irs.factgraph.{Explanation, Expression, Factual, Path, PathItem}
import gov.irs.factgraph.monads.*
import gov.irs.factgraph.types.CollectionItem

trait CollectOperator[+A, -X] extends Operator:
  def apply(vect: MaybeVector[(CollectionItem, Thunk[Result[X]])]): Result[A]

  def explain(path: Path, x: Expression[_])(using
      fact: Factual,
  ): MaybeVector[Explanation] =
    val vect = for {
      item <- fact(path :+ PathItem.Wildcard)
      explanation <- x.explain(using item.get)
    } yield explanation

    MaybeVector(Explanation.opWithInclusiveChildren(vect.toList))
