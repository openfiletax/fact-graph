package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait}
import gov.irs.factgraph.operators.UnaryOperator
import gov.irs.factgraph.types.Rational

object Floor extends CompNodeFactory:
  override val Key: String = "Floor"

  private val rationalOperator = RationalFloorOperator()
  private val integerOperator = IntegerFloorOperator()

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
          s"cannot execute Floor on a ${node.getClass.getName}"
        )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    CompNode.getConfigChildNode(e) match
      case x: RationalNode => this(x)
      case x: IntNode => this(x)
      case _ =>
        throw new UnsupportedOperationException(
          s"invalid child type: ${e.typeName}",
        )

private final class RationalFloorOperator extends UnaryOperator[Int, Rational]:
  override protected def operation(x: Rational): Int = (x.numerator.toFloat / x.denominator).floor.toInt

private final class IntegerFloorOperator extends UnaryOperator[Int, Int]:
  override protected def operation(x: Int): Int = x