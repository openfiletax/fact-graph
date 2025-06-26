package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.operators.AggregateOperator
import gov.irs.factgraph.monads.*
import gov.irs.factgraph.types.*

import scala.annotation.unused

// Sum is used across a collection. If you're trying to add
// a number of dependencies, use the `Add` node.
object CollectionSum extends CompNodeFactory:
  override val Key: String = "CollectionSum"

  def apply(node: CompNode): CompNode =
    node match
      case node: IntNode =>
        IntNode(
          Expression.Aggregate(
            node.expr,
            summon[SumOperator[Int]],
          ),
        )
      case node: DollarNode =>
        DollarNode(
          Expression.Aggregate(
            node.expr,
            summon[SumOperator[Dollar]],
          ),
        )
      case node: RationalNode =>
        RationalNode(
          Expression.Aggregate(
            node.expr,
            summon[SumOperator[Rational]],
          ),
        )
      case _ =>
        throw new UnsupportedOperationException(
          s"cannot sum a ${node.getClass.getName}",
        )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    this(CompNode.getConfigChildNode(e))
private final class SumOperator[A: Numeric] extends AggregateOperator[A, A]:
  override def apply(vect: MaybeVector[Thunk[Result[A]]]): Result[A] =
    accumulator(vect.toList, Result(Numeric[A].zero, vect.complete))

  @scala.annotation.tailrec
  private def accumulator(
      thunks: List[Thunk[Result[A]]],
      a: Result[A],
  ): Result[A] = thunks match
    case thunk :: thunks =>
      thunk.get match
        case Result(value, complete) =>
          val sum = Numeric[A].plus(value, a.get)
          accumulator(
            thunks,
            Result(sum, complete && a.complete),
          )
        case _ => Result.Incomplete
    case Nil => a

@unused
private object SumOperator:
  implicit val intOperator: SumOperator[Int] = SumOperator[Int]
  implicit val dollarOperator: SumOperator[Dollar] = SumOperator[Dollar]
  implicit val rationalOperator: SumOperator[Rational] = SumOperator[Rational]
  // implicit def numericOperator[A: Numeric]: SumOperator[A] = SumOperator[A]
