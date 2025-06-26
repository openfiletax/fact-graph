package gov.irs.factgraph.limits

import gov.irs.factgraph.{FactDictionary, Factual}
import gov.irs.factgraph.compnodes.{BooleanNode, CompNode, Dependency, Not, Regex, StringNode}
import gov.irs.factgraph.definitions.fact.LimitConfigTrait

case class Match(limiter: BooleanNode, context: LimitContext) extends Limit

object Match extends LimitFactory {
  override val Key: String = "Match"
  def apply(e: LimitConfigTrait)(using Factual)(using FactDictionary): Limit =
    val pattern = CompNode.fromDerivedConfig(e.node)
    val lhs = Dependency(summon[Factual].path)
    if (!pattern.isInstanceOf[StringNode] || !lhs.isInstanceOf[StringNode])
      throw new UnsupportedOperationException(
        "String node required for match limit",
      )
    Match(
      Regex(lhs.asInstanceOf[StringNode], pattern.asInstanceOf[StringNode]),
      new LimitContext(Key, e.level, lhs, pattern),
    )

  override def fromConfig(e: LimitConfigTrait)(using Factual)(using
      FactDictionary,
  ): Limit =
    this(e)
}
