package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait}
import gov.irs.factgraph.operators.AggregateOperator
import gov.irs.factgraph.monads.*
import gov.irs.factgraph.types.*

import math.Ordered.orderingToOrdered
import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path, PathItem}

import scala.annotation.unused

object Minimum extends CompNodeFactory:
  override val Key: String = "Minimum"
  def apply(node: CompNode): CompNode =
    node match
      case node: IntNode =>
        IntNode(
          Expression.Aggregate(
            node.expr,
            summon[MinimumOperator[Int]],
          ),
        )
      case node: DollarNode =>
        DollarNode(
          Expression.Aggregate(
            node.expr,
            summon[MinimumOperator[Dollar]],
          ),
        )
      case node: RationalNode =>
        RationalNode(
          Expression.Aggregate(
            node.expr,
            summon[MinimumOperator[Rational]],
          ),
        )
      case node: DayNode =>
        DayNode(
          Expression.Aggregate(
            node.expr,
            summon[MinimumOperator[Day]],
          ),
        )
      case _ =>
        throw new UnsupportedOperationException(
          s"cannot execute minimum on a ${node.getClass.getName}",
        )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    this(CompNode.getConfigChildNode(e))

private final class MinimumOperator[A: Ordering] extends AggregateOperator[A, A]:
  override def apply(vect: MaybeVector[Thunk[Result[A]]]): Result[A] =
    vect.toList match
      case Nil =>
        Result.Incomplete
      case thunk :: Nil =>
        thunk.get
      case thunk :: thunks =>
        accumulator(thunks, thunk.get)

  @scala.annotation.tailrec
  private def accumulator(
      thunks: List[Thunk[Result[A]]],
      a: Result[A],
  ): Result[A] = thunks match
    case thunk :: thunks =>
      thunk.get match
        case Result(value, complete) =>
          val min = if Ordering[A].lt(value, a.get) then value else a.get
          accumulator(
            thunks,
            Result(min, complete && a.complete),
          )
        case _ => Result.Incomplete
    case Nil => a

//3/23/2023
// you might be thinking to yourself...
// "it sure is strange that he has this unused code here"
// Well... this gives a hint to the compiler and allows the
// accumulator operators to build.
@unused
private object MinimumOperator:
  implicit val intOperator: MinimumOperator[Int] = MinimumOperator[Int]
  implicit val dollarOperator: MinimumOperator[Dollar] = MinimumOperator[Dollar]
  implicit val rationalOperator: MinimumOperator[Rational] =
    MinimumOperator[Rational]
  implicit val dayOperator: MinimumOperator[Day] = MinimumOperator[Day]
