package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{ Expression, FactDictionary, Factual }
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.operators.UnaryOperator

object IsComplete extends CompNodeFactory:
  override val Key: String = "IsComplete"

  private val operator = IsCompleteOperator()

  def apply(x: CompNode): BooleanNode =
    BooleanNode(
      Expression.Unary(
        x.expr,
        operator,
      ),
    )

  override def fromDerivedConfig(
      e: CompNodeConfigTrait,
  )(using Factual)(using FactDictionary): CompNode =
    this(CompNode.getConfigChildNode(e))

final private class IsCompleteOperator extends UnaryOperator[Boolean, Any]:
  override protected def operation(x: Any): Boolean = ???

  override def apply(x: Result[Any]): Result[Boolean] =
    Result(x.complete, true)
