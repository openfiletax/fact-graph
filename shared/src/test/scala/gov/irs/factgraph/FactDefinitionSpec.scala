package gov.irs.factgraph

import org.scalatest.funspec.AnyFunSpec
import gov.irs.factgraph.compnodes.*
import gov.irs.factgraph.definitions.*
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
import gov.irs.factgraph.types.{Collection, CollectionItem}

class FactDefinitionSpec extends AnyFunSpec:
  describe("FactDefinition") {
    describe("$apply") {
      it("adds the definition to the dictionary") {
        val value = IntNode(42)
        val path = Path("/test")
        val dictionary = FactDictionary()

        val definition = FactDefinition(value, path, Seq.empty, dictionary)

        assert(definition.value == value)
        assert(definition.path == path)
        assert(definition == dictionary(path).get)
      }

      describe("when the path is not abstract") {
        it("throws an exception") {
          val value = IntNode(42)
          val path = Path("test")
          val dictionary = FactDictionary()

          assertThrows[IllegalArgumentException] {
            FactDefinition(value, path, Seq.empty, dictionary)
          }
        }
      }
    }

    describe("$fromConfig") {
      it("parses a fact from config") {
        val definition = FactDefinition.fromConfig(
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
        )

        assert(definition.value == IntNode(42))
        assert(definition.path == Path("/test"))
        assert(definition.size == Factual.Size.Single)
        assert(definition.abstractPath == Path("/test"))
      }

      it("accepts a placeholder on a derived fact") {
        val definition = FactDefinition.fromConfig(
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
            Some(
              CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("0")
              )
            )
          )
        )

        assert(
          definition.value.expr match
            case Expression.Binary(source, default, _) =>
              source == Expression.Constant(Some(42)) &&
              default == Expression.Constant(Some(0))
            case _ => false
        )
      }

      it("accepts a placeholder on a writable fact") {
        val definition = FactDefinition.fromConfig(
          FactConfigElement(
            "/test",
            Some(
              new WritableConfigElement("Int")
            ),
            None,
            Some(
              CompNodeConfigElement(
                "Int",
                Seq.empty,
                CommonOptionConfigTraits.value("0")
              )
            )
          )
        )

        assert(
          definition.value.expr match
            case Expression.Binary(Expression.Writable(_), default, _) =>
              default == Expression.Constant(Some(0))
            case _ => false
        )
      }

      it("throws an exception if given both a <Writable> and a <Derived>") {
        assertThrows[IllegalArgumentException] {
          val definition = FactDefinition.fromConfig(
            FactConfigElement(
              "/test",
              Some(
                new WritableConfigElement("Int")
              ),
              Some(
                CompNodeConfigElement(
                  "Int",
                  Seq.empty,
                  CommonOptionConfigTraits.value("42")
                )
              ),
              None
            )
          )
        }
      }

      it("throws an exception if not given a <Writable> or a <Derived>") {
        assertThrows[IllegalArgumentException] {
          FactDefinition.fromConfig(
            FactConfigElement("/test", None, None, None)
          )
        }
      }

    }

    describe(".get/.getThunk") {
      describe("when the fact describes a Single value") {
        val definition = FactDefinition.fromConfig(
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
        )

        it("returns an incomplete result") {
          assert(definition.get == MaybeVector(Result.Incomplete))
          assert(
            definition.getThunk.map(_.get) ==
              MaybeVector(Result.Incomplete)
          )
        }
      }
    }

    describe("when the fact describes a Multiple value") {

      FactDefinition.fromConfig(
        FactConfigElement(
          "/collection",
          Some(
            new WritableConfigElement("Collection")
          ),
          None,
          None
        )
      )

      FactDefinition.fromConfig(
        FactConfigElement(
          "/collection/*/value",
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
      )

      val definition = FactDefinition.fromConfig(
        FactConfigElement(
          "/test",
          None,
          Some(
            CompNodeConfigElement(
              "Dependency",
              Seq.empty,
              CommonOptionConfigTraits.path("/collection/*/value")
            )
          ),
          None
        )
      )

      it("returns an empty vector") {
        assert(definition.get == MaybeVector(Nil, true))
        assert(definition.getThunk == MaybeVector(Nil, true))
      }
    }

    describe(".apply") {
      val dictionary = FactDictionary()

      val definition = FactDefinition.fromConfig(
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
      val sibling = FactDefinition.fromConfig(
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
      val child = FactDefinition.fromConfig(
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
      val collection = FactDefinition.fromConfig(
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
          "/collection/*/not",
          None,
          Some(
            new CompNodeConfigElement(
              "Not",
              Seq(
                new CompNodeConfigElement("Dependency", Seq.empty, "../bool")
              )
            )
          ),
          None
        )
      )

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
      val nestedCollection = FactDefinition.fromConfig(
        FactConfigElement(
          "/collection/*/anotherCollection",
          Some(
            new WritableConfigElement("Collection")
          ),
          None,
          None
        )
      )(using dictionary)
      val nestedFact = FactDefinition.fromConfig(
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

      it("returns the definition if it exists") {
        assert(definition(Path("child"))(0).get == child)
        assert(definition(PathItem("child"))(0).get == child)
        assert(definition(Path("/test/child"))(0).get == child)
        assert(definition(Path("/sibling"))(0).get == sibling)
        assert(definition(Path("/test"))(0).get == definition)
        assert(definition(Path("/collection"))(0).get == collection)
      }

      it("returns a vector of facts inside a collection") {
        val vect = definition(Path("/collection/*/anotherCollection"))
        assert(vect.length.contains(1))
        assert(vect(0).get == nestedCollection)

        val nestedVect =
          definition(Path("/collection/*/anotherCollection/*/test"))
        assert(nestedVect.length.contains(1))
        assert(nestedVect(0).get == nestedFact)
      }

      it("returns a vector of facts inside a derived collection") {
        val vect = definition(Path("/filteredCollection/*/anotherCollection"))
        assert(vect.length.contains(1))
        assert(vect(0).get == nestedCollection)

        val nestedVect =
          definition(Path("/filteredCollection/*/anotherCollection/*/test"))
        assert(nestedVect.length.contains(1))
        assert(nestedVect(0).get == nestedFact)
      }

      it("extracts a node for collection items") {
        val vect = definition(Path("/collection/*"))
        assert(vect.length.contains(1))
        assert(vect(0).get.value.isInstanceOf[CollectionItemNode])

        val nestedVect = definition(Path("/collection/*/anotherCollection/*"))
        assert(nestedVect.length.contains(1))
        assert(nestedVect(0).get.value.isInstanceOf[CollectionItemNode])
      }

      it("backtracks when it encounters a '..'") {
        assert(definition(Path("child/.."))(0).get == definition)
        assert(definition(Path("../sibling"))(0).get == sibling)
      }

      it("returns an incomplete result if the fact is undefined") {
        assert(
          definition(Path("child/grandchild")) == MaybeVector(Result.Incomplete)
        )
        assert(
          definition(
            Path("child/grandchild/greatgrandchild")
          ) == MaybeVector(Result.Incomplete)
        )
      }

      it(
        "returns an incomplete result if a wildcard is used on a non-collection fact"
      ) {
        assert(definition(Path("*")) == MaybeVector(Result.Incomplete))
      }
    }

    describe(".attachToGraph") {
      it("hooks up the fact to the graph") {
        val dictionary = FactDictionary()
        val graph = Graph(dictionary)
        val definition = FactDefinition.fromConfig(
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

        val fact = definition.attachToGraph(graph.root, PathItem("test"))

        assert(fact.graph == graph)
        assert(fact.meta == definition.meta)
        assert(fact.get == MaybeVector(Result.Complete(42)))
      }
    }

    describe(".asTuple") {
      it("returns a key, definition pair") {
        val definition = FactDefinition.fromConfig(
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
        )

        assert(definition.asTuple == (definition.path, definition))
      }
    }

    describe(".isWritable") {
      describe("when the Fact is writable") {
        it("returns true") {
          val definition = FactDefinition.fromConfig(
            FactConfigElement(
              "/test",
              Some(
                new WritableConfigElement("Int")
              ),
              None,
              None
            )
          )

          assert(definition.isWritable)
        }
      }

      describe("when the Fact is not writable") {
        it("returns false") {
          val definition = FactDefinition.fromConfig(
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
          )

          assert(!definition.isWritable)
        }
      }
    }
  }
