package gov.irs.factgraph.definitions.fact

import gov.irs.factgraph.definitions.fact.{
  CommonOptionConfigTraits,
  CompNodeConfigElement,
  OptionConfigTrait,
  OptionConfig,
}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import gov.irs.factgraph.definitions.fact.CompNodeConfigTrait

case class CompNodeConfig(
    typeName: String,
    children: Iterable[CompNodeConfigTrait],
    options: Iterable[OptionConfigTrait],
) extends CompNodeConfigTrait

class CompNodeConfigDigestWrapper(
    val typeName: String,
    val children: js.Array[CompNodeConfigDigestWrapper],
    val options: js.Dictionary[String],
) extends js.Object

object CompNodeDigestWrapper:
  def toNative(wrapper: CompNodeConfigDigestWrapper): CompNodeConfig =
    new CompNodeConfig(
      wrapper.typeName,
      wrapper.children.toList.map(CompNodeDigestWrapper.toNative(_)),
      wrapper.options.map((key, value) => OptionConfig.create(key, value)),
    )
