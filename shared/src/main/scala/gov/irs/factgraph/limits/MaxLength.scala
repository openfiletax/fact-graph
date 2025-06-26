package gov.irs.factgraph.limits

import gov.irs.factgraph.{Expression, FactDictionary, Factual, Path}
import gov.irs.factgraph.compnodes.{BooleanNode, CompNode, Dependency, Length, LessThanOrEqual, StringNode}
import gov.irs.factgraph.definitions.fact.{CompNodeConfigTrait, LimitConfigTrait}
import gov.irs.factgraph.limits.LimitFactory

case class MaxLength(limiter: BooleanNode, context: LimitContext) extends Limit

object MaxLength extends LimitFactory {
  override val Key: String = "MaxLength"
  def apply(e: LimitConfigTrait)(using Factual)(using FactDictionary): Limit =
    val rhs = CompNode.fromDerivedConfig(e.node)
    // 3/23/2022
    // Because of the scalajs part we shouldn't leave this to a ClassCaseException
    // Those don't work in the JS.
    val fact = summon[Factual]
    if (!fact.value.isInstanceOf[StringNode])
      throw new UnsupportedOperationException("string node required")
    val lhs = Length(StringNode(Expression.Dependency(fact.path)))
    MaxLength(
      LessThanOrEqual(lhs, rhs),
      new LimitContext(Key, e.level, lhs, rhs),
    )

  override def fromConfig(e: LimitConfigTrait)(using Factual)(using
      FactDictionary,
  ): Limit =
    this(e)
}
