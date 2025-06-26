package gov.irs.factgraph.persisters
import gov.irs.factgraph.types.WritableType
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.scalajs.js

@JSExportTopLevel("JSPersister")
object InMemoryPersisterJS:
  @JSExport
  def create(jsonString: String): InMemoryPersister =
    // We take in a json string so that scala.js can deserialize/read it
    // into the appropriate TypeContainer classes -- taking in a null-prototype
    // js object would not work.
    InMemoryPersister.apply(jsonString)

  @JSExport
  def create(): InMemoryPersister =
    InMemoryPersister.apply()
