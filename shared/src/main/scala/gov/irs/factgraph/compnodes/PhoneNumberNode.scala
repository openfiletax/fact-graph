package gov.irs.factgraph.compnodes

import gov.irs.factgraph.definitions.fact.{ CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait }
import gov.irs.factgraph.types.{ E164Number, InternationalPhoneNumber, PhoneNumber, UsPhoneNumber }
import gov.irs.factgraph.Expression
import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.Factual
import gov.irs.factgraph.Path

final case class PhoneNumberNode(expr: Expression[E164Number]) extends CompNode:
  type Value = E164Number
  override def ValueClass = classOf[E164Number];

  override private[compnodes] def fromExpression(
      expr: Expression[E164Number],
  ): CompNode =
    PhoneNumberNode(expr)

object PhoneNumberNode extends CompNodeFactory with WritableNodeFactory:
  override val Key: String = "PhoneNumber"

  override def fromWritableConfig(e: WritableConfigTrait)(using
      Factual,
  )(using FactDictionary): CompNode =
    new PhoneNumberNode(
      Expression.Writable(classOf[E164Number]),
    )

  def apply(value: E164Number): PhoneNumberNode = new PhoneNumberNode(
    Expression.Constant(Some(value)),
  )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using
      Factual,
  )(using
      FactDictionary,
  ): CompNode =
    this(PhoneNumber(e.getOptionValue(CommonOptionConfigTraits.VALUE).get))
