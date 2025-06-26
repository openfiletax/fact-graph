package gov.irs.factgraph.types
import upickle.default.ReadWriter

import java.util.UUID
import scala.beans.BeanProperty
import scala.scalajs.js.annotation.JSExport

final case class CollectionItem(@BeanProperty id: UUID) derives ReadWriter:
  @JSExport
  def idString = id.toString()
