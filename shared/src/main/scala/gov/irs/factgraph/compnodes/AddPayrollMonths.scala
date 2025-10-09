package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{ Expression, FactDictionary, Factual }
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.BinaryOperator
import gov.irs.factgraph.types.*

// The intent of this is to mimic payroll months. If you have monthly pay periods and the pay period ends on the last
// day of the month it will likely be the end of the next month.
// eg. April 30th the next period end date will likely be May 31st not the 30th.
object AddPayrollMonths extends CompNodeFactory:
  override val Key: String = "AddPayrollMonths"

  private val operator = AddPayrollMonthsBinaryOperator()

  def apply(day: DayNode, months: IntNode): DayNode =
    DayNode(
      Expression.Binary(
        day.expr,
        months.expr,
        operator,
      ),
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using
      Factual,
  )(using
      FactDictionary,
  ): CompNode =
    val children = CompNode.getConfigChildNodes(e)
    children match
      case (day: DayNode) :: (months: IntNode) :: Nil => this(day, months)
      case _ => throw new IllegalArgumentException("Must have a DayNode and an IntNode")

  final private class AddPayrollMonthsBinaryOperator extends BinaryOperator[Day, Day, Int]:
    override protected def operation(day: Day, months: Int): Day = day.addPayrollMonths(months)
