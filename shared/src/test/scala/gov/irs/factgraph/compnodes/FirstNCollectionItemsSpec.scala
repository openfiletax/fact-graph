package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.Collection
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.{
  CompNodeConfigElement,
  FactConfigElement,
  WritableConfigElement,
  CommonOptionConfigTraits
}

import java.util.UUID

class FirstNCollectionItemsSpec extends AnyFunSpec:
  describe("FirstNCollectionItems") {
    val dictionary = FactDictionary()
    FactDefinition.fromConfig(
      FactConfigElement(
        "/first2",
        None,
        Some(
          new CompNodeConfigElement(
            "FirstNCollectionItems",
            Seq(
              new CompNodeConfigElement(
                "Collection",
                Seq(
                  new CompNodeConfigElement(
                    "Dependency",
                    Seq.empty,
                    "/collection"
                  )
                )
              ),
              new CompNodeConfigElement(
                "Count",
                Seq(
                  CompNodeConfigElement(
                    "Int",
                    Seq(),
                    CommonOptionConfigTraits.value("2")
                  )
                )
              )
            )
          )
        ),
        None
      )
    )(using dictionary)

    FactDefinition.fromConfig(
      FactConfigElement(
        "/first4",
        None,
        Some(
          new CompNodeConfigElement(
            "FirstNCollectionItems",
            Seq(
              new CompNodeConfigElement(
                "Collection",
                Seq(
                  new CompNodeConfigElement(
                    "Dependency",
                    Seq.empty,
                    "/collection"
                  )
                )
              ),
              new CompNodeConfigElement(
                "Count",
                Seq(
                  CompNodeConfigElement(
                    "Int",
                    Seq(),
                    CommonOptionConfigTraits.value("4")
                  )
                )
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

    val graph = Graph(dictionary)

    val uuid1: UUID = UUID.fromString("8997039b-31e7-4df9-97fd-9c30f6293f40")
    val uuid2: UUID = UUID.fromString("3009533e-b68d-4b16-8401-295e919daeb0")
    val uuid3: UUID = UUID.fromString("3009533e-b68d-4b16-8401-295e919daeb0")

    for {
      result <- graph(Path("/collection"))
      fact <- result
    } fact.set(Collection(Vector(uuid1, uuid2, uuid3)))

    graph.save()

    it("can get the first 2 collections items from a collection") {
      val fact = graph(Path("/first2"))(0).get

      assert(fact.get(0) == Result.Complete(Collection(Vector(uuid1, uuid2))))
    }

    it(
      "Overflowing (e.g. getting the first 4 elements of a 3 element collection) will return the full collection"
    ) {
      val fact = graph(Path("/first4"))(0).get

      assert(
        fact.get(0) == Result.Complete(Collection(Vector(uuid1, uuid2, uuid3)))
      )
    }
  }
