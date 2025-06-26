package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path, PathItem}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait}
import gov.irs.factgraph.monads.{MaybeVector, Result}
import gov.irs.factgraph.types.BankAccount

final case class BankAccountNode(expr: Expression[BankAccount]) extends CompNode:
  type Value = BankAccount
  override def ValueClass = classOf[BankAccount]

  override private[compnodes] def fromExpression(
      expr: Expression[BankAccount],
  ): CompNode =
    BankAccountNode(expr)

  override def extract(key: PathItem): Option[CompNode] =
    key match
      case PathItem.Child(Symbol("accountType")) =>
        Some(
          StringNode(
            Expression.Extract((x: Result[BankAccount]) => x.map(y => y.accountType)),
          ),
        )
      case PathItem.Child(Symbol("routingNumber")) =>
        Some(
          StringNode(
            Expression.Extract((x: Result[BankAccount]) => x.map(y => y.routingNumber)),
          ),
        )
      case PathItem.Child(Symbol("accountNumber")) =>
        Some(
          StringNode(
            Expression.Extract((x: Result[BankAccount]) => x.map(y => y.accountNumber)),
          ),
        )
      case _ => None

object BankAccountNode extends CompNodeFactory with WritableNodeFactory:
  override val Key: String = "BankAccount"

  override def fromWritableConfig(e: WritableConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    new BankAccountNode(
      Expression.Writable(classOf[BankAccount]),
    )

  def apply(value: BankAccount): BankAccountNode = new BankAccountNode(
    Expression.Constant(Some(value)),
  )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    throw new NotImplementedError("BankAccoutNode.fromDerivedConfig")
