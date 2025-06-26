package gov.irs.factgraph.compnodes

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.*
import gov.irs.factgraph.definitions.fact.{
  CommonOptionConfigTraits,
  CompNodeConfigElement,
  FactConfigElement,
  WritableConfigElement
}
import gov.irs.factgraph.monads.Result
import gov.irs.factgraph.types.{Collection, CollectionItem}

import java.util.UUID

class CollectionItemNodeSpec extends AnyFunSpec:
  describe("CollectionNode") {
    describe(".switch/.dependency") {
      val dictionary = FactDictionary()
      val config = FactConfigElement(
        "/test",
        None,
        Some(
          new CompNodeConfigElement(
            "Switch",
            Seq(
              new CompNodeConfigElement(
                "Case",
                Seq(
                  new CompNodeConfigElement(
                    "When",
                    Seq(
                      new CompNodeConfigElement("False")
                    )
                  ),
                  new CompNodeConfigElement(
                    "Then",
                    Seq(
                      new CompNodeConfigElement(
                        "Dependency",
                        Seq.empty,
                        CommonOptionConfigTraits.path("/unknownItem")
                      )
                    )
                  )
                )
              ),
              new CompNodeConfigElement(
                "Case",
                Seq(
                  new CompNodeConfigElement(
                    "When",
                    Seq(
                      new CompNodeConfigElement("True")
                    )
                  ),
                  new CompNodeConfigElement(
                    "Then",
                    Seq(
                      new CompNodeConfigElement(
                        "Dependency",
                        Seq.empty,
                        CommonOptionConfigTraits.path("/knownItem")
                      )
                    )
                  )
                )
              )
            )
          )
        ),
        None
      )
      FactDefinition.fromConfig(config)(using dictionary)

      val writableConfig = FactConfigElement(
        "/collection",
        Some(new WritableConfigElement("Collection")),
        None,
        None
      )
      FactDefinition.fromConfig(writableConfig)(using dictionary)

      val unknownConfig = FactConfigElement(
        "/unknownItem",
        None,
        Some(
          new CompNodeConfigElement(
            "Find",
            Seq(
              new CompNodeConfigElement("False")
            ),
            CommonOptionConfigTraits.path("/collection")
          )
        ),
        None
      )
      FactDefinition.fromConfig(unknownConfig)(using dictionary)

      val knownConfig = FactConfigElement(
        "/knownItem",
        None,
        Some(
          new CompNodeConfigElement(
            "Find",
            Seq(
              new CompNodeConfigElement("True")
            ),
            CommonOptionConfigTraits.path("/collection")
          )
        ),
        None
      )
      FactDefinition.fromConfig(knownConfig)(using dictionary)

      val anotherConfig = FactConfigElement(
        "/anotherCollection",
        Some(new WritableConfigElement("Collection")),
        None,
        None
      )
      FactDefinition.fromConfig(anotherConfig)(using dictionary)

      val anotherUnknownConfig = FactConfigElement(
        "/anotherUnknownItem",
        None,
        Some(
          new CompNodeConfigElement(
            "Find",
            Seq(
              new CompNodeConfigElement("False")
            ),
            CommonOptionConfigTraits.path("/anotherCollection")
          )
        ),
        None
      )
      FactDefinition.fromConfig(anotherUnknownConfig)(using dictionary)

      val graph = Graph(dictionary)

      val uuid1: UUID = UUID.fromString("4a772c04-5445-4623-b216-edca2c79698a")
      val uuid2: UUID = UUID.fromString("a3a5b3ef-81ba-42e4-8c99-61fa13faae55")

      for {
        result <- graph(Path("/collection"))
        fact <- result
      } fact.set(Collection(Vector(uuid1, uuid2)))

      graph.save()

      it("can be depended on and used inside a switch statement") {
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Complete(CollectionItem(uuid1)))
      }

      it("throws if switching between different collections") {
        val config = FactConfigElement(
          "/badTest",
          None,
          Some(
            new CompNodeConfigElement(
              "Switch",
              Seq(
                new CompNodeConfigElement(
                  "Case",
                  Seq(
                    new CompNodeConfigElement(
                      "When",
                      Seq(
                        new CompNodeConfigElement("False")
                      )
                    ),
                    new CompNodeConfigElement(
                      "Then",
                      Seq(
                        new CompNodeConfigElement(
                          "Dependency",
                          Seq.empty,
                          CommonOptionConfigTraits.path("/anotherUnknownItem")
                        )
                      )
                    )
                  )
                ),
                new CompNodeConfigElement(
                  "Case",
                  Seq(
                    new CompNodeConfigElement(
                      "When",
                      Seq(
                        new CompNodeConfigElement("True")
                      )
                    ),
                    new CompNodeConfigElement(
                      "Then",
                      Seq(
                        new CompNodeConfigElement(
                          "Dependency",
                          Seq.empty,
                          CommonOptionConfigTraits.path("/knownItem")
                        )
                      )
                    )
                  )
                )
              )
            )
          ),
          None
        )
        assertThrows[UnsupportedOperationException] {
          val definition = FactDefinition.fromConfig(config)(using dictionary)
          definition.meta
        }
      }
    }

    describe(".fromExpression") {
      it("throws an exception") {
        val node = CollectionItemNode(Expression.Constant(None), None)
        val newNode = node.fromExpression(
          Expression.Constant(
            Some(
              CollectionItem(
                UUID.fromString("ff381062-ab7a-42e7-ab8a-351b3c8fed39")
              )
            )
          )
        )
        assert(
          newNode.get(0) === Result.Complete(
            CollectionItem(
              UUID.fromString("ff381062-ab7a-42e7-ab8a-351b3c8fed39")
            )
          )
        )
      }
    }

    describe("$writablefromDerivedConfig") {
      val dictionary = FactDictionary()
      val config = FactConfigElement(
        "/collection",
        Some(new WritableConfigElement("Collection")),
        None,
        None
      )
      FactDefinition.fromConfig(config)(using dictionary)

      val config2 = FactConfigElement(
        "/collection/*/int",
        Some(new WritableConfigElement("Int")),
        None,
        None
      )
      FactDefinition.fromConfig(config2)(using dictionary)

      val config3 = FactConfigElement(
        "/test",
        Some(new WritableConfigElement("CollectionItem", "/collection")),
        None,
        None
      )
      FactDefinition.fromConfig(config3)(using dictionary)

      val graph = Graph(dictionary)

      val uuid1: UUID = UUID.fromString("94ee5aea-3463-4206-b396-31774caa9a1d")
      val uuid2: UUID = UUID.fromString("18886ae7-2387-4915-ac69-cd3c57ebbef6")

      for {
        result <- graph(Path("/collection"))
        fact <- result
      } fact.set(Collection(Vector(uuid1, uuid2)))

      graph.save()

      for {
        result <- graph(Path(s"/collection/#$uuid1/int"))
        fact <- result
      } fact.set(1)

      for {
        result <- graph(Path(s"/collection/#$uuid2/int"))
        fact <- result
      } fact.set(2)

      graph.save()

      val fact = graph(Path("/test"))(0).get

      it("can read and write a value") {
        assert(fact.get(0) == Result.Incomplete)

        assert(graph(Path("/test/int"))(0).complete == false)
        assert(graph(Path("/test/int"))(0).get.get(0) == Result.Incomplete)

        fact.set(CollectionItem(uuid2))
        graph.save()

        assert(fact.get(0) == Result.Complete(CollectionItem(uuid2)))
        assert(graph(Path("/test/int"))(0).complete == true)
        assert(graph(Path("/test/int"))(0).get.get(0) == Result.Complete(2))
      }
    }
  }
