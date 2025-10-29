package gov.irs.factgraph.types

import java.util.UUID
import scala.beans.BeanProperty
import scala.scalajs.js.annotation.JSExport
import upickle.default.ReadWriter

final case class Collection(@BeanProperty items: Vector[UUID]) derives ReadWriter:
  @JSExport
  def getItemsAsStrings() = items.map(_.toString()).toList
