package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.UnaryOperator
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.Day
import java.time.ZoneId
import java.time.LocalDate

object Today extends CompNodeFactory:
  override val Key: String = "Today"

  private val operator = TodayOperator()

  def apply(offset: IntNode): DayNode =
    DayNode(
      Expression.Unary(
        offset.expr,
        operator,
      ),
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    CompNode.getConfigChildNode(e) match
      case x: IntNode => this(x)
      case _ =>
        throw new UnsupportedOperationException(
          s"invalid child type: ${e.typeName}",
        )
private final class TodayOperator extends UnaryOperator[Day, Int]:
  override protected def operation(x: Int): Day = ???

  override def apply(offsetDep: Result[Int]): Result[Day] =
    val offset = offsetDep match
      case Result.Complete(v)    => v
      case Result.Placeholder(v) => v
      case Result.Incomplete =>
        throw new IllegalArgumentException(
          s"Today's offset must be complete",
        )
    val zoneId =
      ZoneId.ofOffset("UTC", java.time.ZoneOffset.ofHours(offset))

    Result(Some(new Day(LocalDate.now(zoneId))))
