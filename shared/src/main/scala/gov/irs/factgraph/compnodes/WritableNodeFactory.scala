package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.WritableConfigTrait

trait WritableNodeFactory:
  val Key: String
  def fromWritableConfig(e: WritableConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode
