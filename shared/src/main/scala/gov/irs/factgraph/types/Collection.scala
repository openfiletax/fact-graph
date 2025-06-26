package gov.irs.factgraph.types

import upickle.default.ReadWriter
import java.util.UUID
import scala.beans.BeanProperty
import scala.scalajs.js.annotation.JSExport

final case class Collection(@BeanProperty items: Vector[UUID]) derives ReadWriter:
  @JSExport
  def getItemsAsStrings() = items.toList.map(_.toString())
