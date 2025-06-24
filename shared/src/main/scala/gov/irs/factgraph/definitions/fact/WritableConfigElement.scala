package gov.irs.factgraph.definitions.fact

import gov.irs.factgraph.definitions.fact.WritableConfigTrait

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("WritableConfigElement")
case class WritableConfigElement(
    typeName: String,
    options: Iterable[OptionConfigTrait],
    limits: Iterable[LimitConfigTrait],
    collectionItemAlias: Option[String],
) extends WritableConfigTrait {
  def this(typeName: String) = this(typeName, Seq.empty, Seq.empty, None)
  def this(typeName: String, collectionItemAlias: String) =
    this(typeName, Seq.empty, Seq.empty, Some(collectionItemAlias))
  def this(typeName: String, options: Iterable[OptionConfigTrait]) =
    this(typeName, options, Seq.empty, None)
  def this(
      typeName: String,
      options: Iterable[OptionConfigTrait],
      limits: Iterable[LimitConfigTrait],
  ) = this(typeName, options, limits, None)
}
