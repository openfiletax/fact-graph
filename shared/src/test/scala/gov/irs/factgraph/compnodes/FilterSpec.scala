package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.Collection
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.{
  CompNodeConfigElement,
  FactConfigElement,
  WritableConfigElement
}

import java.util.UUID

class FilterSpec extends AnyFunSpec:
  describe("Filter") {
    val dictionary = FactDictionary()
    FactDefinition.fromConfig(
      FactConfigElement(
        "/test",
        None,
        Some(
          new CompNodeConfigElement(
            "Filter",
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
        Some(
          new WritableConfigElement("Collection")
        ),
        None,
        None
      )
    )(using dictionary)

    FactDefinition.fromConfig(
      FactConfigElement(
        "/collection/*/bool",
        Some(
          new WritableConfigElement("Boolean")
        ),
        None,
        None
      )
    )(using dictionary)

    FactDefinition.fromConfig(
      FactConfigElement(
        "/collection/*/string",
        Some(
          new WritableConfigElement("String")
        ),
        None,
        None
      )
    )(using dictionary)

    val graph = Graph(dictionary)

    val uuid1: UUID = UUID.fromString("8997039b-31e7-4df9-97fd-9c30f6293f40")
    val uuid2: UUID = UUID.fromString("3009533e-b68d-4b16-8401-295e919daeb0")

    for {
      result <- graph(Path("/collection"))
      fact <- result
    } fact.set(Collection(Vector(uuid1, uuid2)))

    graph.save()

    it("filters collections on the value of the Boolean node") {
      val fact = graph(Path("/test"))(0).get

      assert(fact.get(0) == Result.Placeholder(Collection(Vector())))

      for {
        result <- graph(Path(s"/collection/#$uuid1/bool"))
        fact <- result
      } fact.set(true)

      for {
        result <- graph(Path(s"/collection/#$uuid2/bool"))
        fact <- result
      } fact.set(false)

      graph.save()

      assert(fact.get(0) == Result.Complete(Collection(Vector(uuid1))))
    }

    it("throws an error if the collection doesn't exist") {
      assertThrows[IllegalArgumentException] {
        val definition = FactDefinition.fromConfig(
          FactConfigElement(
            "/anotherTest",
            None,
            Some(
              new CompNodeConfigElement(
                "Filter",
                Seq(
                  new CompNodeConfigElement("True")
                ),
                "/fakeCollection"
              )
            ),
            None
          )
        )
        definition.meta
      }
    }

    it("throws an error if asked to filter on non-Boolean nodes") {
      assertThrows[UnsupportedOperationException] {
        val definition = FactDefinition.fromConfig(
          FactConfigElement(
            "/stringTest",
            None,
            Some(
              new CompNodeConfigElement(
                "Filter",
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
