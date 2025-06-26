package gov.irs.factgraph.definitions

import gov.irs.factgraph.Meta
import gov.irs.factgraph.definitions.fact.FactConfigElement
import gov.irs.factgraph.definitions.meta.MetaConfigTrait

import fs2.{Fallible, Stream}
import fs2.data.xml._
import fs2.data.xml.dom._
import fs2.data.xml.scalaXml._

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js
@JSExportTopLevel("FactDictionaryConfig")
object FactDictionaryConfig:
  @JSExport
  def create(
      meta: MetaConfigTrait,
      facts: scala.scalajs.js.Array[FactConfigElement],
  ): FactDictionaryConfigElement =
    FactDictionaryConfigElement(meta, facts.toSeq)
