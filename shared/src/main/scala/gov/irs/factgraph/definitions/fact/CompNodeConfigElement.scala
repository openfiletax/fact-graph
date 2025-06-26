package gov.irs.factgraph.definitions.fact

import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

case class CompNodeConfigElement(
    typeName: String,
    children: Seq[CompNodeConfigTrait],
    options: Seq[OptionConfigTrait],
) extends CompNodeConfigTrait {
  def this(typeName: String) = this(typeName, Seq.empty, Seq.empty)
  def this(typeName: String, children: Seq[CompNodeConfigTrait]) =
    this(typeName, children, Seq.empty)
  def this(typeName: String, children: Seq[CompNodeConfigTrait], path: String) =
    this(typeName, children, CommonOptionConfigTraits.path(path))
}
