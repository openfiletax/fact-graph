package gov.irs.factgraph.limits

import gov.irs.factgraph.{FactDictionary, Factual, Path}
import gov.irs.factgraph.compnodes.{BooleanNode, CompNode, Dependency, GreaterThan, LessThanOrEqual}
import gov.irs.factgraph.definitions.fact.{CompNodeConfigTrait, LimitConfigTrait}
import gov.irs.factgraph.limits.LimitFactory

case class Max(limiter: BooleanNode, context: LimitContext) extends Limit

object Max extends LimitFactory {
  override val Key: String = "Max"
  def apply(e: LimitConfigTrait)(using Factual)(using FactDictionary): Limit =
    val rhs = CompNode.fromDerivedConfig(e.node)
    val lhs = Dependency(summon[Factual].path)
    Max(LessThanOrEqual(lhs, rhs), new LimitContext(Key, e.level, lhs, rhs))

  override def fromConfig(e: LimitConfigTrait)(using Factual)(using
      FactDictionary,
  ): Limit =
    this(e)
}
