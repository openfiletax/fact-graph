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

class CollectionNodeSpec extends AnyFunSpec:
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
                    Seq(new CompNodeConfigElement("False"))
                  ),
                  new CompNodeConfigElement(
                    "Then",
                    Seq(
                      new CompNodeConfigElement(
                        "Dependency",
                        Seq.empty,
                        "/filteredCollection"
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
                    Seq(new CompNodeConfigElement("True"))
                  ),
                  new CompNodeConfigElement(
                    "Then",
                    Seq(
                      new CompNodeConfigElement(
                        "Dependency",
                        Seq.empty,
                        "/collection"
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

      val config2 = FactConfigElement(
        "/collection",
        Some(new WritableConfigElement("Collection")),
        None,
        None
      )
      FactDefinition.fromConfig(config2)(using dictionary)

      val config3 = FactConfigElement(
        "/filteredCollection",
        None,
        Some(
          CompNodeConfigElement(
            "Filter",
            Seq(
              new CompNodeConfigElement("False")
            ),
            CommonOptionConfigTraits.path("/collection")
          )
        ),
        None
      )
      FactDefinition.fromConfig(config3)(using dictionary)

      val config4 = FactConfigElement(
        "/anotherCollection",
        Some(new WritableConfigElement("Collection")),
        None,
        None
      )
      FactDefinition.fromConfig(config4)(using dictionary)

      val graph = Graph(dictionary)

      val uuid1: UUID = UUID.fromString("795b3be3-4006-433d-9665-e028734dfe04")
      val uuid2: UUID = UUID.fromString("890ca049-20e8-46db-8945-0a361363ef46")
      val collection = Collection(Vector(uuid1, uuid2))

      for {
        result <- graph(Path("/collection"))
        fact <- result
      } fact.set(collection)

      graph.save()

      it("can be depended on and used inside a switch statement") {
        val fact = graph(Path("/test"))(0).get

        assert(fact.get(0) == Result.Complete(collection))
      }

      it("throws if switching between different collections") {
        assertThrows[UnsupportedOperationException] {
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
                        Seq(new CompNodeConfigElement("False"))
                      ),
                      new CompNodeConfigElement(
                        "Then",
                        Seq(
                          new CompNodeConfigElement(
                            "Dependency",
                            Seq.empty,
                            "/anotherCollection"
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
                        Seq(new CompNodeConfigElement("True"))
                      ),
                      new CompNodeConfigElement(
                        "Then",
                        Seq(
                          new CompNodeConfigElement(
                            "Dependency",
                            Seq.empty,
                            "/collection"
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
          val definition = FactDefinition.fromConfig(config)(using dictionary)

          definition.meta
        }
      }
    }

    describe("can perform an operation across collections") {
      val dictionary = FactDictionary()
      val config = FactConfigElement(
        "/filers",
        Some(new WritableConfigElement("Collection")),
        None,
        None
      )
      FactDefinition.fromConfig(config)(using dictionary)

      val config2 = FactConfigElement(
        "/filers/*/isBlind",
        Some(new WritableConfigElement("Boolean")),
        None,
        None
      )
      FactDefinition.fromConfig(config2)(using dictionary)

      val config3 = FactConfigElement(
        "/filers/*/age65OrOlder",
        Some(new WritableConfigElement("Boolean")),
        None,
        None
      )
      FactDefinition.fromConfig(config3)(using dictionary)

      val config4 = FactConfigElement(
        "/testCalc",
        None,
        Some(
          new CompNodeConfigElement(
            "Add",
            Seq(
              new CompNodeConfigElement(
                "Count",
                Seq(
                  new CompNodeConfigElement(
                    "Dependency",
                    Seq.empty,
                    CommonOptionConfigTraits.path("/filers/*/isBlind")
                  )
                ),
                Seq.empty
              ),
              new CompNodeConfigElement(
                "Count",
                Seq(
                  new CompNodeConfigElement(
                    "Dependency",
                    Seq.empty,
                    CommonOptionConfigTraits.path("/filers/*/age65OrOlder")
                  )
                ),
                Seq.empty
              )
            ),
            Seq.empty
          )
        ),
        None
      )
      FactDefinition.fromConfig(config4)(using dictionary)

      val graph = Graph(dictionary)

      val uuid1: UUID = UUID.fromString("795b3be3-4006-433d-9665-e028734dfe04")
      val uuid2: UUID = UUID.fromString("890ca049-20e8-46db-8945-0a361363ef46")
      val collection = Collection(Vector(uuid1, uuid2))

      for {
        result <- graph(Path("/filers"))
        fact <- result
      } fact.set(collection)

      graph.save()

      it("can add up the correct number of booleans") {
        graph.set(Path(s"/filers/#$uuid1/isBlind"), true)
        graph.set(Path(s"/filers/#$uuid2/isBlind"), false)
        graph.set(Path(s"/filers/#$uuid1/age65OrOlder"), false)
        graph.set(Path(s"/filers/#$uuid2/age65OrOlder"), false)
        graph.save()
        assert(graph(Path("/testCalc"))(0).complete == true)
        assert(graph(Path("/testCalc"))(0).get.get(0) == Result.Complete(1))

        graph.set(Path(s"/filers/#$uuid1/isBlind"), true)
        graph.set(Path(s"/filers/#$uuid2/isBlind"), true)
        graph.set(Path(s"/filers/#$uuid1/age65OrOlder"), true)
        graph.set(Path(s"/filers/#$uuid2/age65OrOlder"), true)
        graph.save()
        assert(graph(Path("/testCalc"))(0).complete == true)
        assert(graph(Path("/testCalc"))(0).get.get(0) == Result.Complete(4))
      }
    }

    describe(".extract") {
      val uuid = UUID.fromString("1a4762f9-f103-4ca1-8705-753d534afaeb")
      val collection = Collection(Vector(uuid))
      val collectionNode =
        CollectionNode(Expression.Constant(Some(collection)), None)

      it("returns a CollectionItemNode when given a Member id") {
        val extract = collectionNode.extract(PathItem.Member(uuid)).get
        val result = extract.get(0)
        val collectionItem = result.get.asInstanceOf[CollectionItem]

        assert(collectionItem.id == uuid)
      }

      it("returns an incomplete CollectionItemNode when given an Unknown") {
        val extract = collectionNode.extract(PathItem.Unknown).get
        val result = extract.get(0)

        assert(result == Result.Incomplete)
      }

      it("returns nothing when given anything else") {
        assert(collectionNode.extract(PathItem("test")).isEmpty)
      }
    }

    describe(".fromExpression") {
      it("throws an exception") {
        val node = CollectionNode(Expression.Constant(None), None)
        assertThrows[UnsupportedOperationException] {
          node.fromExpression(Expression.Constant(None))
        }
      }
    }

    describe("$writableFromXml") {
      it("can read and write a value") {
        val dictionary = FactDictionary()
        val config = FactConfigElement(
          "/test",
          Some(new WritableConfigElement("Collection")),
          None,
          None
        )
        FactDefinition.fromConfig(config)(using dictionary)

        val graph = Graph(dictionary)
        val fact = graph(Path("/test"))(0).get

        given Factual = fact

        assert(fact.get(0) == Result.Incomplete)

        val collection = Collection(
          Vector(UUID.fromString("d3f7782e-48c8-4717-8aa4-0a1d677022af"))
        )

        fact.value match
          case value: CollectionNode => value.set(collection)
        graph.save()

        assert(fact.get(0) == Result.Complete(collection))
      }
    }
  }
