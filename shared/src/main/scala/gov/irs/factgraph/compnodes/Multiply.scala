package gov.irs.factgraph.compnodes

import gov.irs.factgraph.types.{*, given}
import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.{BinaryOperator, ReduceOperator}
import gov.irs.factgraph.util.Seq.itemsHaveSameRuntimeClass

import scala.annotation.unused

object Multiply extends CompNodeFactory:
  override val Key: String = "Multiply"

  def apply(nodes: Seq[CompNode]): CompNode =
    if (itemsHaveSameRuntimeClass(nodes))
      reduceMultiply(nodes)
    else
      nodes.reduceLeft(binaryMultiply)

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val factors = CompNode.getConfigChildNodes(e)
    this(factors)

  private def reduceMultiply(nodes: Seq[CompNode]): CompNode =
    nodes.head match
      case node: IntNode =>
        IntNode(
          Expression.Reduce(
            nodes.asInstanceOf[List[IntNode]].map(_.expr),
            summon[MultiplyReduceOperator[Int]],
          ),
        )
      case node: DollarNode =>
        DollarNode(
          Expression.Reduce(
            nodes.asInstanceOf[List[DollarNode]].map(_.expr),
            summon[MultiplyReduceOperator[Dollar]],
          ),
        )
      case node: RationalNode =>
        RationalNode(
          Expression.Reduce(
            nodes.asInstanceOf[List[RationalNode]].map(_.expr),
            summon[MultiplyReduceOperator[Rational]],
          ),
        )
      case _ =>
        throw new UnsupportedOperationException(
          s"cannot Multiply a ${nodes.head.getClass.getName}",
        )

  private def binaryMultiply(lhs: CompNode, rhs: CompNode): CompNode =
    (lhs, rhs).match
      case (lhs: IntNode, rhs: IntNode) =>
        IntNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[MultiplyBinaryOperator[Int, Int, Int]],
          ),
        )
      case (lhs: DollarNode, rhs: DollarNode) =>
        DollarNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[MultiplyBinaryOperator[Dollar, Dollar, Dollar]],
          ),
        )
      case (lhs: RationalNode, rhs: RationalNode) =>
        RationalNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[MultiplyBinaryOperator[Rational, Rational, Rational]],
          ),
        )
      case (lhs: IntNode, rhs: DollarNode) =>
        DollarNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[MultiplyBinaryOperator[Dollar, Int, Dollar]],
          ),
        )
      case (lhs: DollarNode, rhs: IntNode) =>
        DollarNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[MultiplyBinaryOperator[Dollar, Dollar, Int]],
          ),
        )
      case (lhs: IntNode, rhs: RationalNode) =>
        RationalNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[MultiplyBinaryOperator[Rational, Int, Rational]],
          ),
        )
      case (lhs: RationalNode, rhs: IntNode) =>
        RationalNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[MultiplyBinaryOperator[Rational, Rational, Int]],
          ),
        )
      case (lhs: RationalNode, rhs: DollarNode) =>
        DollarNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[MultiplyBinaryOperator[Dollar, Rational, Dollar]],
          ),
        )
      case (lhs: DollarNode, rhs: RationalNode) =>
        DollarNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[MultiplyBinaryOperator[Dollar, Dollar, Rational]],
          ),
        )
      case _ =>
        throw new UnsupportedOperationException(
          s"cannot Multiply a ${lhs.getClass.getName} and a ${rhs.getClass.getName}",
        )

private final class MultiplyReduceOperator[A: Numeric] extends ReduceOperator[A]:
  override protected def reduce(x: A, y: A): A = Numeric[A].times(x, y)

@unused
private object MultiplyReduceOperator:
  implicit val intOperator: MultiplyReduceOperator[Int] =
    MultiplyReduceOperator[Int]
  implicit val dollarOperator: MultiplyReduceOperator[Dollar] =
    MultiplyReduceOperator[Dollar]
  implicit val rationalOperator: MultiplyReduceOperator[Rational] =
    MultiplyReduceOperator[Rational]
  // implicit def numericOperator[A: Numeric]: MultiplyReduceOperator[A] =
  //   MultiplyReduceOperator[A]

private trait MultiplyBinaryOperator[A, L, R] extends BinaryOperator[A, L, R]

@unused
private object MultiplyBinaryOperator:
  implicit val intIntOperator: MultiplyBinaryOperator[Int, Int, Int] =
    (lhs: Int, rhs: Int) => lhs * rhs
  implicit val dollarDollarOperator: MultiplyBinaryOperator[Dollar, Dollar, Dollar] =
    (lhs: Dollar, rhs: Dollar) => lhs * rhs
  implicit val rationalRationalOperator: MultiplyBinaryOperator[Rational, Rational, Rational] =
    (lhs: Rational, rhs: Rational) => lhs * rhs
  implicit val dollarIntOperator: MultiplyBinaryOperator[Dollar, Dollar, Int] =
    (lhs: Dollar, rhs: Int) => Numeric[Dollar].times(lhs, rhs)
  implicit val intDollarOperator: MultiplyBinaryOperator[Dollar, Int, Dollar] =
    (lhs: Int, rhs: Dollar) => Numeric[Dollar].times(lhs, rhs)
  implicit val rationalIntOperator: MultiplyBinaryOperator[Rational, Rational, Int] =
    (lhs: Rational, rhs: Int) => Numeric[Rational].times(lhs, rhs)
  implicit val intRationalOperator: MultiplyBinaryOperator[Rational, Int, Rational] =
    (lhs: Int, rhs: Rational) => Numeric[Rational].times(lhs, rhs)
  implicit val dollarRationalOperator: MultiplyBinaryOperator[Dollar, Dollar, Rational] =
    (lhs: Dollar, rhs: Rational) =>
      Fractional[Dollar].div(
        Numeric[Dollar].times(lhs, rhs.numerator),
        rhs.denominator,
      )
  implicit val rationalDollarOperator: MultiplyBinaryOperator[Dollar, Rational, Dollar] =
    (lhs: Rational, rhs: Dollar) =>
      Fractional[Dollar].div(
        Numeric[Dollar].times(rhs, lhs.numerator),
        lhs.denominator,
      )
