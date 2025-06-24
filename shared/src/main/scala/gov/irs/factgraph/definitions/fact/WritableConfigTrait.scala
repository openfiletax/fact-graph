package gov.irs.factgraph.definitions.fact

import scala.scalajs.js.annotation.JSExportTopLevel

trait WritableConfigTrait {
  def typeName: String
  def options: Iterable[OptionConfigTrait]
  def collectionItemAlias: Option[String]
  def limits: Iterable[LimitConfigTrait]
}
