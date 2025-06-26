package gov.irs.factgraph.compnodes

import gov.irs.factgraph.types.*
import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.BinaryOperator

import scala.annotation.unused

object GreaterThan extends CompNodeFactory:
  override val Key: String = "GreaterThan"

  def apply(lhs: CompNode, rhs: CompNode): BooleanNode =
    BooleanNode(
      (lhs, rhs).match
        case (lhs: IntNode, rhs: IntNode) =>
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[GreaterThanBinaryOperator[Int]],
          )
        case (lhs: DollarNode, rhs: DollarNode) =>
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[GreaterThanBinaryOperator[Dollar]],
          )
        case (lhs: RationalNode, rhs: RationalNode) =>
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[GreaterThanBinaryOperator[Rational]],
          )
        case (lhs: DayNode, rhs: DayNode) =>
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[GreaterThanBinaryOperator[Day]],
          )
        case _ =>
          throw new UnsupportedOperationException(
            s"cannot compare a ${lhs.getClass.getName} and a ${rhs.getClass.getName}",
          ),
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val lhs = CompNode.getConfigChildNode(e, "Left")
    val rhs = CompNode.getConfigChildNode(e, "Right")
    this(lhs, rhs)

private final class GreaterThanBinaryOperator[A: Ordering] extends BinaryOperator[Boolean, A, A]:
  override protected def operation(x: A, y: A): Boolean = Ordering[A].gt(x, y)

@unused
private object GreaterThanBinaryOperator:
  implicit val intOperator: GreaterThanBinaryOperator[Int] =
    GreaterThanBinaryOperator[Int]
  implicit val dollarOperator: GreaterThanBinaryOperator[Dollar] =
    GreaterThanBinaryOperator[Dollar]
  implicit val rationalOperator: GreaterThanBinaryOperator[Rational] =
    GreaterThanBinaryOperator[Rational]
  implicit val dayOperator: GreaterThanBinaryOperator[Day] =
    GreaterThanBinaryOperator[Day]

  // implicit def numericOperator[A: Numeric]: GreaterThanBinaryOperator[A] =
  //   GreaterThanBinaryOperator[A]
