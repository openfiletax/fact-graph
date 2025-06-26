package gov.irs.factgraph.operators

import gov.irs.factgraph.monads.*
import gov.irs.factgraph.{Explanation, Expression, Factual}

trait ReduceOperator[A] extends Operator:
  protected def reduce(x: A, y: A): A

  def apply(head: Result[A], tail: List[Thunk[Result[A]]]): Result[A] =
    if (head == Result.Incomplete) return Result.Incomplete

    val lazyTail = tail.view.map(_.get)
    if (lazyTail.contains(Result.Incomplete)) return Result.Incomplete

    val (completes, values) = (head +: lazyTail)
      .unzip(r => (r.complete, r.value))

    val value = values.flatten.reduceLeft(reduce)
    val complete = completes.forall(identity)

    Result(value, complete)

  final def apply(
      head: MaybeVector[Result[A]],
      tail: List[MaybeVector[Thunk[Result[A]]]],
  ): MaybeVector[Result[A]] =
    MaybeVector.vectorizeList(apply, head, tail)

  final def thunk(
      head: Thunk[Result[A]],
      tail: List[Thunk[Result[A]]],
  ): Thunk[Result[A]] =
    for result <- head yield this(result, tail)

  final def thunk(
      head: MaybeVector[Thunk[Result[A]]],
      tail: List[MaybeVector[Thunk[Result[A]]]],
  ): MaybeVector[Thunk[Result[A]]] =
    MaybeVector.vectorizeList(thunk, head, tail)

  def explain(xs: List[Expression[_]])(using
      Factual,
  ): MaybeVector[Explanation] =
    val explanations = for expression <- xs yield expression.explain

    MaybeVector.vectorizeList(
      (head: Explanation, tail: List[Explanation]) => Explanation.opWithInclusiveChildren(head +: tail),
      explanations.head,
      explanations.tail,
    )
