package gov.irs.factgraph.limits

import gov.irs.factgraph.{FactDictionary, Factual, Path, Expression}
import gov.irs.factgraph.compnodes.{
  BooleanNode,
  CollectionNode,
  CollectionSize,
  CompNode,
  Dependency,
  IntNode,
  LessThanOrEqual,
}
import gov.irs.factgraph.definitions.fact.{CompNodeConfigTrait, LimitConfigTrait}
import gov.irs.factgraph.limits.LimitFactory
import gov.irs.factgraph.types.Collection

case class MaxCollectionSize(limiter: BooleanNode, context: LimitContext) extends Limit

object MaxCollectionSize extends LimitFactory {
  override val Key: String = "MaxCollectionSize"
  def apply(e: LimitConfigTrait)(using Factual)(using FactDictionary): Limit =
    val rhs = CompNode.fromDerivedConfig(e.node)
    // 3/23/2022
    // Because of the scalajs part we shouldn't leave this to a ClassCaseException
    // Those don't work in the JS.
    val fact = summon[Factual]
    if (!fact.value.isInstanceOf[CollectionNode])
      throw new UnsupportedOperationException("collection node required")
    val lhs = CollectionSize(
      CollectionNode(Expression.Dependency(fact.path), None),
    )
    Max(LessThanOrEqual(lhs, rhs), new LimitContext(Key, e.level, lhs, rhs))

  override def fromConfig(e: LimitConfigTrait)(using Factual)(using
      FactDictionary,
  ): Limit =
    this(e)
}
