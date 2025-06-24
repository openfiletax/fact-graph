package gov.irs.factgraph.types
import scala.scalajs.js
import gov.irs.factgraph.types.CollectionItem
import java.util.UUID

object CollectionItemFactory:
  @js.annotation.JSExportTopLevel("CollectionItemFactory")
  def apply(item: String): CollectionItem =
    new CollectionItem(
      UUID.fromString(item),
    )
