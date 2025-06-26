package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.operators.AggregateOperator
import gov.irs.factgraph.monads.*

object Count extends CompNodeFactory:
  override val Key: String = "Count"

  private val operator = CountOperator()

  def apply(bool: BooleanNode): CompNode =
    IntNode(Expression.Aggregate(bool.expr, operator))

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    CompNode.getConfigChildNode(e) match
      case x: BooleanNode => this(x)
      case _ =>
        throw new UnsupportedOperationException(
          s"invalid child type: $e",
        )

private final class CountOperator extends AggregateOperator[Int, Boolean]:
  override def apply(vect: MaybeVector[Thunk[Result[Boolean]]]): Result[Int] =
    accumulator(vect.toList, Result(0, vect.complete))

  @scala.annotation.tailrec
  private def accumulator(
      thunks: List[Thunk[Result[Boolean]]],
      a: Result[Int],
  ): Result[Int] = thunks match
    case thunk :: thunks =>
      thunk.get match
        case Result(bool, complete) =>
          val count = if bool then a.get + 1 else a.get
          accumulator(
            thunks,
            Result(count, complete && a.complete),
          )
        case _ => Result.Incomplete
    case Nil => a
