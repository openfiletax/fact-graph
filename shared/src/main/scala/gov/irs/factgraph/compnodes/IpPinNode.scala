package gov.irs.factgraph.compnodes

import gov.irs.factgraph.Expression
import gov.irs.factgraph.Factual
import gov.irs.factgraph.Path
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait}
import gov.irs.factgraph.types.IpPin
import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.PathItem
import gov.irs.factgraph.monads.Result

final case class IpPinNode(expr: Expression[IpPin]) extends CompNode:
  type Value = IpPin
  override def ValueClass = classOf[IpPin]

  override private[compnodes] def fromExpression(
      expr: Expression[IpPin],
  ): CompNode =
    IpPinNode(expr)

object IpPinNode extends CompNodeFactory with WritableNodeFactory:
  override val Key: String = "IPPIN"

  override def fromWritableConfig(e: WritableConfigTrait)(using
      Factual,
  )(using FactDictionary): CompNode =
    new IpPinNode(
      Expression.Writable(classOf[IpPin]),
    )

  def apply(value: IpPin): IpPinNode = new IpPinNode(
    Expression.Constant(Some(value)),
  )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    this(IpPin(e.getOptionValue(CommonOptionConfigTraits.VALUE).get))
