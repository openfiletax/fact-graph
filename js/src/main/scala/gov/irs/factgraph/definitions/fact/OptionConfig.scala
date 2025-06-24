package gov.irs.factgraph.definitions.fact

import gov.irs.factgraph.definitions.fact.{CommonOptionConfigTraits, OptionConfigTrait}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js

case class OptionConfig(name: String, value: String) extends OptionConfigTrait

object OptionConfig {
  def create(name: String, value: String): OptionConfig =
    OptionConfig(name, value)

  def create(
      pairs: Iterable[(String, String)],
  ): scala.scalajs.js.Array[OptionConfig] =
    val array = new scala.scalajs.js.Array[OptionConfig](pairs.size)
    pairs.map(x => create(x._1, x._2)).foreach(x => array.push(x))
    array

  def path(path: String): scala.scalajs.js.Array[OptionConfig] =
    new scala.scalajs.js.Array[OptionConfig] {
      create(CommonOptionConfigTraits.PATH, path)
    }

}
