package gov.irs.factgraph.compnodes

import gov.irs.factgraph.types.*
import gov.irs.factgraph.{Expression, FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait
import gov.irs.factgraph.operators.BinaryOperator

object StepwiseMultiply extends CompNodeFactory:
  override val Key: String = "StepwiseMultiply"

  private val operator = StepwiseMultiplyOperator()

  def apply(multiplicand: DollarNode, rate: RationalNode): DollarNode =
    DollarNode(
      Expression.Binary(
        multiplicand.expr,
        rate.expr,
        operator,
      ),
    )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    val multiplicand = CompNode.getConfigChildNode(e, "Multiplicand")
    val rate = CompNode.getConfigChildNode(e, "Rate")

    (multiplicand, rate) match
      case (m: DollarNode, r: RationalNode) => this(m, r)
      case _ =>
        throw new UnsupportedOperationException(
          s"invalid child types: ${e.typeName}",
        )

private final class StepwiseMultiplyOperator extends BinaryOperator[Dollar, Dollar, Rational]:
  override protected def operation(x: Dollar, y: Rational): Dollar =
    Dollar(x.intValue / y.denominator * y.numerator)
