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

class CountSpec extends AnyFunSpec:
  describe("Count") {
    val dictionary = FactDictionary()

    FactDefinition.fromConfig(
      FactConfigElement(
        "/test",
        None,
        Some(
          new CompNodeConfigElement(
            "Count",
            Seq(
              new CompNodeConfigElement(
                "Dependency",
                Seq.empty,
                "/collection/*/bool"
              )
            )
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

    val uuid1: UUID = UUID.fromString("53430c5c-29ab-4c9b-9e83-075db591caea")
    val uuid2: UUID = UUID.fromString("026091f1-2997-48b1-8f2d-82b2520c1ff2")

    for {
      result <- graph(Path("/collection"))
      fact <- result
    } fact.set(Collection(Vector(uuid1, uuid2)))

    graph.save()

    it("counts truthy values") {
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

      assert(fact.get(0) == Result.Complete(1))
    }

    it("throws an error if asked to count non-Boolean nodes") {
      assertThrows[UnsupportedOperationException] {
        val definition = FactDefinition.fromConfig(
          FactConfigElement(
            "/stringTest",
            None,
            Some(
              new CompNodeConfigElement(
                "Count",
                Seq(
                  new CompNodeConfigElement(
                    "Dependency",
                    Seq.empty,
                    "/collection/*/string"
                  )
                )
              )
            ),
            None
          )
        )(using dictionary)

        definition.meta
      }
    }
  }
