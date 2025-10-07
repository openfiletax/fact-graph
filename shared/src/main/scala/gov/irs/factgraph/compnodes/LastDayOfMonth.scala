package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{ Expression, FactDictionary, Factual }
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.UnaryOperator
import gov.irs.factgraph.types.*
import java.time.temporal.TemporalAdjusters

object LastDayOfMonth extends CompNodeFactory:
  override val Key: String = "LastDayOfMonth"

  private val operator = LastDayOfMonthOperator()

  def apply(day: DayNode): DayNode =
    DayNode(
      Expression.Unary(
        day.expr,
        operator,
      ),
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using
      Factual,
  )(using
      FactDictionary,
  ): CompNode =
    CompNode.getConfigChildNode(e) match
      case x: DayNode => this(x)
      case _          => throw new UnsupportedOperationException(s"invalid child type: ${e.typeName}")

final private class LastDayOfMonthOperator extends UnaryOperator[Day, Day]:
  override protected def operation(day: Day): Day = Day(day.date.`with`(TemporalAdjusters.lastDayOfMonth()))
