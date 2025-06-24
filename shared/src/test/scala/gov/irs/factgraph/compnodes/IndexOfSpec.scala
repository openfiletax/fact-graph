package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.{Collection, CollectionItem}
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.{
  CommonOptionConfigTraits,
  CompNodeConfigElement,
  FactConfigElement,
  WritableConfigElement
}

import java.util.UUID

class IndexOfSpec extends AnyFunSpec {
  describe("IndexOf") {
    val dictionary = FactDictionary()
    FactDefinition.fromConfig(
      FactConfigElement(
        "/test",
        None,
        Some(
          new CompNodeConfigElement(
            "IndexOf",
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
                "Index",
                Seq(
                  CompNodeConfigElement(
                    "Int",
                    Seq(),
                    CommonOptionConfigTraits.value("1")
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
        Some(new WritableConfigElement("Collection")),
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

    FactDefinition.fromConfig(
      FactConfigElement(
        "/secondString",
        None,
        Some(
          new CompNodeConfigElement("Dependency", Seq.empty, "/test/string")
        ),
        None
      )
    )(using dictionary)

    val graph = Graph(dictionary)
    val uuid1: UUID = UUID.fromString("bd54a80b-8d87-4c55-99a1-3bfa514ef613")
    val uuid2: UUID = UUID.fromString("b463129f-b688-4173-a6fc-a5c18c2cbb2d")

    describe("returns an incomplete when") {
      it("has an incomplete collection") {
        val fact = graph.get("/test")
        assert(fact == Result.Incomplete)
      }

      it("doesn't have the index required") {
        for {
          result <- graph(Path("/collection"))
          fact <- result
        } fact.set(Collection(Vector(uuid1)))
        graph.save()
        graph.set(s"/collection/#$uuid1/string", "first")
        val fact = graph.get("/test")
        assert(fact == Result.Incomplete)
      }
    }
    describe("return complete when") {
      it("finds the index in the collection") {
        // add both to the collection and get the second item in the collection
        for {
          result <- graph(Path("/collection"))
          fact <- result
        } fact.set(Collection(Vector(uuid1, uuid2)))

        graph.save()
        graph.set(s"/collection/#$uuid1/string", "first")
        graph.set(s"/collection/#$uuid2/string", "second")

        graph.save()
        val fact = graph.get("/test")
        assert(fact == Result.Complete(CollectionItem(uuid2)))
        val stringFact = graph.get("/test/string")
        assert(stringFact == Result.Complete("second"))
        val furtherProof = graph.get("/secondString")
        assert(furtherProof == Result.Complete("second"))
      }
    }

    describe("throws when") {
      it("doesn't have a collection") {
        assertThrows[UnsupportedOperationException] {
          FactDefinition.fromConfig(
            FactConfigElement(
              "/test2",
              None,
              Some(
                new CompNodeConfigElement(
                  "IndexOf",
                  Seq(
                    new CompNodeConfigElement(
                      "Collection",
                      Seq(
                        CompNodeConfigElement(
                          "Int",
                          Seq(),
                          CommonOptionConfigTraits.value("2")
                        )
                      )
                    ),
                    new CompNodeConfigElement(
                      "Index",
                      Seq(
                        CompNodeConfigElement(
                          "Int",
                          Seq(),
                          CommonOptionConfigTraits.value("1")
                        )
                      )
                    )
                  )
                )
              ),
              None
            )
          )(using dictionary)
          val graph2 = Graph(dictionary)
          graph2.get("/test2")
        }
      }

      it("doesn't have an integer index") {
        assertThrows[UnsupportedOperationException] {
          FactDefinition.fromConfig(
            FactConfigElement(
              "/test2",
              None,
              Some(
                new CompNodeConfigElement(
                  "IndexOf",
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
                      "Index",
                      Seq(
                        CompNodeConfigElement(
                          "Dollar",
                          Seq(),
                          CommonOptionConfigTraits.value("1.00")
                        )
                      )
                    )
                  )
                )
              ),
              None
            )
          )(using dictionary)
          val graph2 = Graph(dictionary)
          graph2.get("/test2")
        }
      }
    }
  }

}
