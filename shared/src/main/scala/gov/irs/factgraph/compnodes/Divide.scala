package gov.irs.factgraph.compnodes

import gov.irs.factgraph.types.{*, given}
import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.{BinaryOperator, ReduceOperator}
import gov.irs.factgraph.util.Seq.itemsHaveSameRuntimeClass

import scala.annotation.unused

object Divide extends CompNodeFactory:
  override val Key: String = "Divide"

  def apply(nodes: Seq[CompNode]): CompNode =
    if (itemsHaveSameRuntimeClass(nodes))
      reduceDivide(nodes)
    else
      nodes.reduceLeft(binaryDivide)

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val dividend = CompNode.getConfigChildNode(e, "Dividend")
    val divisors = CompNode.getConfigChildNodes(e, "Divisors")

    this(dividend +: divisors)

  private def reduceDivide(nodes: Seq[CompNode]): CompNode =
    nodes.head match
      case node: IntNode =>
        nodes.reduceLeft(binaryDivide)
      case node: DollarNode =>
        DollarNode(
          Expression.Reduce(
            nodes.asInstanceOf[List[DollarNode]].map(_.expr),
            summon[DivideReduceOperator[Dollar]],
          ),
        )
      case node: RationalNode =>
        RationalNode(
          Expression.Reduce(
            nodes.asInstanceOf[List[RationalNode]].map(_.expr),
            summon[DivideReduceOperator[Rational]],
          ),
        )
      case _ =>
        throw new UnsupportedOperationException(
          s"cannot Divide a ${nodes.head.getClass.getName}",
        )

  private def binaryDivide(lhs: CompNode, rhs: CompNode): CompNode =
    (lhs, rhs).match
      case (lhs: IntNode, rhs: IntNode) =>
        RationalNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[DivideBinaryOperator[Rational, Int, Int]],
          ),
        )
      case (lhs: DollarNode, rhs: DollarNode) =>
        DollarNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[DivideBinaryOperator[Dollar, Dollar, Dollar]],
          ),
        )
      case (lhs: RationalNode, rhs: RationalNode) =>
        RationalNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[DivideBinaryOperator[Rational, Rational, Rational]],
          ),
        )
      case (lhs: IntNode, rhs: DollarNode) =>
        DollarNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[DivideBinaryOperator[Dollar, Int, Dollar]],
          ),
        )
      case (lhs: DollarNode, rhs: IntNode) =>
        DollarNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[DivideBinaryOperator[Dollar, Dollar, Int]],
          ),
        )
      case (lhs: IntNode, rhs: RationalNode) =>
        RationalNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[DivideBinaryOperator[Rational, Int, Rational]],
          ),
        )
      case (lhs: RationalNode, rhs: IntNode) =>
        RationalNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[DivideBinaryOperator[Rational, Rational, Int]],
          ),
        )
      case (lhs: RationalNode, rhs: DollarNode) =>
        DollarNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[DivideBinaryOperator[Dollar, Rational, Dollar]],
          ),
        )
      case (lhs: DollarNode, rhs: RationalNode) =>
        DollarNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[DivideBinaryOperator[Dollar, Dollar, Rational]],
          ),
        )
      case _ =>
        throw new UnsupportedOperationException(
          s"cannot Divide a ${lhs.getClass.getName} and a ${rhs.getClass.getName}",
        )

private final class DivideReduceOperator[A: Fractional] extends ReduceOperator[A]:
  override protected def reduce(x: A, y: A): A = Fractional[A].div(x, y)

@unused
private object DivideReduceOperator:
  implicit val dollarOperator: DivideReduceOperator[Dollar] =
    DivideReduceOperator[Dollar]
  implicit val rationalOperator: DivideReduceOperator[Rational] =
    DivideReduceOperator[Rational]
  // implicit def fractionalOperator[A: Fractional]: DivideReduceOperator[A] =
  //   DivideReduceOperator[A]

private trait DivideBinaryOperator[A, L, R] extends BinaryOperator[A, L, R]

@unused
private object DivideBinaryOperator:
  implicit val intIntOperator: DivideBinaryOperator[Rational, Int, Int] =
    (lhs: Int, rhs: Int) => Rational(lhs, rhs)
  implicit val dollarDollarOperator: DivideBinaryOperator[Dollar, Dollar, Dollar] =
    (lhs: Dollar, rhs: Dollar) => lhs / rhs
  implicit val rationalRationalOperator: DivideBinaryOperator[Rational, Rational, Rational] =
    (lhs: Rational, rhs: Rational) => lhs / rhs
  implicit val intDollarOperator: DivideBinaryOperator[Dollar, Int, Dollar] =
    (lhs: Int, rhs: Dollar) => Fractional[Dollar].div(lhs, rhs)
  implicit val dollarIntOperator: DivideBinaryOperator[Dollar, Dollar, Int] =
    (lhs: Dollar, rhs: Int) => Fractional[Dollar].div(lhs, rhs)
  implicit val intRationalOperator: DivideBinaryOperator[Rational, Int, Rational] =
    (lhs: Int, rhs: Rational) => Fractional[Rational].div(lhs, rhs)
  implicit val rationalIntOperator: DivideBinaryOperator[Rational, Rational, Int] =
    (lhs: Rational, rhs: Int) => Fractional[Rational].div(lhs, rhs)
  implicit val dollarRationalOperator: DivideBinaryOperator[Dollar, Dollar, Rational] =
    (lhs: Dollar, rhs: Rational) =>
      Fractional[Dollar].div(
        Numeric[Dollar].times(lhs, rhs.denominator),
        rhs.numerator,
      )
  implicit val rationalDollarOperator: DivideBinaryOperator[Dollar, Rational, Dollar] =
    (lhs: Rational, rhs: Dollar) =>
      Fractional[Dollar].div(
        lhs.numerator,
        Numeric[Dollar].times(rhs, lhs.denominator),
      )
