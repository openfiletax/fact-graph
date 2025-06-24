package gov.irs.factgraph.compnodes

import gov.irs.factgraph.types.*
import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.ReduceOperator
import gov.irs.factgraph.util.Seq.itemsHaveSameRuntimeClass

import scala.annotation.unused

object LesserOf extends CompNodeFactory:
  override val Key: String = "LesserOf"

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
            summon[LesserOfOperator[Int]],
          ),
        )
      case node: DollarNode =>
        DollarNode(
          Expression.Reduce(
            nodes.asInstanceOf[List[DollarNode]].map(_.expr),
            summon[LesserOfOperator[Dollar]],
          ),
        )
      case node: RationalNode =>
        RationalNode(
          Expression.Reduce(
            nodes.asInstanceOf[List[RationalNode]].map(_.expr),
            summon[LesserOfOperator[Rational]],
          ),
        )
      case node: DayNode =>
        DayNode(
          Expression.Reduce(
            nodes.asInstanceOf[List[DayNode]].map(_.expr),
            summon[LesserOfOperator[Day]],
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

private final class LesserOfOperator[A: Ordering] extends ReduceOperator[A]:
  override protected def reduce(x: A, y: A): A =
    if (Ordering[A].lteq(x, y)) x else y

@unused
private object LesserOfOperator:
  implicit val intOperator: LesserOfOperator[Int] =
    LesserOfOperator[Int]
  implicit val dollarOperator: LesserOfOperator[Dollar] =
    LesserOfOperator[Dollar]
  implicit val rationalOperator: LesserOfOperator[Rational] =
    LesserOfOperator[Rational]
  implicit val dayOperator: LesserOfOperator[Day] =
    LesserOfOperator[Day]

  // implicit def numericOperator[A: Numeric]: LesserOfOperator[A] =
  //   LesserOfOperator[A]
