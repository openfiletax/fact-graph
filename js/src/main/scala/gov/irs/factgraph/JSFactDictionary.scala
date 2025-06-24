package gov.irs.factgraph
import gov.irs.factgraph.FactDictionary
import gov.irs.factgraph.definitions.FactDictionaryConfigTrait
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import gov.irs.factgraph.compnodes.RootNode

@JSExportTopLevel("FactDictionaryFactory")
object JSFactDictionary:
  def apply(): FactDictionary =
    val dictionary = new FactDictionary()
    FactDefinition(RootNode(), Path.Root, Seq.empty, dictionary)
    dictionary

  @JSExport
  def fromConfig(e: FactDictionaryConfigTrait): FactDictionary =
    // A lot like the Scala fact dictionary factory, but uses the JSMeta, which
    // contains useful methods the frontend needs
    val dictionary = this()
    JSMeta.fromConfig(e.meta, dictionary)
    e.facts.map(FactDefinition.fromConfig(_)(using dictionary))
    dictionary.freeze()
    dictionary
