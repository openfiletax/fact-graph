package gov.irs.factgraph

import gov.irs.factgraph.compnodes.IntNode
import gov.irs.factgraph.definitions.fact.{FactConfigElement, WritableConfigElement}
import gov.irs.factgraph.persisters.{InMemoryPersister, IntWrapper}
import gov.irs.factgraph.types.{Address, AddressValidationFailure}
import org.scalatest.funspec.AnyFunSpec

class MigrationsSpec extends AnyFunSpec {
  describe("Migrations") {

    // This test (correctly) fails when a new migration is added
    // You need to increment the /meta/migrationsApplied value
    it("serializes a fact graph with no migrations, and adds them on serialization") {
      val jsonFactGraph =
        """{"/test":{"$type":"gov.irs.factgraph.persisters.IntWrapper","item":42}}"""
      val persister = InMemoryPersister(jsonFactGraph)

      val testFact = persister.store(Path("/test")).asInstanceOf[Int]
      val expectedSerialization = """{"/test":{"$type":"gov.irs.factgraph.persisters.IntWrapper","item":42},"/meta/migrationsApplied":{"$type":"gov.irs.factgraph.persisters.IntWrapper","item":2}}"""
      assert(testFact == 42)
      assert(persister.toJson() == expectedSerialization)
    }

    it("m1 doesn't change the fact graph at all") {
      val jsonFactGraph =
        """{"/test":{"$type":"gov.irs.factgraph.persisters.IntWrapper","item":42}}"""
      val persister = InMemoryPersister(jsonFactGraph)

      val testFact = persister.store(Path("/test")).asInstanceOf[Int]
      assert(testFact == 42)
    }

    it("m2 leaves valid addresses") {
      val jsonFactGraph =
        """{"/address":{"$type":"gov.irs.factgraph.persisters.AddressWrapper","item":{"streetAddress":"Evergreen Terrace","city":"Springfield","postalCode":"62701","stateOrProvence":"IL","country":"USA"}},"/meta/migrationsApplied":{"$type":"gov.irs.factgraph.persisters.IntWrapper","item":1}}"""
      val expectedAddress = Address("Evergreen Terrace", "Springfield", "62701", "IL", "", "USA")
      val persister = InMemoryPersister(jsonFactGraph)

      val testAddress = persister.store(Path("/address")).asInstanceOf[Address]
      assert(testAddress == expectedAddress)
    }

    it("m2 leaves non-addresses") {
      val jsonFactGraph =
        """{"/age":{"$type":"gov.irs.factgraph.persisters.IntWrapper","item": 18},"/meta/migrationsApplied":{"$type":"gov.irs.factgraph.persisters.IntWrapper","item":1}}"""
      val persister = InMemoryPersister(jsonFactGraph)

      val testAge = persister.store(Path("/age")).asInstanceOf[Int]
      assert(testAge == 18)
    }

    it("m2 deletes invalid addresses") {
      val jsonFactGraph =
        """{"/address":{"$type":"gov.irs.factgraph.persisters.AddressWrapper","item":{"streetAddress":"&&*","city":"Springfield","postalCode":"62701","stateOrProvence":"IL","country":"USA"}},"/meta/migrationsApplied":{"$type":"gov.irs.factgraph.persisters.IntWrapper","item":1}}"""
      val persister = InMemoryPersister(jsonFactGraph)

      assert(!persister.store.contains(Path("/address")))
    }

    it("m2 does not run if 2 migrations have already been applied") {
      // This is an address that would be deleted, if the migration had not already been applied
      val jsonFactGraph =
        """{"/address":{"$type":"gov.irs.factgraph.persisters.AddressWrapper","item":{"streetAddress":"&&*","city":"Springfield","postalCode":"62701","stateOrProvence":"IL","country":"USA"}},"/meta/migrationsApplied":{"$type":"gov.irs.factgraph.persisters.IntWrapper","item":2}}"""

      // It should throw because the address we're trying to load is invalid
      assertThrows[AddressValidationFailure](InMemoryPersister(jsonFactGraph))
    }

  }

}
