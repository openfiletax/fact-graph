package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait}
import gov.irs.factgraph.operators.UnaryOperator
import gov.irs.factgraph.types.Rational

object AsDecimalString extends CompNodeFactory:
  override val Key: String = "AsDecimalString"

  private val defaultScale = 2
  private val defaultScaleAsString = defaultScale.toString()
  private val defaultOperator = RationalAsDecimalString(defaultScale)

  def apply(node: CompNode, scale: Int): StringNode =
    val operator =
      if (scale == defaultScale) defaultOperator
      else RationalAsDecimalString(scale)
    node match
      case node: RationalNode =>
        StringNode(
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
      case x: RationalNode =>
        this(
          x,
          e.getOptionValue(CommonOptionConfigTraits.SCALE)
            .getOrElse(defaultScaleAsString)
            .toIntOption
            .get,
        )
      case _ =>
        throw new UnsupportedOperationException(
          s"invalid child type: ${e.typeName}",
        )

private final class RationalAsDecimalString(val scale: Int) extends UnaryOperator[String, Rational]:
  override protected def operation(x: Rational): String =
    (BigDecimal(x.numerator) / BigDecimal(x.denominator))
      .setScale(scale, BigDecimal.RoundingMode.HALF_UP)
      .toString()
