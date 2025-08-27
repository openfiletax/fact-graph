package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{ Expression, FactDictionary, Factual }
import gov.irs.factgraph.definitions.fact.{ CommonOptionConfigTraits, CompNodeConfigTrait }
import gov.irs.factgraph.operators.UnaryOperator
import gov.irs.factgraph.types.{ Dollar, Rational }

object Ceiling extends CompNodeFactory:
  override val Key: String = "Ceiling"

  private val rationalOperator = RationalCeilingOperator()
  private val integerOperator = IntegerCeilingOperator()

  def apply(node: CompNode): IntNode =
    node match
      case node: RationalNode =>
        IntNode(
          Expression.Unary(
            node.expr,
            rationalOperator,
          ),
        )
      case node: IntNode =>
        IntNode(
          Expression.Unary(
            node.expr,
            integerOperator,
          ),
        )
      case _ =>
        throw new UnsupportedOperationException(
          s"cannot execute Ceiling on a ${node.getClass.getName}",
        )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using
      Factual,
  )(using
      FactDictionary,
  ): CompNode =
    CompNode.getConfigChildNode(e) match
      case x: RationalNode => this(x)
      case x: IntNode      => this(x)
      case _               =>
        throw new UnsupportedOperationException(
          s"invalid child type: ${e.typeName}",
        )

final private class RationalCeilingOperator extends UnaryOperator[Int, Rational]:
  override protected def operation(x: Rational): Int = (BigDecimal(x.numerator) / BigDecimal(x.denominator))
    .setScale(0, scala.math.BigDecimal.RoundingMode.CEILING)
    .intValue()

final private class IntegerCeilingOperator extends UnaryOperator[Int, Int]:
  override protected def operation(x: Int): Int = x
