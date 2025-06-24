package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait}
import gov.irs.factgraph.types.Dollar

final case class DollarNode(expr: Expression[Dollar]) extends CompNode:
  type Value = Dollar
  override def ValueClass = classOf[BigDecimal].asInstanceOf[Class[Dollar]]

  override private[compnodes] def fromExpression(
      expr: Expression[Dollar],
  ): CompNode =
    DollarNode(expr)

object DollarNode extends CompNodeFactory with WritableNodeFactory:
  override val Key: String = "Dollar"

  override def fromWritableConfig(e: WritableConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    new DollarNode(
      Expression.Writable(classOf[BigDecimal].asInstanceOf[Class[Dollar]]),
    )

  def apply(value: Dollar): DollarNode = new DollarNode(
    Expression.Constant(Some(value)),
  )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    this(Dollar(e.getOptionValue(CommonOptionConfigTraits.VALUE).get))
