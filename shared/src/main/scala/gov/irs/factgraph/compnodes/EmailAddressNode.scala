package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait}
import gov.irs.factgraph.types.EmailAddress

final case class EmailAddressNode(expr: Expression[EmailAddress]) extends CompNode:
  type Value = EmailAddress
  override def ValueClass = classOf[EmailAddress]

  override private[compnodes] def fromExpression(
      expr: Expression[EmailAddress],
  ): CompNode =
    EmailAddressNode(expr)

object EmailAddressNode extends CompNodeFactory with WritableNodeFactory:
  override val Key: String = "EmailAddress"

  override def fromWritableConfig(e: WritableConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    new EmailAddressNode(
      Expression.Writable(classOf[EmailAddress]),
    )

  def apply(value: EmailAddress): EmailAddressNode = new EmailAddressNode(
    Expression.Constant(Some(value)),
  )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    this(EmailAddress(e.getOptionValue(CommonOptionConfigTraits.VALUE).get))
