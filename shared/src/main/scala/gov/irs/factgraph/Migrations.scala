package gov.irs.factgraph
import gov.irs.factgraph.persisters.TypeContainer
import gov.irs.factgraph.types.WritableType
import ujson.Value
import upickle.default.{read, write}

// When we make changes to the Fact Graph or Fact Dictionary, those changes not only get deployed to
// our backend servers, they get deployed to the users' browsers as well.
//
// In order to safely make changes to the Fact Graph, we can define programmatic changes that modify
// existing Fact Graphs. That way in-progress Fact Graphs (which live in the user's browser) can be
// modified to fit the new requirements, before we attempt to load them in.
object Migrations {
  val MigrationsFieldName = "/meta/migrationsApplied";

  // For each new migration, make a function for it, then add it to this list
  // Migrations numbers should increase monotonically, i.e. m0_, m1_, m2_, ...
  // It's very important that these migrations stay in order, so don't re-order the list.
  // The leading number (i.e. m1) is a way of explicitly denoting that order, but it's the List
  // order itself that matters for ensuring consistency.
  private val AllMigrations = List(
    m1_BlankMigration,
    m2_DeleteInvalidAddresses,
  )
  val TotalMigrations: Int = AllMigrations.length

  def run(data: Map[String, Value], numMigrations: Int): Map[Path, WritableType] =
    AllMigrations
      .drop(numMigrations) // get the missing migrations
      .foldLeft(data)((data, migration) => migration(data)) // apply each of them
      .map((k, v) => (Path(k), read[TypeContainer](v).item)) // convert the result to Map[Path, WritableType]

  // Blank migration to test the mechanism
  private def m1_BlankMigration(data: Map[String, Value]): Map[String, Value] =
    data

  // Remove addresses that don't match MeF validation
  private def m2_DeleteInvalidAddresses(data: Map[String, Value]): Map[String, Value] =
    data.filterNot((_, value) =>
      value("$type").value == "gov.irs.factgraph.persisters.AddressWrapper" &&
        !value("item")("streetAddress").str.matches("[A-Za-z0-9]( ?[A-Za-z0-9\\-/])*"),
    )

}
