package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait}

final case class StringNode(expr: Expression[String]) extends CompNode:
  type Value = String
  override def ValueClass = classOf[String];

  override private[compnodes] def fromExpression(
      expr: Expression[String],
  ): CompNode =
    StringNode(expr)

object StringNode extends CompNodeFactory with WritableNodeFactory:
  override val Key: String = "String"

  override def fromWritableConfig(e: WritableConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    new StringNode(
      Expression.Writable(classOf[String]),
    )

  def apply(value: String): StringNode = new StringNode(
    Expression.Constant(Some(value)),
  )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    this(e.getOptionValue(CommonOptionConfigTraits.VALUE).getOrElse(""))
