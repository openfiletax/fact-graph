package gov.irs.factgraph.types

import gov.irs.factgraph.FactDictionary
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import upickle.default.ReadWriter

final case class MultiEnum(
    values: Set[String],
    enumOptionsPath: String,
) derives ReadWriter:
  override def equals(obj: Any): Boolean =
    obj match
      case e: MultiEnum =>
        // for a multi-enum, all selected values must be equivalent
        e.values == this.values
      case _ =>
        throw new IllegalArgumentException(
          "MultiEnum must be compared to MultiEnum",
        )

  @JSExport("getValue")
  def getValue(): Set[String] =
    return values

  @JSExport("getEnumOptionsPath")
  def getEnumOptionsPath(): String = enumOptionsPath

object MultiEnum:

  def apply(values: Set[String], enumOptionsPath: String): MultiEnum =
    new MultiEnum(values, enumOptionsPath)

  def apply(enumOptionsPath: String): MultiEnum =
    MultiEnum(
      Set(),
      enumOptionsPath,
    )

  def apply(value: String, enumOptionsPath: String): MultiEnum =
    MultiEnum(
      Set(value),
      enumOptionsPath,
    )

  def apply(
      value: Option[String],
      enumOptionsPath: String,
  ): MultiEnum =
    value match
      case Some(x) => new MultiEnum(Set(x), enumOptionsPath)
      case None    => new MultiEnum(Set(), enumOptionsPath)
