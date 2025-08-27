package gov.irs.factgraph.definitions

import fs2.{ Fallible, Stream }
import fs2.data.xml._
import fs2.data.xml.dom._
import fs2.data.xml.scalaXml._
import gov.irs.factgraph.definitions.fact.FactConfigElement
import gov.irs.factgraph.definitions.meta.MetaConfigTrait
import gov.irs.factgraph.Meta
import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
@JSExportTopLevel("FactDictionaryConfig")
object FactDictionaryConfig:
  @JSExport
  def create(
      meta: MetaConfigTrait,
      facts: scala.scalajs.js.Array[FactConfigElement],
  ): FactDictionaryConfigElement =
    FactDictionaryConfigElement(meta, facts.toSeq)
