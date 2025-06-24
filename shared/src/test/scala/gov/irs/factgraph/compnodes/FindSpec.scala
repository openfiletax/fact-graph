package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.{Collection, CollectionItem}
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.{
  CompNodeConfigElement,
  FactConfigElement,
  WritableConfigElement
}

import java.util.UUID

class FindSpec extends AnyFunSpec:
  describe("Find") {
    val dictionary = FactDictionary()
    FactDefinition.fromConfig(
      FactConfigElement(
        "/test",
        None,
        Some(
          new CompNodeConfigElement(
            "Find",
            Seq(
              new CompNodeConfigElement("Dependency", Seq.empty, "bool")
            ),
            "/collection"
          )
        ),
        None
      )
    )(using dictionary)

    FactDefinition.fromConfig(
      FactConfigElement(
        "/collection",
        Some(new WritableConfigElement("Collection")),
        None,
        None
      )
    )(using dictionary)

    FactDefinition.fromConfig(
      FactConfigElement(
        "/collection/*/bool",
        Some(new WritableConfigElement("Boolean")),
        None,
        None
      )
    )(using dictionary)

    FactDefinition.fromConfig(
      FactConfigElement(
        "/collection/*/string",
        Some(new WritableConfigElement("String")),
        None,
        None
      )
    )(using dictionary)

    val graph = Graph(dictionary)

    val uuid1: UUID = UUID.fromString("bd54a80b-8d87-4c55-99a1-3bfa514ef613")
    val uuid2: UUID = UUID.fromString("b463129f-b688-4173-a6fc-a5c18c2cbb2d")

    for {
      result <- graph(Path("/collection"))
      fact <- result
    } fact.set(Collection(Vector(uuid1, uuid2)))

    graph.save()

    it("finds the collection item with the truthy value") {
      val fact = graph(Path("/test"))(0).get

      assert(fact.get(0) == Result.Incomplete)

      for {
        result <- graph(Path(s"/collection/#$uuid1/bool"))
        fact <- result
      } fact.set(true)

      for {
        result <- graph(Path(s"/collection/#$uuid2/bool"))
        fact <- result
      } fact.set(false)

      graph.save()

      assert(fact.get(0) == Result.Complete(CollectionItem(uuid1)))
    }

    it("finds the first in the collection with the truthy value") {
      val fact = graph(Path("/test"))(0).get
      graph.set(s"/collection/#$uuid1/bool", true)
      graph.set(s"/collection/#$uuid2/bool", true)

      graph.save()

      assert(fact.get(0) == Result.Complete(CollectionItem(uuid1)))
    }

    it("throws an error if the collection doesn't exist") {
      assertThrows[IllegalArgumentException] {
        val definition = FactDefinition.fromConfig(
          FactConfigElement(
            "/anotherTest",
            None,
            Some(
              new CompNodeConfigElement("Find", Seq.empty, "/fakeCollection")
            ),
            None
          )
        )

        definition.meta
      }
    }

    it("throws an error if asked to find on non-Boolean nodes") {
      assertThrows[UnsupportedOperationException] {
        val definition = FactDefinition.fromConfig(
          FactConfigElement(
            "/stringTest",
            None,
            Some(
              new CompNodeConfigElement(
                "Find",
                Seq(
                  new CompNodeConfigElement("Dependency", Seq.empty, "string")
                ),
                "/collection"
              )
            ),
            None
          )
        )(using dictionary)

        definition.meta
      }
    }
  }
