package gov.irs.factgraph.types
import java.util.UUID
import scala.beans.BeanProperty
import scala.scalajs.js.annotation.JSExport
import upickle.default.ReadWriter

final case class CollectionItem(@BeanProperty id: UUID) derives ReadWriter:
  @JSExport
  def idString = id.toString()
