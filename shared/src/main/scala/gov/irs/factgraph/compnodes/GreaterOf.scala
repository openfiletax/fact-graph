package gov.irs.factgraph.compnodes

import gov.irs.factgraph.types.*
import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.ReduceOperator
import gov.irs.factgraph.util.Seq.itemsHaveSameRuntimeClass

import scala.annotation.unused

object GreaterOf extends CompNodeFactory:
  override val Key: String = "GreaterOf"

  def apply(nodes: Seq[CompNode]): CompNode =
    if (!itemsHaveSameRuntimeClass(nodes))
      throw new UnsupportedOperationException(
        s"cannot compare nodes of different classes",
      )

    nodes.head match
      case node: IntNode =>
        IntNode(
          Expression.Reduce(
            nodes.asInstanceOf[List[IntNode]].map(_.expr),
            summon[GreaterOfOperator[Int]],
          ),
        )
      case node: DollarNode =>
        DollarNode(
          Expression.Reduce(
            nodes.asInstanceOf[List[DollarNode]].map(_.expr),
            summon[GreaterOfOperator[Dollar]],
          ),
        )
      case node: RationalNode =>
        RationalNode(
          Expression.Reduce(
            nodes.asInstanceOf[List[RationalNode]].map(_.expr),
            summon[GreaterOfOperator[Rational]],
          ),
        )
      case node: DayNode =>
        DayNode(
          Expression.Reduce(
            nodes.asInstanceOf[List[DayNode]].map(_.expr),
            summon[GreaterOfOperator[Day]],
          ),
        )
      case _ =>
        throw new UnsupportedOperationException(
          s"cannot compare a ${nodes.head.getClass.getName}",
        )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    this(CompNode.getConfigChildNodes(e))

private final class GreaterOfOperator[A: Ordering] extends ReduceOperator[A]:
  override protected def reduce(x: A, y: A): A =
    if (Ordering[A].gteq(x, y)) x else y

@unused
private object GreaterOfOperator:
  implicit val intOperator: GreaterOfOperator[Int] =
    GreaterOfOperator[Int]
  implicit val dollarOperator: GreaterOfOperator[Dollar] =
    GreaterOfOperator[Dollar]
  implicit val rationalOperator: GreaterOfOperator[Rational] =
    GreaterOfOperator[Rational]
  implicit val dayOperator: GreaterOfOperator[Day] =
    GreaterOfOperator[Day]
  // implicit def numericOperator[A: Numeric]: GreaterOfOperator[A] =
  //   GreaterOfOperator[A]
