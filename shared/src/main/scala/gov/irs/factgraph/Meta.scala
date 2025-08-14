package gov.irs.factgraph

import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.meta.{
  EnumDeclarationTrait,
  MetaConfigTrait
}
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import gov.irs.factgraph.definitions.meta.EnumDeclarationOptionsTrait

case class Meta(
    val version: String,
    override val isTestDictionary: Boolean = false
) extends MetaConfigTrait:
  def getVersion() = version
  def getIsTestDictionary() = isTestDictionary

object Meta:
  def empty(): Meta = new Meta("Invalid")
  def fromConfig(e: MetaConfigTrait, factDictionary: FactDictionary): Unit =
    factDictionary.addMeta(new Meta(e.version, e.isTestDictionary))
