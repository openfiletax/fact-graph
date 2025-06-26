package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path, PathItem}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait}
import gov.irs.factgraph.monads.{MaybeVector, Result}
import gov.irs.factgraph.types.Address

final case class AddressNode(expr: Expression[Address]) extends CompNode:
  type Value = Address
  override def ValueClass = classOf[Address]

  override private[compnodes] def fromExpression(
      expr: Expression[Address],
  ): CompNode =
    AddressNode(expr)

  override def extract(key: PathItem): Option[CompNode] =
    key match
      case PathItem.Child(Symbol("streetAddress")) =>
        Some(
          StringNode(
            Expression.Extract((x: Result[Address]) => x.map(y => y.streetAddress)),
          ),
        )
      case PathItem.Child(Symbol("city")) =>
        Some(
          StringNode(
            Expression.Extract((x: Result[Address]) => x.map(y => y.city)),
          ),
        )
      case PathItem.Child(Symbol("postalCode")) =>
        Some(
          StringNode(
            Expression.Extract((x: Result[Address]) => x.map(y => y.postalCode)),
          ),
        )
      case PathItem.Child(Symbol("stateOrProvence")) =>
        Some(
          StringNode(
            Expression.Extract((x: Result[Address]) => x.map(y => y.stateOrProvence)),
          ),
        )
      case PathItem.Child(Symbol("streetAddressLine2")) =>
        Some(
          StringNode(
            Expression.Extract((x: Result[Address]) => x.map(y => y.streetAddressLine2)),
          ),
        )
      case PathItem.Child(Symbol("country")) =>
        Some(
          StringNode(
            Expression.Extract((x: Result[Address]) => x.map(y => y.country)),
          ),
        )
      case PathItem.Child(Symbol("foreignAddress")) =>
        Some(
          BooleanNode(
            Expression.Extract((x: Result[Address]) => x.map(y => y.foreignAddress())),
          ),
        )
      case _ => None

object AddressNode extends CompNodeFactory with WritableNodeFactory:
  override val Key: String = "Address"

  override def fromWritableConfig(e: WritableConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    new AddressNode(
      Expression.Writable(classOf[Address]),
    )

  def apply(value: Address): AddressNode = new AddressNode(
    Expression.Constant(Some(value)),
  )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    this(Address(e.getOptionValue(CommonOptionConfigTraits.VALUE).get))
