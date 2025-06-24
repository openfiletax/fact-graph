package gov.irs.factgraph.types

import gov.irs.factgraph.FactDictionary
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import upickle.default.ReadWriter

final case class Enum(
    value: Option[String],
    enumOptionsPath: String,
) derives ReadWriter:
  override def equals(obj: Any): Boolean =
    obj match
      case e: Enum =>
        e.value == this.value && e.enumOptionsPath == this.enumOptionsPath
      case _ =>
        throw new IllegalArgumentException("enum must be compared to enum")

  override def toString: String = getValue()

  @JSExport("getValue")
  def getValue(): String =
    value match
      case None        => ""
      case Some(value) => value

  @JSExport("getEnumOptionsPath")
  def getEnumOptionsPath(): String = enumOptionsPath

object Enum:
  def apply(enumOptionsPath: String): Enum =
    Enum(
      None,
      enumOptionsPath,
    )

  def apply(value: String, enumOptionsPath: String): Enum =
    new Enum(Some(value), enumOptionsPath)

  def apply(value: Option[String], enumOptionsPath: String): Enum =
    value match
      case Some(x) => new Enum(Some(x), enumOptionsPath)
      case None    => new Enum(value, enumOptionsPath)
