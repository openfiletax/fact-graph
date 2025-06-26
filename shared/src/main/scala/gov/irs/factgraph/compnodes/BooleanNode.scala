package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path}
import gov.irs.factgraph.definitions.fact.{CompNodeConfigTrait, WritableConfigTrait}

final case class BooleanNode(expr: Expression[Boolean]) extends CompNode:
  type Value = Boolean

  override def ValueClass =
    classOf[java.lang.Boolean].asInstanceOf[Class[Boolean]]

  override private[compnodes] def fromExpression(
      expr: Expression[Boolean],
  ): CompNode =
    BooleanNode(expr)

object BooleanNode extends WritableNodeFactory:
  override val Key: String = "Boolean"

  override def fromWritableConfig(e: WritableConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    new BooleanNode(
      Expression.Writable(
        classOf[java.lang.Boolean].asInstanceOf[Class[Boolean]],
      ),
    )

  def apply(value: Boolean): BooleanNode = if (value) True.node else False.node

  object True extends CompNodeFactory:
    override val Key: String = "True"
    val node: BooleanNode = new BooleanNode(Expression.Constant(Some(true)))
    override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
        FactDictionary,
    ): CompNode = node

  object False extends CompNodeFactory:
    override val Key: String = "False"
    val node: BooleanNode = new BooleanNode(Expression.Constant(Some(false)))
    override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
        FactDictionary,
    ): CompNode = node
