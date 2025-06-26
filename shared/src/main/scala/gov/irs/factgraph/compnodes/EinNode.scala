package gov.irs.factgraph.compnodes

import gov.irs.factgraph.Expression
import gov.irs.factgraph.Factual
import gov.irs.factgraph.Path
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait}
import gov.irs.factgraph.types.Ein
import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.PathItem
import gov.irs.factgraph.monads.Result

final case class EinNode(expr: Expression[Ein]) extends CompNode:
  type Value = Ein
  override def ValueClass = classOf[Ein]

  override private[compnodes] def fromExpression(
      expr: Expression[Ein],
  ): CompNode =
    EinNode(expr)

object EinNode extends CompNodeFactory with WritableNodeFactory:
  override val Key: String = "EIN"

  override def fromWritableConfig(e: WritableConfigTrait)(using
      Factual,
  )(using FactDictionary): CompNode =
    new EinNode(
      Expression.Writable(classOf[Ein]),
    )

  def apply(value: Ein): EinNode = new EinNode(
    Expression.Constant(Some(value)),
  )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    this(Ein(e.getOptionValue(CommonOptionConfigTraits.VALUE).get))
