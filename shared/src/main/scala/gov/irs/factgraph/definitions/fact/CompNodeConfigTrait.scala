package gov.irs.factgraph.definitions.fact

import scala.scalajs.js.annotation.JSExportTopLevel

trait CompNodeConfigTrait {
  def typeName: String
  def children: Iterable[CompNodeConfigTrait]
  def options: Iterable[OptionConfigTrait]

  def getOption(optionName: String): Option[OptionConfigTrait] =
    options.find(x => x.name == optionName)

  def getOptionValue(optionName: String): Option[String] =
    getOption(optionName).map(x => x.value)
}
