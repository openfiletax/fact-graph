package gov.irs.factgraph.compnodes

import gov.irs.factgraph.{FactDictionary, Factual}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait

trait CompNodeFactory:
  val Key: String
  def asTuple: (String, CompNodeFactory) = (Key, this)
  def fromDerivedConfig(e: CompNodeConfigTrait)(using Factual)(using
      FactDictionary,
  ): CompNode
