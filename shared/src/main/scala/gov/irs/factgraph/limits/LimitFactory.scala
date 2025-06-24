package gov.irs.factgraph.limits

import gov.irs.factgraph.compnodes.CompNode
import gov.irs.factgraph.{FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.LimitConfigTrait

trait LimitFactory {
  val Key: String
  def asTuple: (String, LimitFactory) = (Key, this)
  def fromConfig(e: LimitConfigTrait)(using Factual)(using
      FactDictionary,
  ): Limit
}
