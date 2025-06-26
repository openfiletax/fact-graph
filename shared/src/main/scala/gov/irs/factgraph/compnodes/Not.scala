package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.UnaryOperator

object Not extends CompNodeFactory:
  override val Key: String = "Not"

  private val operator = NotOperator()

  def apply(bool: BooleanNode): BooleanNode =
    BooleanNode(
      Expression.Unary(
        bool.expr,
        operator,
      ),
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    CompNode.getConfigChildNode(e) match
      case x: BooleanNode => this(x)
      case _ =>
        throw new UnsupportedOperationException(
          s"invalid child type: ${e.typeName}",
        )
private final class NotOperator extends UnaryOperator[Boolean, Boolean]:
  override protected def operation(x: Boolean): Boolean = !x
