package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait}
import gov.irs.factgraph.operators.AggregateOperator
import gov.irs.factgraph.monads.*
import gov.irs.factgraph.types.*

import math.Ordered.orderingToOrdered
import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path, PathItem}

import scala.annotation.unused

object Maximum extends CompNodeFactory:
  override val Key: String = "Maximum"
  def apply(node: CompNode): CompNode =
    node match
      case node: IntNode =>
        IntNode(
          Expression.Aggregate(
            node.expr,
            summon[MaximumOperator[Int]],
          ),
        )
      case node: DollarNode =>
        DollarNode(
          Expression.Aggregate(
            node.expr,
            summon[MaximumOperator[Dollar]],
          ),
        )
      case node: RationalNode =>
        RationalNode(
          Expression.Aggregate(
            node.expr,
            summon[MaximumOperator[Rational]],
          ),
        )
      case node: DayNode =>
        DayNode(
          Expression.Aggregate(
            node.expr,
            summon[MaximumOperator[Day]],
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

private final class MaximumOperator[A: Ordering] extends AggregateOperator[A, A]:
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
          val max = if Ordering[A].gt(value, a.get) then value else a.get
          accumulator(
            thunks,
            Result(max, complete && a.complete),
          )
        case _ => Result.Incomplete
    case Nil => a

@unused
private object MaximumOperator:
  implicit val intOperator: MaximumOperator[Int] = MaximumOperator[Int]
  implicit val dollarOperator: MaximumOperator[Dollar] = MaximumOperator[Dollar]
  implicit val rationalOperator: MaximumOperator[Rational] =
    MaximumOperator[Rational]
  implicit val dayOperator: MaximumOperator[Day] = MaximumOperator[Day]
