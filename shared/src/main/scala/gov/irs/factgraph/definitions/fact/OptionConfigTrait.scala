package gov.irs.factgraph.definitions.fact

import scala.scalajs.js.annotation.JSExportTopLevel

trait OptionConfigTrait {
  def name: String
  def value: String

}

object CommonOptionConfigTraits:
  val PATH = "path"
  val VALUE = "value"
  val SEPARATOR = "sep"
  val ID = "id"
  val ENUM_OPTIONS_PATH = "optionsPath"
  val ALLOW_ALL_ZEROS = "allowAllZeros"
  val SCALE = "scale"

  def create(values: Seq[(String, String)]): Seq[OptionConfigTrait] =
    values.map(x =>
      new OptionConfigTrait:
        override def name: String = x._1
        override def value: String = x._2,
    )

  def path(path: String): Seq[OptionConfigTrait] = Seq(
    new OptionConfigTrait:
      override def name: String = PATH
      override def value: String = path,
  )

  def id(id: String): Seq[OptionConfigTrait] = Seq(
    new OptionConfigTrait:
      override def name: String = ID
      override def value: String = id,
  )

  def value(optValue: String): Seq[OptionConfigTrait] = Seq(
    new OptionConfigTrait:
      override def name: String = VALUE
      override def value: String = optValue,
  )

  def sep(sep: String): Seq[OptionConfigTrait] = Seq(
    new OptionConfigTrait:
      override def name: String = SEPARATOR
      override def value: String = sep,
  )

  def optionsPath(optionsPath: String): Seq[OptionConfigTrait] = Seq(
    new OptionConfigTrait:
      override def name: String = ENUM_OPTIONS_PATH
      override def value: String = optionsPath,
  )

  def allowAllZeros(allowAllZeros: String): Seq[OptionConfigTrait] = Seq(
    new OptionConfigTrait:
      override def name: String = ALLOW_ALL_ZEROS
      override def value: String = allowAllZeros,
  )

  def scale(scale: String): Seq[OptionConfigTrait] = Seq(
    new OptionConfigTrait:
      override def name: String = SCALE
      override def value: String = scale,
  )
