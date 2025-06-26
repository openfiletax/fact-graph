package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait}
import gov.irs.factgraph.operators.{ReduceOperator, UnaryOperator}

object Length extends CompNodeFactory:
  override val Key: String = "Length"
  private val operator = LengthOperator()
  def apply(node: StringNode): IntNode =
    IntNode(
      Expression.Unary(
        node.expr,
        operator,
      ),
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    CompNode.getConfigChildNode(e) match
      case x: StringNode => this(x)
      case _ =>
        throw new UnsupportedOperationException(
          s"invalid child type: ${e.typeName}",
        )

private final class LengthOperator() extends UnaryOperator[Int, String]:
  override protected def operation(x: String): Int =
    x.length
