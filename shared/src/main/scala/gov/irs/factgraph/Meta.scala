package gov.irs.factgraph

import gov.irs.factgraph.definitions.meta.{ EnumDeclarationTrait, MetaConfigTrait }
import gov.irs.factgraph.definitions.meta.EnumDeclarationOptionsTrait
import gov.irs.factgraph.FactDictionary
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

case class Meta(
    val version: String,
    override val isTestDictionary: Boolean = false,
) extends MetaConfigTrait:
  def getVersion() = version
  def getIsTestDictionary() = isTestDictionary

object Meta:
  def empty(): Meta = new Meta("Invalid")
  def fromConfig(e: MetaConfigTrait, factDictionary: FactDictionary): Unit =
    factDictionary.addMeta(new Meta(e.version, e.isTestDictionary))
