package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait}
import gov.irs.factgraph.types.Days

final case class DaysNode(expr: Expression[Days]) extends CompNode:
  type Value = Days
  override def ValueClass = classOf[java.lang.Integer].asInstanceOf[Class[Days]]

  override private[compnodes] def fromExpression(
      expr: Expression[Days],
  ): CompNode =
    DaysNode(expr)

object DaysNode extends CompNodeFactory with WritableNodeFactory:
  override val Key: String = "Days"

  override def fromWritableConfig(e: WritableConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    new DaysNode(
      Expression.Writable(classOf[java.lang.Integer].asInstanceOf[Class[Days]]),
    )

  def apply(value: Days): DaysNode = new DaysNode(
    Expression.Constant(Some(value)),
  )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    this(Days(e.getOptionValue(CommonOptionConfigTraits.VALUE).get))
