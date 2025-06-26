package gov.irs.factgraph.compnodes

import gov.irs.factgraph.types.*
import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.BinaryOperator

import scala.annotation.unused

object LessThanOrEqual extends CompNodeFactory:
  override val Key: String = "LessThanOrEqual"

  def apply(lhs: CompNode, rhs: CompNode): BooleanNode =
    BooleanNode(
      (lhs, rhs).match
        case (lhs: IntNode, rhs: IntNode) =>
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[LessThanOrEqualBinaryOperator[Int]],
          )
        case (lhs: DollarNode, rhs: DollarNode) =>
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[LessThanOrEqualBinaryOperator[Dollar]],
          )
        case (lhs: RationalNode, rhs: RationalNode) =>
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[LessThanOrEqualBinaryOperator[Rational]],
          )
        case (lhs: DayNode, rhs: DayNode) =>
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[LessThanOrEqualBinaryOperator[Day]],
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

private final class LessThanOrEqualBinaryOperator[A: Ordering] extends BinaryOperator[Boolean, A, A]:
  override protected def operation(x: A, y: A): Boolean = Ordering[A].lteq(x, y)

@unused
private object LessThanOrEqualBinaryOperator:
  implicit val intOperator: LessThanOrEqualBinaryOperator[Int] =
    LessThanOrEqualBinaryOperator[Int]
  implicit val dollarOperator: LessThanOrEqualBinaryOperator[Dollar] =
    LessThanOrEqualBinaryOperator[Dollar]
  implicit val rationalOperator: LessThanOrEqualBinaryOperator[Rational] =
    LessThanOrEqualBinaryOperator[Rational]
  implicit val dayOperator: LessThanOrEqualBinaryOperator[Day] =
    LessThanOrEqualBinaryOperator[Day]

  // implicit def numericOperator[A: Numeric]: LessThanOrEqualBinaryOperator[A] =
  //   LessThanOrEqualBinaryOperator[A]
