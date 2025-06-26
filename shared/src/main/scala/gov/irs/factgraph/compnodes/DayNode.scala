package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path, PathItem}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait}
import gov.irs.factgraph.monads.{Result}
import gov.irs.factgraph.types.{CollectionItem, Day}

final case class DayNode(expr: Expression[Day]) extends CompNode:
  type Value = Day
  override def ValueClass = classOf[Day];

  override private[compnodes] def fromExpression(
      expr: Expression[Day],
  ): CompNode =
    DayNode(expr)

  override def extract(key: PathItem): Option[CompNode] =
    key match
      case PathItem.Child(Symbol("year")) =>
        Some(
          IntNode(Expression.Extract((x: Result[Day]) => x.map(y => y.year))),
        )
      case PathItem.Child(Symbol("month")) =>
        Some(
          IntNode(Expression.Extract((x: Result[Day]) => x.map(y => y.month))),
        )
      case PathItem.Child(Symbol("day")) =>
        Some(
          IntNode(Expression.Extract((x: Result[Day]) => x.map(y => y.day))),
        )
      case _ => None

object DayNode extends CompNodeFactory with WritableNodeFactory:
  override val Key: String = "Day"

  override def fromWritableConfig(e: WritableConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    new DayNode(
      Expression.Writable(classOf[Day]),
    )

  def apply(value: Day): DayNode = new DayNode(
    Expression.Constant(Some(value)),
  )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    this(Day(e.getOptionValue(CommonOptionConfigTraits.VALUE).get))
