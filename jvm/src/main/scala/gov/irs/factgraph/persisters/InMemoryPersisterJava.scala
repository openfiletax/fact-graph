package gov.irs.factgraph.persisters
import gov.irs.factgraph.types.WritableType
import scala.jdk.CollectionConverters.MapHasAsScala

object InMemoryPersisterJava:
  def create(store: java.util.Map[String, WritableType]): InMemoryPersister =
    new InMemoryPersister(
      store.asScala.map((x, y) => (gov.irs.factgraph.Path.apply(x), y)).toMap,
    )

  def create(): InMemoryPersister =
    InMemoryPersister.apply()
