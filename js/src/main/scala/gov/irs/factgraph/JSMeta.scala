package gov.irs.factgraph
import gov.irs.factgraph.definitions.meta.{ EnumDeclarationOptionsTrait, EnumDeclarationTrait }
import gov.irs.factgraph.definitions.meta.MetaConfigTrait
import gov.irs.factgraph.types.Enum
import gov.irs.factgraph.Meta
import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

final case class JSEnumDeclarationOptions(val value: String) extends EnumDeclarationOptionsTrait

final case class JSEnumDelcaration(
    val id: String,
    val options: Seq[EnumDeclarationOptionsTrait],
) extends EnumDeclarationTrait

class EnumDeclarationWrapper(val id: String, val options: js.Array[String]) extends js.Object

@JSExportTopLevel("DigestMetaWrapper")
class DigestMetaWrapper(
    val version: String,
) extends js.Object:
  def toNative(): JSMeta = JSMeta(
    version,
  )

@JSExportTopLevel("Meta")
class JSMeta(version: String) extends Meta(version):
  override def getVersion() =
    version

@JSExportTopLevel("MetaFactory")
object JSMeta:
  def empty(): JSMeta = new JSMeta("Invalid")
  @JSExport
  def fromConfig(e: MetaConfigTrait, factDictionary: FactDictionary): Unit =
    factDictionary.addMeta(new JSMeta(e.version))
