package gov.irs.factgraph.compnodes

import gov.irs.factgraph.types.*
import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.BinaryOperator

import scala.annotation.unused

object LessThan extends CompNodeFactory:
  override val Key: String = "LessThan"

  def apply(lhs: CompNode, rhs: CompNode): BooleanNode =
    BooleanNode(
      (lhs, rhs).match
        case (lhs: IntNode, rhs: IntNode) =>
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[LessThanBinaryOperator[Int]],
          )
        case (lhs: DollarNode, rhs: DollarNode) =>
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[LessThanBinaryOperator[Dollar]],
          )
        case (lhs: RationalNode, rhs: RationalNode) =>
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[LessThanBinaryOperator[Rational]],
          )
        case (lhs: DayNode, rhs: DayNode) =>
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[LessThanBinaryOperator[Day]],
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

private final class LessThanBinaryOperator[A: Ordering] extends BinaryOperator[Boolean, A, A]:
  override protected def operation(x: A, y: A): Boolean = Ordering[A].lt(x, y)

@unused
private object LessThanBinaryOperator:
  implicit val intOperator: LessThanBinaryOperator[Int] =
    LessThanBinaryOperator[Int]
  implicit val dollarOperator: LessThanBinaryOperator[Dollar] =
    LessThanBinaryOperator[Dollar]
  implicit val rationalOperator: LessThanBinaryOperator[Rational] =
    LessThanBinaryOperator[Rational]
  implicit val dayOperator: LessThanBinaryOperator[Day] =
    LessThanBinaryOperator[Day]

  // implicit def numericOperator[A: Numeric]: LessThanBinaryOperator[A] =
  //   LessThanBinaryOperator[A]
