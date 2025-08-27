package gov.irs.factgraph.definitions.fact

import gov.irs.factgraph.definitions.fact.{ CompNodeConfigTrait, WritableConfigTrait }
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.xml.NodeSeq

trait FactConfigTrait {
  def path: String
  def node: NodeSeq
  def writable: Option[WritableConfigTrait]
  def derived: Option[CompNodeConfigTrait]
  def placeholder: Option[CompNodeConfigTrait]
  def overrideCondition: Option[CompNodeConfigTrait]
  def overrideDefault: Option[CompNodeConfigTrait]
}
