package gov.irs.factgraph.compnodes

import gov.irs.factgraph.types.*
import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.UnaryOperator

object Round extends CompNodeFactory:
  override val Key: String = "Round"

  private val operator = RoundOperator()

  def apply(amount: DollarNode): DollarNode =
    DollarNode(
      Expression.Unary(
        amount.expr,
        operator,
      ),
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val amount = CompNode.getConfigChildNode(e)

    amount match
      case x: DollarNode => this(x)
      case _ =>
        throw new UnsupportedOperationException(
          s"invalid child type: ${e.typeName}",
        )

private final class RoundOperator extends UnaryOperator[Dollar, Dollar]:
  override protected def operation(x: Dollar): Dollar = x.round
