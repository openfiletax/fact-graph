package gov.irs.factgraph.types
import gov.irs.factgraph.types.CollectionItem
import java.util.UUID
import scala.scalajs.js

object CollectionItemFactory:
  @js.annotation.JSExportTopLevel("CollectionItemFactory")
  def apply(item: String): CollectionItem =
    new CollectionItem(
      UUID.fromString(item),
    )
