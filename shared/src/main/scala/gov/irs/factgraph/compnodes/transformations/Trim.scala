package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.UnaryOperator

object Trim extends CompNodeFactory:
  override val Key: String = "Trim"

  private val operator = TrimOperator()

  def apply(str: StringNode): StringNode =
    StringNode(
      Expression.Unary(
        str.expr,
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

private final class TrimOperator extends UnaryOperator[String, String]:
  override protected def operation(x: String): String =
    // Collapse spaces
    val out = " +".r.replaceAllIn(x, " ")
    out.trim
