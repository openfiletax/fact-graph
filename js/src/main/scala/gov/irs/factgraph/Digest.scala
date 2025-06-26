package gov.irs.factgraph

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportTopLevel, JSExport}
import gov.irs.factgraph.definitions.fact.{
  FactConfigElement,
  WritableConfigElementDigestWrapper,
  CompNodeConfigDigestWrapper,
  CompNodeDigestWrapper,
}

@JSExportTopLevel("DigestNodeWrapper")
class DigestNodeWrapper(
    val path: String,
    val writable: WritableConfigElementDigestWrapper | Null,
    val derived: CompNodeConfigDigestWrapper | Null,
    val placeholder: CompNodeConfigDigestWrapper | Null,
) extends js.Object:
  /** A digest-node is the JSON serialization of a Fact, as produced by the direct-file Java application, from the XML
    * fact dictionary. This wrapper allows us to map digest JSON nodes into FactConfigElements, handling the necessary
    * type conversion and null safety matches
    */
  def writableOption = this.writable match
    case null => None
    case node =>
      Some(WritableConfigElementDigestWrapper.toNative(node))

  def derivedOption = this.derived match
    case null => None
    case _    => Some(CompNodeDigestWrapper.toNative(this.derived))

  def placeholderOption = this.placeholder match
    case null => None
    case _    => Some(CompNodeDigestWrapper.toNative(this.placeholder))

@JSExportTopLevel("DigestNodeWrapperFactory")
object DigestNodeWrapper:
  @JSExport
  def toNative(wrapper: DigestNodeWrapper) = FactConfigElement(
    wrapper.path,
    wrapper.writableOption,
    wrapper.derivedOption,
    wrapper.placeholderOption,
  )
