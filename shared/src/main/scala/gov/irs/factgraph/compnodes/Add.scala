package gov.irs.factgraph.compnodes

import gov.irs.factgraph.types.{*, given}
import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.{BinaryOperator, ReduceOperator}
import gov.irs.factgraph.util.Seq.itemsHaveSameRuntimeClass

import scala.annotation.unused

// Sum is used across multiple child nodes. If you're trying to add
// a maybe vector from a collection, use the `Sum` node.
object Add extends CompNodeFactory:
  override val Key: String = "Add"

  def apply(nodes: Seq[CompNode]): CompNode =
    if (itemsHaveSameRuntimeClass(nodes))
      reduceAdd(nodes)
    else
      nodes.reduceLeft(binaryAdd)

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val addends = CompNode.getConfigChildNodes(e)
    this(addends)

  private def reduceAdd(nodes: Seq[CompNode]): CompNode =
    nodes.head match
      case node: IntNode =>
        IntNode(
          Expression.Reduce(
            nodes.asInstanceOf[List[IntNode]].map(_.expr),
            summon[AddReduceOperator[Int]],
          ),
        )
      case node: DollarNode =>
        DollarNode(
          Expression.Reduce(
            nodes.asInstanceOf[List[DollarNode]].map(_.expr),
            summon[AddReduceOperator[Dollar]],
          ),
        )
      case node: RationalNode =>
        RationalNode(
          Expression.Reduce(
            nodes.asInstanceOf[List[RationalNode]].map(_.expr),
            summon[AddReduceOperator[Rational]],
          ),
        )
      case _ =>
        throw new UnsupportedOperationException(
          s"cannot add a ${nodes.head.getClass.getName}",
        )

  private def binaryAdd(lhs: CompNode, rhs: CompNode): CompNode =
    (lhs, rhs).match
      case (lhs: IntNode, rhs: IntNode) =>
        IntNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[AddBinaryOperator[Int, Int, Int]],
          ),
        )
      case (lhs: DollarNode, rhs: DollarNode) =>
        DollarNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[AddBinaryOperator[Dollar, Dollar, Dollar]],
          ),
        )
      case (lhs: RationalNode, rhs: RationalNode) =>
        RationalNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[AddBinaryOperator[Rational, Rational, Rational]],
          ),
        )
      case (lhs: IntNode, rhs: DollarNode) =>
        DollarNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[AddBinaryOperator[Dollar, Int, Dollar]],
          ),
        )
      case (lhs: DollarNode, rhs: IntNode) =>
        DollarNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[AddBinaryOperator[Dollar, Dollar, Int]],
          ),
        )
      case (lhs: IntNode, rhs: RationalNode) =>
        RationalNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[AddBinaryOperator[Rational, Int, Rational]],
          ),
        )
      case (lhs: RationalNode, rhs: IntNode) =>
        RationalNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[AddBinaryOperator[Rational, Rational, Int]],
          ),
        )
      case (lhs: RationalNode, rhs: DollarNode) =>
        DollarNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[AddBinaryOperator[Dollar, Rational, Dollar]],
          ),
        )
      case (lhs: DollarNode, rhs: RationalNode) =>
        DollarNode(
          Expression.Binary(
            lhs.expr,
            rhs.expr,
            summon[AddBinaryOperator[Dollar, Dollar, Rational]],
          ),
        )
      case _ =>
        throw new UnsupportedOperationException(
          s"cannot add a ${lhs.getClass.getName} and a ${rhs.getClass.getName}",
        )

private final class AddReduceOperator[A: Numeric] extends ReduceOperator[A]:
  override protected def reduce(x: A, y: A): A = Numeric[A].plus(x, y)

@unused
private object AddReduceOperator:
  implicit val intOperator: AddReduceOperator[Int] = AddReduceOperator[Int]
  implicit val dollarOperator: AddReduceOperator[Dollar] =
    AddReduceOperator[Dollar]
  implicit val rationalOperator: AddReduceOperator[Rational] =
    AddReduceOperator[Rational]
  // implicit def numericOperator[A: Numeric]: AddReduceOperator[A] =
  //   AddReduceOperator[A]

private trait AddBinaryOperator[A, L, R] extends BinaryOperator[A, L, R]

@unused
private object AddBinaryOperator:
  implicit val intIntOperator: AddBinaryOperator[Int, Int, Int] =
    (lhs: Int, rhs: Int) => lhs + rhs
  implicit val dollarDollarOperator: AddBinaryOperator[Dollar, Dollar, Dollar] =
    (lhs: Dollar, rhs: Dollar) => lhs + rhs
  implicit val rationalRationalOperator: AddBinaryOperator[Rational, Rational, Rational] =
    (lhs: Rational, rhs: Rational) => lhs + rhs
  implicit val dollarIntOperator: AddBinaryOperator[Dollar, Dollar, Int] =
    (lhs: Dollar, rhs: Int) => Numeric[Dollar].plus(lhs, rhs)
  implicit val intDollarOperator: AddBinaryOperator[Dollar, Int, Dollar] =
    (lhs: Int, rhs: Dollar) => Numeric[Dollar].plus(lhs, rhs)
  implicit val rationalIntOperator: AddBinaryOperator[Rational, Rational, Int] =
    (lhs: Rational, rhs: Int) => Numeric[Rational].plus(lhs, rhs)
  implicit val intRationalOperator: AddBinaryOperator[Rational, Int, Rational] =
    (lhs: Int, rhs: Rational) => Numeric[Rational].plus(lhs, rhs)
  implicit val dollarRationalOperator: AddBinaryOperator[Dollar, Dollar, Rational] =
    (lhs: Dollar, rhs: Rational) => Numeric[Dollar].plus(lhs, rhs)
  implicit val rationalDollarOperator: AddBinaryOperator[Dollar, Rational, Dollar] =
    (lhs: Rational, rhs: Dollar) => Numeric[Dollar].plus(lhs, rhs)
