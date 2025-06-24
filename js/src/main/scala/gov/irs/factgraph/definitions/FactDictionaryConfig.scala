package gov.irs.factgraph.definitions

import gov.irs.factgraph.definitions.fact.FactConfigElement
import gov.irs.factgraph.definitions.meta.MetaConfigTrait

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js
@JSExportTopLevel("FactDictionaryConfig")
object FactDictionaryConfig:
  @JSExport
  def create(
      meta: MetaConfigTrait,
      facts: scala.scalajs.js.Array[FactConfigElement],
  ): FactDictionaryConfigElement =
    new FactDictionaryConfigElement(meta, facts.toSeq)
