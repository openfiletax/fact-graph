package gov.irs.factgraph

import gov.irs.factgraph.compnodes.RootNode
import gov.irs.factgraph.definitions.FactDictionaryConfigTrait
import gov.irs.factgraph.definitions.meta.{EnumDeclarationTrait, MetaConfigTrait}
import gov.irs.factgraph.Meta

import scala.collection.mutable
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import gov.irs.factgraph.compnodes.MultiEnumNode
import gov.irs.factgraph.compnodes.EnumNode

class FactDictionary:
  private val definitions: mutable.Map[Path, FactDefinition] = mutable.Map()
  private var frozen: Boolean = false
  private var meta: MetaConfigTrait = Meta.empty()

  def getPaths(): Iterable[Path] =
    definitions.keys

  def freeze(): Unit =
    for {
      (_, definition) <- definitions
    } definition.meta
    if (meta == Meta.empty())
      throw new UnsupportedOperationException(
        "Must provide meta information to FactDictionary",
      )
    frozen = true

  def apply(path: Path): Option[FactDefinition] = definitions.get(path)

  @JSExport("getDefinition")
  def apply(path: String): FactDefinition | Null =
    definitions.get(Path(path)) match
      case Some(value) => value
      case _           => null

  @JSExport
  def getMeta(): MetaConfigTrait = meta

  @JSExport("getOptionsPathForEnum")
  def getOptionsPathForEnum(enumPath: String): Option[String] =
    val factDef = this(enumPath)
    factDef.value match
      case value: EnumNode      => Some(value.enumOptionsPath.toString)
      case value: MultiEnumNode => Some(value.enumOptionsPath.toString)
      case _                    => None

  protected[factgraph] def addDefinition(definition: FactDefinition): Unit =
    if (frozen)
      throw new UnsupportedOperationException(
        "cannot add definitions to a frozen FactDictionary",
      )

    definitions.addOne(definition.asTuple)

  protected[factgraph] def addMeta(metaConfigTrait: MetaConfigTrait): Unit =
    if (frozen)
      throw new UnsupportedOperationException(
        "Meta configuration must be added before freezing the dictionary",
      )
    meta = metaConfigTrait

object FactDictionary:
  def apply(): FactDictionary =
    val dictionary = new FactDictionary()
    FactDefinition(RootNode(), Path.Root, Seq.empty, dictionary)
    dictionary

  @JSExport
  def fromConfig(e: FactDictionaryConfigTrait): FactDictionary =
    val dictionary = this()
    Meta.fromConfig(e.meta, dictionary)
    e.facts.map(FactDefinition.fromConfig(_)(using dictionary))
    dictionary.freeze()
    dictionary
