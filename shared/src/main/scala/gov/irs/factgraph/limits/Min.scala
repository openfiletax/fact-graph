package gov.irs.factgraph.limits

import gov.irs.factgraph.{FactDictionary, Factual, Path}
import gov.irs.factgraph.compnodes.{BooleanNode, CompNode, Dependency, GreaterThanOrEqual}
import gov.irs.factgraph.definitions.fact.{CompNodeConfigTrait, LimitConfigTrait}
import gov.irs.factgraph.limits.LimitFactory

case class Min(limiter: BooleanNode, context: LimitContext) extends Limit

object Min extends LimitFactory {
  override val Key: String = "Min"
  def apply(e: LimitConfigTrait)(using Factual)(using FactDictionary): Limit =
    val rhs = CompNode.fromDerivedConfig(e.node)
    val lhs = Dependency(summon[Factual].path)
    Max(GreaterThanOrEqual(lhs, rhs), new LimitContext(Key, e.level, lhs, rhs))

  override def fromConfig(e: LimitConfigTrait)(using Factual)(using
      FactDictionary,
  ): Limit =
    this(e)
}
