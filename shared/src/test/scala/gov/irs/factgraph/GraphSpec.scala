package gov.irs.factgraph

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.compnodes.IntNode
import gov.irs.factgraph.definitions.*
import gov.irs.factgraph.definitions.fact.{
  CommonOptionConfigTraits,
  CompNodeConfigElement,
  FactConfigElement,
  WritableConfigElement
}
import gov.irs.factgraph.monads.{Result, MaybeVector}
import gov.irs.factgraph.types.Collection

import java.util.UUID

class GraphSpec extends AnyFunSpec:
  describe("Graph") {
    describe(".apply") {
      it("finds a fact in the dictionary") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            None,
            Some(
              CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("42")
              )
            ),
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)

        assert(graph(Path("/test"))(0).get.get(0) == Result.Complete(42))
      }
    }

    describe(".save") {
      it("saves the persister") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Int")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        given Factual = fact

        assert(fact.get(0) == Result.Incomplete)

        fact.value match
          case value: IntNode => value.set(42)

        assert(fact.get(0) == Result.Incomplete)

        graph.save()

        assert(fact.get(0) == Result.Complete(42))
      }
    }

    describe(".getCollectionPaths") {
      it("handles paths with no wildcards") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Int")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        given Factual = fact

        assert(fact.get(0) == Result.Incomplete)

        fact.value match
          case value: IntNode => value.set(42)

        assert(fact.get(0) == Result.Incomplete)

        graph.save()

        assert(fact.get(0) == Result.Complete(42))

        val paths = graph.getCollectionPaths("/test")
        assert(paths.length == 1)
      }

      it("handles paths with one wildcard") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/collection",
            Some(
              new WritableConfigElement("Collection")
            ),
            None,
            None
          )
        )(using dictionary)
        FactDefinition.fromConfig(
          FactConfigElement(
            "/collection/*/test",
            None,
            Some(
              CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("42")
              )
            ),
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)

        val uuid1: UUID =
          UUID.fromString("33f4c5e8-07f3-405f-a54b-85062883fc23")
        val uuid2: UUID =
          UUID.fromString("084f4407-4cb7-43cc-8e9e-3c5c5dbe9261")

        for {
          result <- graph(Path("/collection"))
          fact <- result
        } fact.set(Collection(Vector(uuid1, uuid2)))

        graph.save()

        val paths = graph.getCollectionPaths("/collection/*")

        assert(paths.contains(s"/collection/#${uuid1}"))
        assert(paths.contains(s"/collection/#${uuid2}"))
        assert(paths.length == 2)
      }

      it("handles empty collections") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/collection",
            Some(
              new WritableConfigElement("Collection")
            ),
            None,
            None
          )
        )(using dictionary)
        FactDefinition.fromConfig(
          FactConfigElement(
            "/collection/*/test",
            None,
            Some(
              CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("42")
              )
            ),
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)
        graph.save()

        val paths = graph.getCollectionPaths("/collection/*")
        assert(paths.length == 0)
      }

      it("handles paths with multiple wildcards") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/collection",
            Some(
              new WritableConfigElement("Collection")
            ),
            None,
            None
          )
        )(using dictionary)
        FactDefinition.fromConfig(
          FactConfigElement(
            "/collection/*/anotherCollection",
            Some(
              new WritableConfigElement("Collection")
            ),
            None,
            None
          )
        )(using dictionary)
        FactDefinition.fromConfig(
          FactConfigElement(
            "/collection/*/anotherCollection/*/test",
            None,
            Some(
              CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("42")
              )
            ),
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)

        val uuid1: UUID =
          UUID.fromString("33f4c5e8-07f3-405f-a54b-85062883fc23")
        val uuid2: UUID =
          UUID.fromString("084f4407-4cb7-43cc-8e9e-3c5c5dbe9261")

        for {
          result <- graph(Path("/collection"))
          fact <- result
        } fact.set(Collection(Vector(uuid1, uuid2)))

        graph.save()

        for {
          result <- graph(Path("/collection/*/anotherCollection"))
          fact <- result
        } fact.set(Collection(Vector(uuid1, uuid2)))

        graph.save()

        val facts =
          graph.getCollectionPaths("/collection/*/anotherCollection/*/test")

        assert(
          facts.contains(
            s"/collection/#${uuid1}/anotherCollection/#${uuid1}/test"
          )
        )
        assert(
          facts.contains(
            s"/collection/#${uuid1}/anotherCollection/#${uuid2}/test"
          )
        )
        assert(
          facts.contains(
            s"/collection/#${uuid2}/anotherCollection/#${uuid1}/test"
          )
        )
        assert(
          facts.contains(
            s"/collection/#${uuid2}/anotherCollection/#${uuid2}/test"
          )
        )
        assert(facts.length == 4)
      }
    }

    describe(".set") {
      it("prevents deleting collection items") {
        val dictionary = FactDictionary()
        FactDefinition.fromConfig(
          FactConfigElement(
            "/collection",
            Some(
              new WritableConfigElement("Collection")
            ),
            None,
            None
          )
        )(using dictionary)
        FactDefinition.fromConfig(
          FactConfigElement(
            "/collection/*/test",
            Some(
              new WritableConfigElement("Int")
            ),
            None,
            None
          )
        )(using dictionary)

        val graph = Graph(dictionary)

        val uuid1: UUID =
          UUID.fromString("33f4c5e8-07f3-405f-a54b-85062883fc23")
        val uuid2: UUID =
          UUID.fromString("084f4407-4cb7-43cc-8e9e-3c5c5dbe9261")

        for {
          result <- graph(Path("/collection"))
          fact <- result
        } fact.set(Collection(Vector(uuid1, uuid2)))

        graph.save()

        var threw = false

        try {
          graph.set("/collection", Collection(Vector(uuid1)))
        } catch {
          case _ => threw = true
        }

        assert(threw)
      }
    }
  }
