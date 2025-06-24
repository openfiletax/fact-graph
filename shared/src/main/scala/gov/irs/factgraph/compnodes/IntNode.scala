package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait}

final case class IntNode(expr: Expression[Int]) extends CompNode:
  type Value = Int
  override def ValueClass = classOf[java.lang.Number].asInstanceOf[Class[Int]]

  override private[compnodes] def fromExpression(
      expr: Expression[Int],
  ): CompNode =
    IntNode(expr)

object IntNode extends CompNodeFactory with WritableNodeFactory:
  override val Key: String = "Int"

  override def fromWritableConfig(e: WritableConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    new IntNode(
      Expression.Writable(classOf[java.lang.Integer].asInstanceOf[Class[Int]]),
    )

  def apply(value: Int): IntNode = new IntNode(Expression.Constant(Some(value)))

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    this(e.getOptionValue(CommonOptionConfigTraits.VALUE).get.toInt)
