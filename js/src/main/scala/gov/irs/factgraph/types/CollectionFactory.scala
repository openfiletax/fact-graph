package gov.irs.factgraph.types
import scala.scalajs.js
import gov.irs.factgraph.types.Collection
import java.util.UUID

object CollectionFactory:
  @js.annotation.JSExportTopLevel("CollectionFactory")
  def apply(items: js.Array[String]): Collection =
    new Collection(
      items.toVector.map(UUID.fromString(_)),
    )

  @js.annotation.JSExportTopLevel("convertCollectionToArray")
  def unapply(c: Collection): js.Array[String] =
    import js.JSConverters._
    c.items.toJSArray.map(_.toString())
