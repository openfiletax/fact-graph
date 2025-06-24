package gov.irs.factgraph.definitions

import gov.irs.factgraph.definitions.fact.FactConfigTrait
import gov.irs.factgraph.definitions.meta.MetaConfigTrait

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js

case class FactDictionaryConfigElement(
    meta: MetaConfigTrait,
    facts: Seq[FactConfigTrait],
) extends FactDictionaryConfigTrait
