package gov.irs.factgraph.compnodes

import gov.irs.factgraph.Expression
import gov.irs.factgraph.Factual
import gov.irs.factgraph.Path
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait}
import gov.irs.factgraph.types.Pin
import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.PathItem
import gov.irs.factgraph.monads.Result

final case class PinNode(expr: Expression[Pin]) extends CompNode:
  type Value = Pin
  override def ValueClass = classOf[Pin]

  override private[compnodes] def fromExpression(
      expr: Expression[Pin],
  ): CompNode =
    PinNode(expr)

object PinNode extends CompNodeFactory with WritableNodeFactory:
  override val Key: String = "PIN"

  override def fromWritableConfig(e: WritableConfigTrait)(using
      Factual,
  )(using FactDictionary): CompNode =
    new PinNode(
      Expression.Writable(classOf[Pin]),
    )

  def apply(value: Pin): PinNode = new PinNode(
    Expression.Constant(Some(value)),
  )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    this(Pin(e.getOptionValue(CommonOptionConfigTraits.VALUE).get))
