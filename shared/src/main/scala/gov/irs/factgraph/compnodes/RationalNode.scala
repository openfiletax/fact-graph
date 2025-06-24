package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path}
import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, CompNodeConfigTrait, WritableConfigTrait}
import gov.irs.factgraph.types.Rational

final case class RationalNode(expr: Expression[Rational]) extends CompNode:
  type Value = Rational
  override def ValueClass = classOf[Rational]

  override private[compnodes] def fromExpression(
      expr: Expression[Rational],
  ): CompNode =
    RationalNode(expr)

object RationalNode extends CompNodeFactory with WritableNodeFactory:
  override val Key: String = "Rational"

  override def fromWritableConfig(e: WritableConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    new RationalNode(
      Expression.Writable(classOf[Rational]),
    )

  def apply(value: Rational): RationalNode = new RationalNode(
    Expression.Constant(Some(value)),
  )

  override def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode =
    this(Rational(e.getOptionValue(CommonOptionConfigTraits.VALUE).get))
