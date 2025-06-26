package gov.irs.factgraph

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.compnodes.CollectionNode
import gov.irs.factgraph.definitions.fact.{
  CommonOptionConfigTraits,
  CompNodeConfigElement,
  FactConfigElement,
  WritableConfigElement
}
import gov.irs.factgraph.definitions.meta.{
  EnumDeclarationTrait,
  MetaConfigTrait
}
import gov.irs.factgraph.monads.{MaybeVector, Result}
import gov.irs.factgraph.types.Collection

import java.util.UUID

class FactSpec extends AnyFunSpec:
  describe("Fact") {
    describe(".apply") {
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
      FactDefinition.fromConfig(
        FactConfigElement(
          "/sibling",
          None,
          Some(
            CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("43")
            )
          ),
          None
        )
      )(using dictionary)
      FactDefinition.fromConfig(
        FactConfigElement(
          "/test/child",
          None,
          Some(
            CompNodeConfigElement(
              "Int",
              Seq.empty,
              CommonOptionConfigTraits.value("44")
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
          None,
          Some(
            new CompNodeConfigElement("True")
          ),
          None
        )
      )(using dictionary)
      FactDefinition.fromConfig(
        FactConfigElement(
          "/filteredCollection",
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

      dictionary.addMeta(new MetaConfigTrait {
        override def version: String = "1"
      })

      dictionary.freeze()
      val graph = Graph(dictionary)

      val fact = graph(Path("/test"))(0).get

      it("returns the fact if it exists") {
        assert(fact(Path("child"))(0).get.get(0) == Result.Complete(44))
        assert(fact(PathItem("child"))(0).get.get(0) == Result.Complete(44))
        assert(fact(Path("/test/child"))(0).get.get(0) == Result.Complete(44))
        assert(fact(Path("/sibling"))(0).get.get(0) == Result.Complete(43))
        assert(fact(Path("/test"))(0).get.get(0) == Result.Complete(42))
      }

      it("backtracks when it encounters a '..'") {
        assert(fact(Path("child/.."))(0).get.get(0) == Result.Complete(42))
        assert(fact(Path("../sibling"))(0).get.get(0) == Result.Complete(43))
      }

      it("returns an incomplete result if the fact is undefined") {
        assert(fact(Path("child/grandchild")) == MaybeVector(Result.Incomplete))
        assert(
          fact(Path("child/grandchild/greatgrandchild")) == MaybeVector(
            Result.Incomplete
          )
        )
      }

      it("returns an empty vector when a collection is incomplete") {
        val empty = MaybeVector(Nil, false)
        assert(graph(Path("/collection/*")) == empty)
        assert(graph(Path("/collection/*/anotherCollection")) == empty)
        assert(graph(Path("/collection/*/anotherCollection/*")) == empty)
        assert(graph(Path("/collection/*/anotherCollection/*/test")) == empty)
        assert(graph(Path("/filteredCollection/*")) == empty)
        assert(graph(Path("/filteredCollection/*/anotherCollection")) == empty)
        assert(
          graph(Path("/filteredCollection/*/anotherCollection/*")) == empty
        )
        assert(
          graph(Path("/filteredCollection/*/anotherCollection/*/test")) == empty
        )
      }

      it(
        "returns an incomplete result if a wildcard is used on a non-collection fact"
      ) {
        assert(fact(Path("*")) == MaybeVector(Result.Incomplete))
      }

      it(
        "returns an incomplete result if a member is used on a non-collection fact"
      ) {
        val pathItem = PathItem.Member(
          UUID.fromString("c38fe164-1502-4f10-8786-414824bddf21")
        )
        assert(fact(pathItem) == MaybeVector(Result.Incomplete))
      }

      describe("when collections are defined") {
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

        it("retrieves a single fact for specified collection members") {
          val path = s"/collection/#$uuid1/anotherCollection/#$uuid2/test"
          val fact = graph(Path(path))(0).get
          assert(fact.get(0) == Result.Complete(42))
        }

        it("explodes nested collections into a vector of facts") {
          val vect = graph(Path("/collection/*/anotherCollection/*/test"))

          for {
            result <- vect
            fact <- result
            value <- fact.get
          } assert(value == Result.Complete(42))

          assert(vect.length.contains(4))
        }

        it("returns an incomplete result if the id is not in the collection") {
          val path =
            s"/collection/#${UUID.fromString("c1c6d6bb-2aa4-4939-be87-e3fcf1429d07")}"
          val result = graph(Path(path))(0)
          assert(result == Result.Incomplete)
        }

        it(
          "retrieves a single fact for specified member of derived collection"
        ) {
          val path =
            s"/filteredCollection/#$uuid1/anotherCollection/#$uuid2/test"
          val fact = graph(Path(path))(0).get
          assert(fact.get(0) == Result.Complete(42))
        }

        it("wildcard retrieves all children of the collection") {
          val path = s"/collection/*/anotherCollection/*/test"
          assert(graph(Path(path)).length.get == 4)

          val fact = graph(Path(path))(3).get
          assert(fact.get(0) == Result.Complete(42))
        }

        it("wildcard retrieves all children of the derived collection") {
          val path = s"/filteredCollection/*/anotherCollection/*/test"
          assert(graph(Path(path)).length.get == 4)

          val fact = graph(Path(path))(3).get
          assert(fact.get(0) == Result.Complete(42))
        }

        it("recycles fact objects for collection members") {
          val path = s"/collection/#$uuid1/anotherCollection/#$uuid2/test"
          assert(graph(Path(path))(0).get == graph(Path(path))(0).get)
        }

        it("doesn't persist collection members after the graph has changed") {
          val path = s"/collection/#$uuid1"
          val fact = graph(Path(path))(0).get

          graph.save()

          assert(fact != graph(Path(path))(0).get)
        }
      }

      it("recycles the fact object") {
        val child = fact(Path("child"))(0).get

        assert(fact(PathItem("child"))(0).get == child)
        assert(fact(Path("/test/child"))(0).get == child)
        assert(fact(Path("../test/child"))(0).get == child)
      }

      it("doesn't persist facts after the graph has changed") {
        val child = fact(Path("child"))(0).get

        graph.save()

        assert(fact(PathItem("child"))(0).get != child)
        assert(fact(Path("/test/child"))(0).get != child)
        assert(fact(Path("../test/child"))(0).get != child)
      }

      it("doesn't persist collection members after they've been removed") {
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

        val path = s"/collection/#$uuid1/anotherCollection/#$uuid2/test"

        assert(graph(Path(path))(0).get.get(0) == Result.Complete(42))

        for {
          result <- graph(Path("/collection/*/anotherCollection"))
          fact <- result
        } fact.set(Collection(Vector()), true)

        graph.save()

        assert(graph(Path(path))(0) == Result.Incomplete)
      }
    }

    describe(".get") {
      it("gets the result") {
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
        val fact = graph(Path("/test"))(0).get

        assert(fact.get == MaybeVector.Single(Result.Complete(42)))
      }
    }

    describe(".getThunk") {
      it("returns a thunk to get the result") {
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
        val fact = graph(Path("/test"))(0).get

        assert(fact.getThunk(0).get == Result.Complete(42))
      }
    }
  }
