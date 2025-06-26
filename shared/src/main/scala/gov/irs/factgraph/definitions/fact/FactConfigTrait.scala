package gov.irs.factgraph.definitions.fact

import gov.irs.factgraph.definitions.fact.{CompNodeConfigTrait, WritableConfigTrait}

import scala.scalajs.js.annotation.JSExportTopLevel

trait FactConfigTrait {
  def path: String
  def writable: Option[WritableConfigTrait]
  def derived: Option[CompNodeConfigTrait]
  def placeholder: Option[CompNodeConfigTrait]
}
