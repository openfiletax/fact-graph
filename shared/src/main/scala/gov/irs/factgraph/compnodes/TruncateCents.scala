package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait}
import gov.irs.factgraph.operators.UnaryOperator
import gov.irs.factgraph.types.Rational

object TruncateCents extends CompNodeFactory:
  override val Key: String = "TruncateCents"

  private val operator = TruncateOperator()

  def apply(node: CompNode): RationalNode =
    node match
      case node: RationalNode =>
        RationalNode(
          Expression.Unary(
            node.expr,
            operator,
          ),
        )
      case _ =>
        throw new UnsupportedOperationException(
          s"cannot execute AsDecimalString on a ${node.getClass.getName}",
        )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    CompNode.getConfigChildNode(e) match
      case x: RationalNode => this(x)
      case _ =>
        throw new UnsupportedOperationException(
          s"invalid child type: ${e.typeName}",
        )

private final class TruncateOperator extends UnaryOperator[Rational, Rational]:
  override protected def operation(x: Rational): Rational =
    val value = (BigDecimal(x.numerator) / BigDecimal(x.denominator))
      .setScale(2, scala.math.BigDecimal.RoundingMode.DOWN)
    val numerator = value.bigDecimal.unscaledValue().intValue()
    val denominator = BigDecimal(10).pow(value.scale).intValue
    Rational(numerator, denominator).simplify
